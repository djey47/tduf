package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class allowing to convert database patches between different formats (TDUF, TDUMT)
 */
public class PatchConverter {

    private static Class<PatchConverter> thisClass = PatchConverter.class;

    /**
     * Convertit un patch TDUF en patch TDUMT (XML).
     * @param tdufDatabasePatch : TDUF patch object to convert
     * @return corresponding TDUMT patch as XML document.
     */
    // TODO generate one instruction per topic (append values)
    public static Document jsonToPch(DbPatchDto tdufDatabasePatch) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        requireNonNull(tdufDatabasePatch, "A TDUF database patch object is required.");

        Document patchDocument = initXmlDocumentFromTemplate();

        List<Element> instructionElements = tdufDatabasePatch.getChanges().stream()

                .map((changeObject) -> changeObjectToInstruction(changeObject, patchDocument))

                .collect(toList());

        Node instructionsNode = patchDocument.getElementsByTagName("instructions").item(0);
        instructionElements.forEach(instructionsNode::appendChild);

        return patchDocument;
    }

    private static Element changeObjectToInstruction(DbPatchDto.DbChangeDto changeObject, Document patchDocument) {
        DbPatchDto.DbChangeDto.ChangeTypeEnum type = changeObject.getType();
        String instructionType;
        String resourceValues;
        switch (type) {
            case UPDATE:
                instructionType = "updateDatabase";
                resourceValues = getEntryValues(Optional.ofNullable(changeObject.getRef()), changeObject.getValues());
                break;
            case UPDATE_RES:
                instructionType = "updateResource";
                resourceValues = getResourceValues(changeObject.getRef(), changeObject.getValue());
                break;
            default:
                throw new IllegalArgumentException("Unhandled instruction type: " + type);
        }

        Element instructionElement = patchDocument.createElement("instruction");

        addAtribute("type", instructionType, instructionElement, patchDocument);
        addAtribute("failOnError", "True", instructionElement, patchDocument);
        addAtribute("enabled", "True", instructionElement, patchDocument);

        addParameter("resourceFileName", getTopicLabel(changeObject.getTopic()), patchDocument, instructionElement);
        addParameter("resourceValues", resourceValues, patchDocument, instructionElement);

        return instructionElement;
    }

    private static void addAtribute(String name, String value, Element instructionElement, Document patchDocument) {
        Attr typeAttribute = patchDocument.createAttribute(name);
        typeAttribute.setValue(value);
        instructionElement.setAttributeNode(typeAttribute);
    }

    private static void addParameter(String name, String value, Document patchDocument, Element instructionElement) {
        Element resourceParameterElement = patchDocument.createElement("parameter");
        resourceParameterElement.setAttribute("name", name);
        resourceParameterElement.setAttribute("value", value);
        instructionElement.appendChild(resourceParameterElement);
    }

    private static String getResourceValues(String ref, String value) {
        return ref + "|" + value;
    }

    private static String getEntryValues(Optional<String> potentialRef, List<String> values) {
        StringBuilder builder = new StringBuilder();

        builder
                .append(potentialRef.get())
                .append("|");

        values.forEach((itemValue) -> builder
                .append(itemValue)
                .append("\t"));

        return builder.toString().trim();
    }

    private static String getTopicLabel(DbDto.Topic topic) {
        String topicLabel = topic.getLabel();
        return topicLabel.substring(4, topicLabel.length());
    }

    private static Document initXmlDocumentFromTemplate() throws ParserConfigurationException, URISyntaxException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        String templateURI = thisClass.getResource("/files/db/tdumt/patchTemplate.xml").toURI().toString();
        return docBuilder.parse(templateURI);
    }
}
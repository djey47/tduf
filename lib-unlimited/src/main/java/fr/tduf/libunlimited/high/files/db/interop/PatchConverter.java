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

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class allowing to convert database patches between different formats (TDUF, TDUMT)
 */
public class PatchConverter {

    private static Class<PatchConverter> thisClass = PatchConverter.class;

    private static final String INSTRUCTION_TDUMT_UPDATE_DATABASE = "updateDatabase";
    private static final String INSTRUCTION_TDUMT_UPDATE_RESOURCE = "updateResource";

    private static final String SEPARATOR_KEY_VALUE = "|";
    private static final String SEPARATOR_ITEMS = "\t";

    /**
     * Convertit un patch TDUF en patch TDUMT (XML).
     * @param tdufDatabasePatch : TDUF patch object to convert
     * @return corresponding TDUMT patch as XML document.
     */
    // TODO generate one instruction per topic (append values)
    public static Document jsonToPch(DbPatchDto tdufDatabasePatch) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        requireNonNull(tdufDatabasePatch, "A TDUF database patch object is required.");

        Document patchDocument = initXmlDocumentFromTemplate();

        Node instructionsNode = patchDocument.getElementsByTagName("instructions").item(0);
        getUpdateElements(tdufDatabasePatch, UPDATE, patchDocument).forEach(instructionsNode::appendChild);
        getUpdateElements(tdufDatabasePatch, UPDATE_RES, patchDocument).forEach(instructionsNode::appendChild);

        return patchDocument;
    }

    private static List<Element> getUpdateElements(DbPatchDto tdufDatabasePatch, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, Document patchDocument) {
        return tdufDatabasePatch.getChanges().stream()

                .filter((changeObject) -> changeObject.getType() == changeType)

                .map((updateChangeObject) -> {
                    String resourceValues;
                    String instructionType;
                    if (UPDATE == changeType) {
                        instructionType = INSTRUCTION_TDUMT_UPDATE_DATABASE;
                        resourceValues = getEntryValues(Optional.ofNullable(updateChangeObject.getRef()), updateChangeObject.getValues());
                    } else {
                        instructionType = INSTRUCTION_TDUMT_UPDATE_RESOURCE;
                        resourceValues = getResourceValues(updateChangeObject.getRef(), updateChangeObject.getValue());
                    }

                    return changeObjectToInstruction(updateChangeObject, instructionType, resourceValues, patchDocument);
                })

                .collect(toList());
    }

    private static Element changeObjectToInstruction(DbPatchDto.DbChangeDto changeObject, String instructionType, String resourceValues, Document patchDocument) {
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
        return ref + SEPARATOR_KEY_VALUE + value;
    }

    private static String getEntryValues(Optional<String> potentialRef, List<String> values) {
        StringBuilder builder = new StringBuilder();

        builder
                .append(potentialRef.get())
                .append(SEPARATOR_KEY_VALUE);

        values.forEach((itemValue) -> builder
                .append(itemValue)
                .append(SEPARATOR_ITEMS));

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
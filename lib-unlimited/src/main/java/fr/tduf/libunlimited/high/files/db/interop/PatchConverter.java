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
import java.util.*;

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

    private static final String SEPARATOR_ENTRIES = "||";
    private static final String SEPARATOR_KEY_VALUE = "|";
    private static final String SEPARATOR_ITEMS = "\t";

    /**
     * Convertit un patch TDUF en patch TDUMT (XML).
     * @param tdufDatabasePatch : TDUF patch object to convert
     * @return corresponding TDUMT patch as XML document.
     */
    public static Document jsonToPch(DbPatchDto tdufDatabasePatch) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        requireNonNull(tdufDatabasePatch, "A TDUF database patch object is required.");

        Document patchDocument = initXmlDocumentFromTemplate();

        Node instructionsNode = patchDocument.getElementsByTagName("instructions").item(0);
        getUpdateElements(tdufDatabasePatch, UPDATE, patchDocument).forEach(instructionsNode::appendChild);
        getUpdateElements(tdufDatabasePatch, UPDATE_RES, patchDocument).forEach(instructionsNode::appendChild);

        return patchDocument;
    }

    private static List<Element> getUpdateElements(DbPatchDto tdufDatabasePatch, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, Document patchDocument) {

        return groupUpdateChangeObjectsByTopic(tdufDatabasePatch, changeType).entrySet().stream()

                .map((entry) -> createUpdateInstructionForTopicChanges(entry, changeType, patchDocument))

                .collect(toList());
    }

    private static Element createUpdateInstructionForTopicChanges(Map.Entry<DbDto.Topic, List<DbPatchDto.DbChangeDto>> entry, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, Document patchDocument) {
        DbDto.Topic topic = entry.getKey();
        List<DbPatchDto.DbChangeDto> changeObjects = entry.getValue();

        List<String> entries = changeObjects.stream()

                .map((updateChangeObject) -> {

                    if (UPDATE == changeType) {
                        return getContentsValue(Optional.ofNullable(updateChangeObject.getRef()), updateChangeObject.getValues());
                    } else {
                        return getResourceValue(updateChangeObject.getRef(), updateChangeObject.getValue());
                    }

                })

                .collect(toList());

        return createUpdateInstruction(topic, changeType, entries, patchDocument);
    }

    private static Map<DbDto.Topic, List<DbPatchDto.DbChangeDto>> groupUpdateChangeObjectsByTopic(DbPatchDto tdufDatabasePatch, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType) {
        Map<DbDto.Topic, List<DbPatchDto.DbChangeDto>> changeObjectsByTopic = new HashMap<>();

        tdufDatabasePatch.getChanges().stream()

                .filter((changeObject) -> changeObject.getType() == changeType)

                .forEach((changeObject) -> {

                    if (!changeObjectsByTopic.containsKey(changeObject.getTopic())) {
                        changeObjectsByTopic.put(changeObject.getTopic(), new ArrayList<>());
                    }
                    changeObjectsByTopic.get(changeObject.getTopic()).add(changeObject);

                });
        return changeObjectsByTopic;
    }

    private static Element createUpdateInstruction(DbDto.Topic topic, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, List<String> entries, Document patchDocument) {
        String instructionType = UPDATE == changeType ?
                INSTRUCTION_TDUMT_UPDATE_DATABASE :
                INSTRUCTION_TDUMT_UPDATE_RESOURCE;

        String resourceValues = String.join(SEPARATOR_ENTRIES, entries);

        Element instructionElement = patchDocument.createElement("instruction");

        addAttribute("type", instructionType, instructionElement, patchDocument);
        addAttribute("failOnError", "True", instructionElement, patchDocument);
        addAttribute("enabled", "True", instructionElement, patchDocument);

        addParameter("resourceFileName", getTopicLabel(topic), instructionElement, patchDocument);
        addParameter("resourceValues", resourceValues, instructionElement, patchDocument);

        return instructionElement;
    }

    private static void addAttribute(String name, String value, Element instructionElement, Document patchDocument) {
        Attr typeAttribute = patchDocument.createAttribute(name);
        typeAttribute.setValue(value);
        instructionElement.setAttributeNode(typeAttribute);
    }

    private static void addParameter(String name, String value, Element instructionElement, Document patchDocument) {
        Element resourceParameterElement = patchDocument.createElement("parameter");
        resourceParameterElement.setAttribute("name", name);
        resourceParameterElement.setAttribute("value", value);
        instructionElement.appendChild(resourceParameterElement);
    }

    private static String getResourceValue(String ref, String value) {
        return ref + SEPARATOR_KEY_VALUE + value;
    }

    private static String getContentsValue(Optional<String> potentialRef, List<String> values) {
        return potentialRef.get() + SEPARATOR_KEY_VALUE + String.join(SEPARATOR_ITEMS, values);
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
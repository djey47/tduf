package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class allowing to convert database patches between different formats (TDUF, TDUMT)
 */
public class PatchConverter {

    private static Class<PatchConverter> thisClass = PatchConverter.class;

    private static final String XML_ELEMENT_INSTRUCTIONS = "instructions";
    private static final String XML_ELEMENT_INSTRUCTION = "instruction";
    private static final String XML_ELEMENT_PARAMETER = "parameter";
    private static final String XML_ATTRIBUTE_INSTRUCTION_TYPE = "type";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_ATTRIBUTE_VALUE = "value";

    private static final String INSTRUCTION_TDUMT_UPDATE_DATABASE = "updateDatabase";
    private static final String INSTRUCTION_TDUMT_UPDATE_RESOURCE = "updateResource";
    private static final String PARAMETER_TDUMT_RESOURCE_FILE_NAME = "resourceFileName";
    private static final String PARAMETER_TDUMT_RESOURCE_VALUES = "resourceValues";

    private static final String SEPARATOR_ENTRIES = "||";
    private static final String SEPARATOR_KEY_VALUE = "|";
    private static final String SEPARATOR_ITEMS = "\t";

    private static final String PREFIX_TOPIC_LABEL = "TDU_";

    /**
     * Converts a TDUF patch into TDUMT one (XML).
     * @param tdufDatabasePatch : TDUF patch object to convert
     * @return corresponding TDUMT patch as XML document.
     */
    // TODO handle update patch without REF
    public static Document jsonToPch(DbPatchDto tdufDatabasePatch) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        requireNonNull(tdufDatabasePatch, "A TDUF database patch object is required.");

        Document patchDocument = initXmlDocumentFromResource("/files/db/tdumt/patchTemplate.xml");

        Element instructionsElement = findInstructionsElement(patchDocument);
        getUpdateElements(tdufDatabasePatch, UPDATE, patchDocument).forEach(instructionsElement::appendChild);
        getUpdateElements(tdufDatabasePatch, UPDATE_RES, patchDocument).forEach(instructionsElement::appendChild);

        return patchDocument;
    }

    /**
     * Converts a TDUMT (XML) patch into TDUF one.
     * @param tdumtDatabasePatch : TDUMT patch as XML document to convert
     * @return corresponding TDUF patch as DTO.
     */
    public static DbPatchDto pchToJson(Document tdumtDatabasePatch) {
        requireNonNull(tdumtDatabasePatch, "A TDUMT database patch document is required.");

        List<DbPatchDto.DbChangeDto> changesObjects = new ArrayList<>();

        NodeList instructions = findInstructionsElement(tdumtDatabasePatch).getElementsByTagName(XML_ELEMENT_INSTRUCTION);
        for (int i = 0 ; i < instructions.getLength() ; i++) {
            Element instructionElement = (Element) instructions.item(i);

            switch (instructionElement.getAttribute(XML_ATTRIBUTE_INSTRUCTION_TYPE)) {

                case INSTRUCTION_TDUMT_UPDATE_DATABASE:
                    changesObjects.addAll(getChangesObjectsForContentsUpdate(instructionElement));
                    break;
                case INSTRUCTION_TDUMT_UPDATE_RESOURCE:
                    changesObjects.addAll(getChangesObjectsForResourceUpdate(instructionElement));
                    break;
                default:
            }
        }

        return DbPatchDto.builder()
                .addChanges(changesObjects)
                .build();
    }

    static Document initXmlDocumentFromResource(String resource) throws ParserConfigurationException, URISyntaxException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        String templateURI = thisClass.getResource(resource).toURI().toString();
        return docBuilder.parse(templateURI);
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

        Element instructionElement = patchDocument.createElement(XML_ELEMENT_INSTRUCTION);

        addAttribute(XML_ATTRIBUTE_INSTRUCTION_TYPE, instructionType, instructionElement, patchDocument);
        addAttribute("failOnError", "True", instructionElement, patchDocument);
        addAttribute("enabled", "True", instructionElement, patchDocument);

        addParameter(PARAMETER_TDUMT_RESOURCE_FILE_NAME, getTopicLabel(topic), instructionElement, patchDocument);
        addParameter(PARAMETER_TDUMT_RESOURCE_VALUES, resourceValues, instructionElement, patchDocument);

        return instructionElement;
    }

    private static void addAttribute(String name, String value, Element instructionElement, Document patchDocument) {
        Attr typeAttribute = patchDocument.createAttribute(name);
        typeAttribute.setValue(value);
        instructionElement.setAttributeNode(typeAttribute);
    }

    private static void addParameter(String name, String value, Element instructionElement, Document patchDocument) {
        Element resourceParameterElement = patchDocument.createElement(XML_ELEMENT_PARAMETER);
        resourceParameterElement.setAttribute(XML_ATTRIBUTE_NAME, name);
        resourceParameterElement.setAttribute(XML_ATTRIBUTE_VALUE, value);
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
        return topicLabel.substring(PREFIX_TOPIC_LABEL.length(), topicLabel.length());
    }

    private static DbDto.Topic getTopicFromLabel(String resourceFileName) {
        return DbDto.Topic.fromLabel(PREFIX_TOPIC_LABEL + resourceFileName);
    }

    private static Element findInstructionsElement(Document patchDocument) {
        return (Element) patchDocument.getElementsByTagName(XML_ELEMENT_INSTRUCTIONS).item(0);
    }

    private static List<DbPatchDto.DbChangeDto> getChangesObjectsForContentsUpdate(Element instructionElement) {

        String resourceFileName = null;
        String resourceValues = "";

        NodeList parameterElements = instructionElement.getElementsByTagName(XML_ELEMENT_PARAMETER);
        for (int i = 0 ; i < parameterElements.getLength() ; i++) {
            Element parameterElement = (Element) parameterElements.item(i);
            String value = parameterElement.getAttribute(XML_ATTRIBUTE_VALUE);

            switch(parameterElement.getAttribute(XML_ATTRIBUTE_NAME)) {
                case PARAMETER_TDUMT_RESOURCE_FILE_NAME:
                    resourceFileName = value;
                    break;
                case PARAMETER_TDUMT_RESOURCE_VALUES:
                    resourceValues = value;
                    break;
                default:
            }
        }

        DbDto.Topic topic = getTopicFromLabel(resourceFileName);

        return Stream.of(resourceValues.split("\\|\\|"))

                .map( (entry) -> {

                    // TODO handle complex references (first 2 columns)
                    String[] entryComponents = entry.split("\\|");
                    String reference = entryComponents[0];
                    List<String> values = asList(entryComponents[1].split("\\s"));

                    return DbPatchDto.DbChangeDto.builder()
                            .withType(UPDATE)
                            .forTopic(topic)
                            .asReference(reference)
                            .withEntryValues(values)
                            .build();
                })

                .collect(toList());
    }

    private static List<DbPatchDto.DbChangeDto> getChangesObjectsForResourceUpdate(Element instructionElement) {
        return new ArrayList<>();
    }
}
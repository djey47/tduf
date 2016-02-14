package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
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
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.DELETE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class allowing to convert database patches between TDUMT and TDUF systems.
 */
public class TdumtPatchConverter {

    private static Class<TdumtPatchConverter> thisClass = TdumtPatchConverter.class;

    private static final String XML_ELEMENT_INSTRUCTIONS = "instructions";
    private static final String XML_ELEMENT_INSTRUCTION = "instruction";
    private static final String XML_ELEMENT_PARAMETER = "parameter";
    private static final String XML_ATTRIBUTE_INSTRUCTION_TYPE = "type";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_ATTRIBUTE_VALUE = "value";

    private static final String INSTRUCTION_TDUMT_UPDATE_DATABASE = "updateDatabase";
    private static final String INSTRUCTION_TDUMT_UPDATE_RESOURCE = "updateResource";
    private static final String INSTRUCTION_TDUMT_REMOVE_ALL_LINES = "removeAllLinesFromDatabase";
    private static final String PARAMETER_TDUMT_RESOURCE_FILE_NAME = "resourceFileName";
    private static final String PARAMETER_TDUMT_RESOURCE_VALUES = "resourceValues";
    private static final String PARAMETER_TDUMT_DATABASE_IDENTIFIER = "databaseId";

    private static final String SEPARATOR_ENTRIES = "||";
    private static final String SEPARATOR_KEY_VALUE = "|";
    private static final String SEPARATOR_ITEMS = "\t";
    private static final String SEPARATOR_COMPOSITE_REF = "=";

    private static final String REGEX_SEPARATOR_ENTRIES = "\\|\\|";
    private static final String REGEX_SEPARATOR_KEY_VALUE = "\\|";
    private static final String REGEX_SEPARATOR_ITEMS = "\\s";
    private static final String REGEX_COMPOSITE_REF = "\\d+=\\d+";

    private static final String PREFIX_TOPIC_LABEL = "TDU_";

    /**
     * Converts a TDUF patch into TDUMT one (XML).
     * @param tdufDatabasePatch : TDUF patch object to convert
     * @return corresponding TDUMT patch as XML document.
     */
    public static Document jsonToPch(DbPatchDto tdufDatabasePatch) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        requireNonNull(tdufDatabasePatch, "A TDUF database patch object is required.");

        Document patchDocument = initXmlDocumentFromResource("/files/db/tdumt/patchTemplate.xml");

        Element instructionsElement = findXMLInstructionsElement(patchDocument);
        createUpdateElements(tdufDatabasePatch, UPDATE, patchDocument).forEach(instructionsElement::appendChild);
        createUpdateElements(tdufDatabasePatch, UPDATE_RES, patchDocument).forEach(instructionsElement::appendChild);

        return patchDocument;
    }

    /**
     * Converts a TDUMT (XML) patch into TDUF one.
     * @param tdumtDatabasePatch : TDUMT patch as XML document to convert
     * @return corresponding TDUF patch as DTO.
     */
    public static DbPatchDto pchToJson(Document tdumtDatabasePatch) {
        requireNonNull(tdumtDatabasePatch, "A TDUMT database patch document is required.");

        List<DbPatchDto.DbChangeDto> changesObjects = getChangesObjectsForUpdates(tdumtDatabasePatch);

        return DbPatchDto.builder()
                .addChanges(changesObjects)
                .build();
    }

    /**
     * @param potentialRef  : value of reference field if available
     * @param values        : list of all entry values
     * @return a generated key/values contents entry.
     */
    public static String getContentsValue(Optional<String> potentialRef, List<String> values) {

        String entryRef = potentialRef.orElse(values.get(0) + SEPARATOR_COMPOSITE_REF + values.get(1));

        return entryRef + SEPARATOR_KEY_VALUE + String.join(SEPARATOR_ITEMS, values);
    }

    static Document initXmlDocumentFromResource(String resource) throws ParserConfigurationException, URISyntaxException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        String templateURI = thisClass.getResource(resource).toURI().toString();
        return docBuilder.parse(templateURI);
    }

    private static List<Element> createUpdateElements(DbPatchDto tdufDatabasePatch, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, Document patchDocument) {

        return groupUpdateChangeObjectsByTopic(tdufDatabasePatch, changeType).entrySet().stream()

                .map((entry) -> createUpdateInstructionForTopicChanges(entry, changeType, patchDocument))

                .collect(toList());
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

    private static String getResourceValue(String ref, String value) {
        return ref + SEPARATOR_KEY_VALUE + value;
    }

    private static Element createUpdateInstruction(DbDto.Topic topic, DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, List<String> entries, Document patchDocument) {
        String instructionType = UPDATE == changeType ?
                INSTRUCTION_TDUMT_UPDATE_DATABASE :
                INSTRUCTION_TDUMT_UPDATE_RESOURCE;

        String resourceValues = String.join(SEPARATOR_ENTRIES, entries);

        Element instructionElement = patchDocument.createElement(XML_ELEMENT_INSTRUCTION);

        addXMLAttribute(XML_ATTRIBUTE_INSTRUCTION_TYPE, instructionType, instructionElement, patchDocument);
        addXMLAttribute("failOnError", "True", instructionElement, patchDocument);
        addXMLAttribute("enabled", "True", instructionElement, patchDocument);

        addXMLParameter(PARAMETER_TDUMT_RESOURCE_FILE_NAME, getTopicLabel(topic), instructionElement, patchDocument);
        addXMLParameter(PARAMETER_TDUMT_RESOURCE_VALUES, resourceValues, instructionElement, patchDocument);

        return instructionElement;
    }

    private static List<DbPatchDto.DbChangeDto> getChangesObjectsForUpdates(Document patchDocument) {
        // TODO return set instead of list
        List<DbPatchDto.DbChangeDto> changesObjects = new ArrayList<>();

        NodeList instructions = findXMLInstructionsElement(patchDocument).getElementsByTagName(XML_ELEMENT_INSTRUCTION);
        for (int i = 0 ; i < instructions.getLength() ; i++) {
            Element instructionElement = (Element) instructions.item(i);
            InstructionParametersParser parser = new InstructionParametersParser(instructionElement).invoke();

            switch (instructionElement.getAttribute(XML_ATTRIBUTE_INSTRUCTION_TYPE)) {
                case INSTRUCTION_TDUMT_UPDATE_DATABASE:
                    changesObjects.addAll(getChangesObjectsForContentsUpdates(parser));
                    break;
                case INSTRUCTION_TDUMT_UPDATE_RESOURCE:
                    changesObjects.addAll(getChangesObjectsForResourceUpdates(parser));
                    break;
                case INSTRUCTION_TDUMT_REMOVE_ALL_LINES:
                    changesObjects.add(getChangeObjectForContentsDeletion(parser));
                default:
            }
        }

        return changesObjects;
    }

    private static List<DbPatchDto.DbChangeDto> getChangesObjectsForContentsUpdates(InstructionParametersParser parser) {
        DbDto.Topic topic = parser.getResourceFileNameAsTopic();

        return Stream.of(parser.resourceValues.split(REGEX_SEPARATOR_ENTRIES))

                .map((contentsEntry) -> getChangeObjectForContentsUpdate(contentsEntry, topic))

                .collect(toList());
    }

    private static List<DbPatchDto.DbChangeDto> getChangesObjectsForResourceUpdates(InstructionParametersParser parser) {
        DbDto.Topic topic = parser.getResourceFileNameAsTopic();

        return Stream.of(parser.resourceValues.split(REGEX_SEPARATOR_ENTRIES))

                .map((entry) -> getChangeObjectForResourceUpdate(entry, topic))

                .collect(toList());
    }

    private static DbPatchDto.DbChangeDto getChangeObjectForContentsDeletion(InstructionParametersParser parser) {
        DbDto.Topic topic = parser.getResourceFileNameAsTopic();

        return DbPatchDto.DbChangeDto.builder()
                .forTopic(topic)
                .withType(DELETE)
                .filteredBy(Collections.singletonList(DbFieldValueDto.fromCouple(1, parser.databaseIdentifier)))
                .build();
    }

    private static DbPatchDto.DbChangeDto getChangeObjectForContentsUpdate(String contentsEntry, DbDto.Topic topic) {
        String[] entryComponents = contentsEntry.split(REGEX_SEPARATOR_KEY_VALUE);
        String reference = entryComponents[0];
        if (Pattern.matches(REGEX_COMPOSITE_REF, reference)) {
            reference = null;
        }
        List<String> values = asList(entryComponents[1].split(REGEX_SEPARATOR_ITEMS));

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(topic)
                .asReference(reference)
                .withEntryValues(values)
                .build();
    }

    private static DbPatchDto.DbChangeDto getChangeObjectForResourceUpdate(String resourceEntry, DbDto.Topic topic) {
        String[] entryComponents = resourceEntry.split(REGEX_SEPARATOR_KEY_VALUE);
        String reference = entryComponents[0];
        String value = entryComponents[1];

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(topic)
                .asReference(reference)
                .withValue(value)
                .build();
    }

    private static void addXMLAttribute(String name, String value, Element instructionElement, Document patchDocument) {
        Attr typeAttribute = patchDocument.createAttribute(name);
        typeAttribute.setValue(value);
        instructionElement.setAttributeNode(typeAttribute);
    }

    private static void addXMLParameter(String name, String value, Element instructionElement, Document patchDocument) {
        Element resourceParameterElement = patchDocument.createElement(XML_ELEMENT_PARAMETER);
        resourceParameterElement.setAttribute(XML_ATTRIBUTE_NAME, name);
        resourceParameterElement.setAttribute(XML_ATTRIBUTE_VALUE, value);
        instructionElement.appendChild(resourceParameterElement);
    }

    private static Element findXMLInstructionsElement(Document patchDocument) {
        return (Element) patchDocument.getElementsByTagName(XML_ELEMENT_INSTRUCTIONS).item(0);
    }

    private static String getTopicLabel(DbDto.Topic topic) {
        String topicLabel = topic.getLabel();
        return topicLabel.substring(PREFIX_TOPIC_LABEL.length(), topicLabel.length());
    }

    private static class InstructionParametersParser {
        private Element instructionElement;
        private String resourceFileName;
        private String resourceValues;
        private String databaseIdentifier;

        public InstructionParametersParser(Element instructionElement) {
            this.instructionElement = instructionElement;
        }

        public DbDto.Topic getResourceFileNameAsTopic() {
            return DbDto.Topic.fromLabel(PREFIX_TOPIC_LABEL + resourceFileName);
        }

        public InstructionParametersParser invoke() {
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
                    case PARAMETER_TDUMT_DATABASE_IDENTIFIER:
                        databaseIdentifier = value;
                        break;
                    default:
                }
            }
            return this;
        }
    }
}

package fr.tduf.libunlimited.low.files.research.rw;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import fr.tduf.libunlimited.low.files.research.common.helper.FormulaHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.domain.Entry;
import fr.tduf.libunlimited.low.files.research.domain.LinksContainer;
import fr.tduf.libunlimited.low.files.research.domain.Type;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper.*;
import static fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper.byteArrayToHexRepresentation;
import static fr.tduf.libunlimited.low.files.research.domain.DataStore.generateKeyPrefixForRepeatedField;
import static fr.tduf.libunlimited.low.files.research.domain.Type.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * Handles converting DataStore to JSON and vice-versa
 */
public class JsonAdapter {
    private static final String THIS_CLASS_NAME = JsonAdapter.class.getSimpleName();

    private static final String LINKS_FIELD_NAME = "#links#";
    private static final String LINK_SOURCE_KEY_FIELD_NAME = "sourceKey";
    private static final String LINK_TARGET_KEY_FIELD_NAME = "targetKey";
    private static final String META_FIELD_NAME = "#meta#";
    private static final String META_ACCESS_KEY_FIELD_NAME = "accessKeyPrefix";

    @SuppressWarnings("FieldMayBeFinal")
    private DataStore dataStore;

    /**
     * Creates a JSON Adapter
     * @param dataStore: DataStore instance to be converted from/to JSON
     */
    public JsonAdapter(DataStore dataStore) {
        this.dataStore = requireNonNull(dataStore, "A DataStore instance is required");
    }

    /**
     * @return a String representation of store contents, on JSON format.
     */
    public String toJsonString() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();

        readStructureFields(dataStore.getFileStructure().getFields(), rootNode, "");

        if (dataStore.getLinksContainer().hasLinks()) {
            followAndAddLinks(rootNode);
        }

        return rootNode.toString();
    }

    /**
     * Replaces current store contents with those in provided JSON String.
     *
     * @param jsonInput : json String containing all values
     */
    public void fromJsonString(String jsonInput) throws IOException {
        dataStore.clearAll();

        JsonNode rootNode = new ObjectMapper().readTree(jsonInput);

        readJsonNode(rootNode, "");
    }

    private void readStructureFields(List<FileStructureDto.Field> fields, ObjectNode currentObjectNode, String parentRepeaterKey) {

        for (FileStructureDto.Field field : fields) {

            String fieldName = field.getName();
            Type fieldType = field.getType();

            if (REPEATER == fieldType) {

                if (dataStore.repeaterHasSubItems(fieldName, parentRepeaterKey)) {
                    readRepeatedFields(field, currentObjectNode, parentRepeaterKey);
                }

            } else if (fieldType.isValueToBeStored()) {

                Optional<Entry> storeEntry = dataStore.fetchEntry(fieldName, parentRepeaterKey);
                if (!storeEntry.isPresent()) {
                    break;
                }

                readRegularField(field, currentObjectNode, storeEntry.get());
            }

        }
    }

    private void readRegularField(FileStructureDto.Field currentField, ObjectNode currentObjectNode, Entry storeEntry) {
        Type fieldType = currentField.getType();
        String fieldName = currentField.getName();
        byte[] rawValue = storeEntry.getRawValue();

        switch (fieldType) {
            case TEXT:
                currentObjectNode.put(fieldName, rawToText(rawValue, rawValue.length));
                break;
            case FPOINT:
                currentObjectNode.put(fieldName, rawToFloatingPoint(rawValue));
                break;
            case INTEGER:
                currentObjectNode.put(fieldName, rawToInteger(rawValue, storeEntry.isSigned(), storeEntry.getSize()));
                break;
            default:
                currentObjectNode.put(fieldName, byteArrayToHexRepresentation(rawValue));
                break;
        }
    }

    private void readRepeatedFields(FileStructureDto.Field repeaterField, ObjectNode objectNode, String parentRepeaterKey) {
        String repeaterFieldName = repeaterField.getName();
        ArrayNode repeaterNode = objectNode.arrayNode();
        objectNode.set(repeaterFieldName, repeaterNode);

        int parsedCount = 0;
        int repeatedCount = dataStore.getRepeatedValues(repeaterField.getName(), parentRepeaterKey).size();
        while (parsedCount < repeatedCount) {
            ObjectNode itemNode = objectNode.objectNode();

            String newRepeaterKeyPrefix = generateKeyPrefixForRepeatedField(repeaterFieldName, parsedCount, parentRepeaterKey);

            addRepeaterMetaNode(itemNode, newRepeaterKeyPrefix);

            readStructureFields(repeaterField.getSubFields(), itemNode, newRepeaterKeyPrefix);

            repeaterNode.add(itemNode);

            parsedCount++;
        }
    }

    private void addRepeaterMetaNode(ObjectNode itemNode, String repeaterKeyPrefix) {
        ObjectNode metaNode = itemNode.objectNode();

        metaNode.put(META_ACCESS_KEY_FIELD_NAME, repeaterKeyPrefix);

        itemNode.set(META_FIELD_NAME, metaNode);
    }

    private void followAndAddLinks(ObjectNode rootNode) {
        requireNonNull(rootNode, "A JSON root node must be provided");

        ArrayNode linksArrayNode = rootNode.arrayNode();
        LinksContainer linksContainer = dataStore.getLinksContainer();
        linksContainer.getSourcesSortedByAddress().parallelStream()
                .forEachOrdered(sourceEntry -> {
                    int address = sourceEntry.getKey();
                    String targetKey = linksContainer.getTargetFieldKeyWithAddress(address)
                            .orElseThrow(() -> new IllegalStateException(String.format("No target link found with address: %d", address)));
                    ObjectNode linkNode = rootNode.objectNode();
                    linkNode.put(LINK_SOURCE_KEY_FIELD_NAME, sourceEntry.getValue());
                    linkNode.put(LINK_TARGET_KEY_FIELD_NAME, targetKey);
                    linksArrayNode.add(linkNode);
                });

        rootNode.set(LINKS_FIELD_NAME, linksArrayNode);
    }

    private void readJsonObjectNode(JsonNode jsonNode, String parentKey) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> nextField = fields.next();

            String nextFieldKey = nextField.getKey();
            if (LINKS_FIELD_NAME.equals(nextFieldKey)) {
                readLinksNode((ArrayNode) nextField.getValue());
            } else if (!META_FIELD_NAME.equals(nextFieldKey)) {
                readJsonNode(nextField.getValue(), parentKey + nextFieldKey);
            }
        }
    }

    private void readJsonNode(JsonNode jsonNode, String parentKey) {

        Type type = Type.GAP;
        byte[] rawValue = new byte[0];
        boolean signed = false;
        Integer size = null;

        if (jsonNode instanceof ObjectNode) {

            readJsonObjectNode(jsonNode, parentKey);

        } else if (jsonNode instanceof ArrayNode) {

            readJsonArrayNode(jsonNode, parentKey);

        } else if (jsonNode instanceof DoubleNode) {

            type = FPOINT;
            rawValue = TypeHelper.floatingPoint32ToRaw(((Double) jsonNode.doubleValue()).floatValue());

        } else {

            FileStructureDto.Field fieldDefinition = StructureHelper.getFieldDefinitionFromFullName(parentKey, dataStore.getFileStructure())
                    .orElseThrow(() -> new IllegalStateException("Field definition not found for key: " + parentKey));
            if (jsonNode instanceof IntNode || jsonNode instanceof LongNode) {

                type = INTEGER;
                rawValue = TypeHelper.integerToRaw(jsonNode.longValue());
                signed = fieldDefinition.isSigned();
                size = computeValueLengthWithoutParentKey(fieldDefinition.getSizeFormula());

            } else if (jsonNode instanceof TextNode) {

                String stringValue = jsonNode.textValue();
                try {
                    type = UNKNOWN;
                    rawValue = TypeHelper.hexRepresentationToByteArray(stringValue);
                } catch (IllegalArgumentException iae) {
                    type = TEXT;
                    Optional<String> potentialParentKey = ofNullable(parentKey);
                    int length = potentialParentKey
                            .map(k -> computeValueLengthWithParentKey(fieldDefinition.getSizeFormula(), k))
                            .orElseGet(() -> computeValueLengthWithoutParentKey(fieldDefinition.getSizeFormula()));
                    rawValue = TypeHelper.textToRaw(stringValue, length);
                    Log.info(THIS_CLASS_NAME, "Unable to parse hex: '" + stringValue + "', will be considered as text");
                }
            }
        }

        if (type.isValueToBeStored()) {
            dataStore.putEntry(parentKey, type, signed, size, rawValue);
        }
    }

    private void readJsonArrayNode(JsonNode jsonNode, String parentKey) {
        int elementIndex = 0;
        Iterator<JsonNode> elements = jsonNode.elements();
        while (elements.hasNext()) {
            readJsonNode(elements.next(), generateKeyPrefixForRepeatedField(parentKey, elementIndex++));
        }
    }

    private void readLinksNode(ArrayNode linksArrayNode) {
        Iterator<JsonNode> elements = linksArrayNode.elements();
        while (elements.hasNext()) {
            JsonNode linkNode = elements.next();
            String sourceKey = linkNode.get(LINK_SOURCE_KEY_FIELD_NAME).asText();
            String targetKey = linkNode.get(LINK_TARGET_KEY_FIELD_NAME).asText();
            int targetAddress = dataStore.getInteger(sourceKey)
                    .orElseThrow(() -> new IllegalStateException(String.format("No target address found for link source: %s", sourceKey)))
                    .intValue();
            LinksContainer linksContainer = dataStore.getLinksContainer();
            linksContainer.registerSource(sourceKey, targetAddress);
            linksContainer.registerTarget(targetKey, targetAddress);
        }
    }

    private int computeValueLengthWithoutParentKey(String sizeFormula) {
        return FormulaHelper.resolveToInteger(sizeFormula, null, dataStore);
    }

    private int computeValueLengthWithParentKey(String sizeFormula, String parentKey) {
        return FormulaHelper.resolveToInteger(sizeFormula, parentKey, dataStore);
    }
}

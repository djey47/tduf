package fr.tduf.libunlimited.low.files.research.domain;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.research.common.helper.FormulaHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;
import static fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper.*;
import static fr.tduf.libunlimited.low.files.research.domain.Type.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Place to store and extract data with {@link fr.tduf.libunlimited.low.files.research.rw.GenericParser}
 * and {@link fr.tduf.libunlimited.low.files.research.rw.GenericWriter}
 */
public class DataStore {
    private static final String THIS_CLASS_NAME = DataStore.class.getSimpleName();

    private static final String REPEATER_FIELD_SEPARATOR = ".";

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^(?:.*\\.)?(.+)$");              // e.g 'entry_list[1].my_field', 'my_field'

    private static final Pattern SUB_FIELD_NAME_PATTERN = Pattern.compile("^(.+)\\[(\\d+)]\\.(.+)$"); // e.g 'entry_list[1].my_field'
    private static final String SUB_FIELD_PREFIX_FORMAT = "%s[%d]" + REPEATER_FIELD_SEPARATOR;

    private final Map<String, Entry> store = new HashMap<>();

    private final FileStructureDto fileStructure;
    private final int repeatIndex;

    /**
     * Creates a datastore.
     *
     * @param fileStructure : structure of stored file contents.
     */
    public DataStore(FileStructureDto fileStructure) {
        this(fileStructure, -1);
    }

    /**
     * Creates a sub datastore for repeated values.
     *
     * @param fileStructure : structure of stored file contents
     * @param repeatIndex : 0-based index for repeated values in it.
     */
    public DataStore(FileStructureDto fileStructure, int repeatIndex) {
        requireNonNull(fileStructure, "File structure must be provided.");

        this.fileStructure = fileStructure;
        this.repeatIndex = repeatIndex;
    }

    /**
     * Remove all entries from current store.
     */
    public void clearAll() {
        this.store.clear();
    }

    /**
     * Adds all entries from provided data store to current one.
     * If entries with the same key already exist, values will be replaced.
     * @param sourceStore   : pre-filled data store.
     */
    public void mergeAll(DataStore sourceStore) {
        if (sourceStore == null) {
            return;
        }

        if(!this.getFileStructure().equals(sourceStore.getFileStructure())) {
            throw new IllegalArgumentException("File structure differ between data stores to be merged.");
        }

        this.getStore().putAll(sourceStore.store);
    }

    /**
     * Adds provided bytes to the store, if type is stor-able. Length is automatically determined by raw value length.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param type      : value type
     * @param rawValue  : value to store
     */
    public void addValue(String fieldName, Type type, byte[] rawValue) {
        addValue(fieldName, type, false, rawValue.length, rawValue);
    }

    /**
     * Adds provided bytes to the store, if type is stor-able.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param type      : value type
     * @param length    : value length
     * @param rawValue  : value to store
     */
    public void addValue(String fieldName, Type type, int length, byte[] rawValue) {
        addValue(fieldName, type, false, length, rawValue);
    }

    /**
     * Adds provided bytes to the store, if type is stor-able.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param type      : value type
     * @param signed    : indicates if value is signed or not (only applicable to integer data type)
     * @param length    : size of value, in bytes (can be null)
     * @param rawValue  : value to store
     */
    public void addValue(String fieldName, Type type, boolean signed, Integer length, byte[] rawValue) {
        if (!type.isValueToBeStored()) {
            return;
        }

        putEntry(fieldName, type, signed, length, rawValue);
    }

    /**
     * Adds a String value to the store.
     *
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addText(String fieldName, String value) {
        addValue(fieldName, TEXT, TypeHelper.textToRaw(value, value.length()));
    }

    /**
     * Adds an 4byte Integer value to the store.
     *
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addInteger(String fieldName, long value) {
        addValue(fieldName, INTEGER, 4, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds an Integer value to the store.
     *
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     * @param length    : length of this numeric value (1, 2, 4 or 8 bytes)
     */
    public void addInteger(String fieldName, long value, int length) {
        addValue(fieldName, INTEGER, length, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds a Floating Point value (32 bit) to the store.
     *
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addFloatingPoint(String fieldName, float value) {
        addValue(fieldName, FPOINT, 4, TypeHelper.floatingPoint32ToRaw(value));
    }

    /**
     * Adds a Floating Point value (16 bit) to the store.
     *
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addHalfFloatingPoint(String fieldName, float value) {
        addValue(fieldName, FPOINT, 2, TypeHelper.floatingPoint16ToRaw(value));
    }

    /**
     * Adds a repeated field to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param valueBytes        : value to store
     */
    public void addRepeatedRawValue(String repeaterFieldName, String fieldName, long index, byte[] valueBytes) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, UNKNOWN, valueBytes);
    }

    /**
     * Adds a repeated field to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedTextValue(String repeaterFieldName, String fieldName, long index, String value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, TEXT, TypeHelper.textToRaw(value, value.length()));
    }

    /**
     * Adds a repeated field (32bit unsigned numeric) to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedIntegerValue(String repeaterFieldName, String fieldName, long index, long value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, INTEGER, 4, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds a repeated field (8bit to 64bit signed or unsigned numeric) to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     * @param length            : length of this numeric value (1, 2, 4 or 8 bytes)
     */
    public void addRepeatedIntegerValue(String repeaterFieldName, String fieldName, long index, long value, int length) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, INTEGER, length, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds a repeated field to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedFloatingPointValue(String repeaterFieldName, String fieldName, int index, float value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, FPOINT, 4, TypeHelper.floatingPoint32ToRaw(value));
    }

    /**
     * Integrates a sub data store (produced by {@link #getRepeatedValues} method to current store, under a repater field, at a given index.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param index             : rank in repeater
     * @param subStore          : sub data store to merge into existing one
     */
    public void mergeRepeatedValues(String repeaterFieldName, int index, DataStore subStore) {
        requireNonNull(subStore, "A sub data store is required.").getStore().entrySet().forEach(entry -> {
            String newKey = generateKeyForRepeatedField(repeaterFieldName, entry.getKey(), index);
            putEntryFromExisting(newKey, entry.getValue());
        });
    }

    /**
     * @return entry count in store.
     */
    public int size() {
        return this.store.size();
    }

    /**
     * Returns all bytes from the store.
     *
     * @param fieldName : identifier of field hosting the value
     * @return the stored raw value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<byte[]> getRawValue(String fieldName) {
        Entry entry = this.store.get(fieldName);

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(entry.getRawValue());
    }

    /**
     * Returns a String value from the store.
     *
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<String> getText(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return Optional.empty();
        }

        Entry entry = this.store.get(fieldName);
        assertSimpleCondition(() -> TEXT == entry.getType());

        byte[] rawValue = entry.getRawValue();
        return of(
                rawToText(rawValue, rawValue.length));
    }

    /**
     * Returns a long value from the store.
     *
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<Long> getInteger(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return Optional.empty();
        }

        Entry entry = this.store.get(fieldName);
        assertSimpleCondition(() -> INTEGER == entry.getType());

        return of(
                rawToInteger(entry.getRawValue(), entry.isSigned(), entry.getSize()));
    }

    /**
     * Returns a 16-bit/32-bit floating point value from the store.
     *
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<Float> getFloatingPoint(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return Optional.empty();
        }

        Entry entry = this.store.get(fieldName);
        Type entryType = entry.getType();
        assertSimpleCondition(() -> FPOINT == entryType, "Wrong entry type: " + entryType + ", expected: " + FPOINT);

        return of(
                rawToFloatingPoint(entry.getRawValue()));
    }

    /**
     * Returns a list of numeric integer values from the store.
     *
     * @param fieldName : name of field to search
     * @return all stored values whose key match provided identifier
     */
    public List<Long> getIntegerListOf(String fieldName) {

        return store.keySet().stream()
                .filter(key -> {
                    Matcher matcher = FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() && matcher.group(1).equals(fieldName);
                })
                .map(this.store::get)
                .map(storeEntry -> TypeHelper.rawToInteger(storeEntry.getRawValue(), storeEntry.isSigned(), storeEntry.getSize()))
                .collect(toList());
    }

    /**
     * Returns a list of numeric floating point values from the store.
     *
     * @param fieldName : name of field to search
     * @return all stored values whose key match provided identifier
     */
    public List<Float> getFloatingPointListOf(String fieldName) {
        return store.keySet().stream()
                .filter(key -> {
                    Matcher matcher = FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() && matcher.group(1).equals(fieldName);
                })
                .map(key -> this.store.get(key).getRawValue())
                .map(TypeHelper::rawToFloatingPoint)
                .collect(toList());
    }

    /**
     * Returns sub-DataStores of items contained by a repeater field.
     *
     * @param repeaterFieldName : name of repeater field
     */
    public List<DataStore> getRepeatedValues(String repeaterFieldName) {
        Map<Integer, List<String>> groupedKeysByIndex = store.keySet().stream()
                .filter(key -> key.startsWith(repeaterFieldName))
                .collect(Collectors.groupingBy(key -> {
                    Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() ? Integer.valueOf(matcher.group(2)) : 0; // extracts index part
                }));

        List<DataStore> repeatedValues = createEmptyList(groupedKeysByIndex.size(), this.getFileStructure());

        for (Map.Entry<Integer, List<String>> entry: groupedKeysByIndex.entrySet()) {
            DataStore subDataStore = repeatedValues.get(entry.getKey());

            for (String key : entry.getValue()) {
                Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                if (matcher.matches()) {
                    subDataStore.getStore().put(matcher.group(3), store.get(key)); // extracts field name part
                }
            }
        }

        return repeatedValues;
    }

    /**
     * @return a String representation of store contents, on JSON format.
     */
    public String toJsonString() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();

        readStructureFields(getFileStructure().getFields(), rootNode, "");

        return rootNode.toString();
    }

    /**
     * Replaces current store contents with those in provided JSON String.
     *
     * @param jsonInput : json String containing all values
     */
    public void fromJsonString(String jsonInput) throws IOException {
        this.getStore().clear();

        JsonNode rootNode = new ObjectMapper().readTree(jsonInput);

        readJsonNode(rootNode, "");
    }

    /**
     * @return a full copy of data store instance.
     */
    public DataStore copy() {
        DataStore clone = new DataStore(fileStructure, repeatIndex);
        clone.getStore().putAll(copyAllEntries(store));
        return clone;
    }

    /**
     * Returns key prefix for repeated (under repeater) field.
     *
     * @param repeaterFieldName : name of parent, repeater field
     * @param index             : item rank in repeater
     * @return a prefix allowing to parse sub-fields.
     */
    public static String generateKeyPrefixForRepeatedField(String repeaterFieldName, long index) {
        return String.format(SUB_FIELD_PREFIX_FORMAT, repeaterFieldName, index);
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
            rawValue = TypeHelper.floatingPoint32ToRaw(((Double) jsonNode.getDoubleValue()).floatValue());

        } else {

            FileStructureDto.Field fieldDefinition = StructureHelper.getFieldDefinitionFromFullName(parentKey, fileStructure)
                    .orElseThrow(() -> new IllegalStateException("Field definition not found for key: " + parentKey));
            if (jsonNode instanceof IntNode || jsonNode instanceof LongNode) {

                type = INTEGER;
                rawValue = TypeHelper.integerToRaw(jsonNode.getLongValue());
                signed = fieldDefinition.isSigned();
                size = computeValueLengthWithoutParentKey(fieldDefinition.getSizeFormula());

            } else if (jsonNode instanceof TextNode) {

                String stringValue = jsonNode.getTextValue();
                try {
                    type = UNKNOWN;
                    rawValue = TypeHelper.hexRepresentationToByteArray(stringValue);
                } catch (IllegalArgumentException iae) {
                    type = TEXT;
                    Optional<String> potentialParentKey = ofNullable(parentKey);
                    int length = potentialParentKey.isPresent() ?
                            computeValueLengthWithParentKey(fieldDefinition.getSizeFormula(), potentialParentKey.get()) :
                            computeValueLengthWithoutParentKey(fieldDefinition.getSizeFormula());
                    rawValue = TypeHelper.textToRaw(stringValue, length);
                    Log.info(THIS_CLASS_NAME, "Unable to parse hex: '" + stringValue + "', will be considered as text");
                }
            }
        }

        if (type.isValueToBeStored()) {
            putEntry(parentKey, type, signed, size, rawValue);
        }
    }

    private int computeValueLengthWithoutParentKey(String sizeFormula) {
        return FormulaHelper.resolveToInteger(sizeFormula, empty(), this);
    }

    private int computeValueLengthWithParentKey(String sizeFormula, String parentKey) {
        return FormulaHelper.resolveToInteger(sizeFormula, of(parentKey), this);
    }

    private void readJsonArrayNode(JsonNode jsonNode, String parentKey) {
        int elementIndex = 0;
        Iterator<JsonNode> elements = jsonNode.getElements();
        while (elements.hasNext()) {
            readJsonNode(elements.next(), generateKeyPrefixForRepeatedField(parentKey, elementIndex++));
        }
    }

    private void readJsonObjectNode(JsonNode jsonNode, String parentKey) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> nextField = fields.next();
            readJsonNode(nextField.getValue(), parentKey + nextField.getKey());
        }
    }

    private boolean readStructureFields(List<FileStructureDto.Field> fields, ObjectNode currentObjectNode, String repeaterKey) {

        for (FileStructureDto.Field field : fields) {

            String fieldName = field.getName();
            Type fieldType = field.getType();

            if (REPEATER == fieldType) {

                readRepeatedFields(field.getSubFields(), currentObjectNode, fieldName);

            } else if (fieldType.isValueToBeStored()) {

                Entry storeEntry = this.getStore().get(repeaterKey + fieldName);
                if (storeEntry == null) {
                    return false;
                }

                readRegularField(field, currentObjectNode, storeEntry);
            }
        }

        return true;
    }

    private void readRepeatedFields(List<FileStructureDto.Field> repeatedFields, ObjectNode objectNode, String repeaterFieldName) {
        ArrayNode repeaterNode = objectNode.arrayNode();
        objectNode.put(repeaterFieldName, repeaterNode);

        int parsedCount = 0;
        boolean hasMoreItems = true;
        while (hasMoreItems) {
            ObjectNode itemNode = objectNode.objectNode();

            hasMoreItems = readStructureFields(repeatedFields, itemNode, DataStore.generateKeyPrefixForRepeatedField(repeaterFieldName, parsedCount++));

            if (hasMoreItems) {
                repeaterNode.add(itemNode);
            }
        }
    }

    private void putEntry(String key, Type type, boolean signed, Integer size, byte[] rawValue) {
        Entry entry = new Entry(type, signed, size, rawValue);
        this.getStore().put(key, entry);
    }

    private void putEntryFromExisting(String key, Entry existingEntry) {
        Entry entry = new Entry(existingEntry.getType(), existingEntry.isSigned(), existingEntry.getSize(), existingEntry.getRawValue());
        this.getStore().put(key, entry);
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

    private static String generateKeyForRepeatedField(String repeaterFieldName, String repeatedFieldName, long index) {
        String keyPrefix = generateKeyPrefixForRepeatedField(repeaterFieldName, index);
        return keyPrefix + repeatedFieldName;
    }

    private static List<DataStore> createEmptyList(int size, FileStructureDto fileStructure) {
        List<DataStore> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(new DataStore(fileStructure, i));
        }

        return list;
    }

    private static Map<String, Entry> copyAllEntries(Map<String, Entry> store) {

        Map<String, Entry> storeCopy = new HashMap<>();

        store.forEach( (key, entry) -> storeCopy.put(key, entry.copy()));

        return storeCopy;
    }

    Map<String, Entry> getStore() {
        return store;
    }

    public FileStructureDto getFileStructure() {
        return fileStructure;
    }

    public int getRepeatIndex() {
        return repeatIndex;
    }
}

package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper.*;
import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.*;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Place to store and extract data with {@link fr.tduf.libunlimited.low.files.research.rw.GenericParser}
 * and {@link fr.tduf.libunlimited.low.files.research.rw.GenericWriter}
 */
public class DataStore {
    private static final String REPEATER_FIELD_SEPARATOR = ".";

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^(?:.*\\.)?(.+)$");              // e.g 'entry_list[1].my_field', 'my_field'

    private static final Pattern SUB_FIELD_NAME_PATTERN = Pattern.compile("^(.+)\\[(\\d+)\\]\\.(.+)$"); // e.g 'entry_list[1].my_field'
    private static final String SUB_FIELD_PREFIX_FORMAT = "%s[%d]" + REPEATER_FIELD_SEPARATOR;

    private final Map<String, Entry> store = new HashMap<>();

    /**
     * Remove all entries from current store.
     */
    public void clearAll() {
        this.store.clear();
    }

    /**
     * Adds provided bytes to the store, if type is stor-able.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param type      : value type
     * @param rawValue  : value to store
     */
    public void addValue(String fieldName, FileStructureDto.Type type, byte[] rawValue) {
        if (!type.isValueToBeStored()) {
            return;
        }

        putEntry(fieldName, type, rawValue);
    }

    /**
     * Adds a String value to the store.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addText(String fieldName, String value) {
        addValue(fieldName, TEXT, TypeHelper.textToRaw(value));
    }

    /**
     * Adds an Integer value to the store.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addInteger(String fieldName, long value) {
        addValue(fieldName, INTEGER, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds a Floating Point value (32 bit) to the store.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addFloatingPoint(String fieldName, float value) {
        addValue(fieldName, FPOINT, TypeHelper.floatingPoint32ToRaw(value));
    }

    /**
     * Adds a Floating Point value (16 bit) to the store.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addHalfFloatingPoint(String fieldName, float value) {
        addValue(fieldName, FPOINT, TypeHelper.floatingPoint16ToRaw(value));
    }

    /**
     * Adds a repeated field to the store.
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
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedTextValue(String repeaterFieldName, String fieldName, long index, String value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, TEXT, TypeHelper.textToRaw(value));
    }

    /**
     * Adds a repeated field to the store.
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedIntegerValue(String repeaterFieldName, String fieldName, long index, long value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, INTEGER, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds a repeated field to the store.
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedFloatingPointValue(String repeaterFieldName, String fieldName, int index, float value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, FPOINT, TypeHelper.floatingPoint32ToRaw(value));
    }

    /**
     * @return entry count in store.
     */
    public int size() {
        return this.store.size();
    }

    /**
     * Returns all bytes from the store.
     * @param fieldName : identifier of field hosting the value
     * @return the stored raw value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<byte[]> getRawValue(String fieldName) {
        Entry entry = this.store.get(fieldName);

        if(entry == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(entry.rawValue);
    }

    /**
     * Returns a String value from the store.
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<String> getText(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return Optional.empty();
        }

        Entry entry = this.store.get(fieldName);
        assert (entry.getType() == FileStructureDto.Type.TEXT);

        return Optional.of(
                rawToText(entry.rawValue));
    }

    /**
     * Returns a long value from the store.
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<Long> getInteger(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return Optional.empty();
        }

        Entry entry = this.store.get(fieldName);
        assert (entry.getType() == FileStructureDto.Type.INTEGER);

        return Optional.of(
                rawToInteger(entry.rawValue));
    }

    /**
     * Returns a 16-bit/32-bit floating point value from the store.
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<Float> getFloatingPoint(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return Optional.empty();
        }

        Entry entry = this.store.get(fieldName);
        assert (entry.getType() == FileStructureDto.Type.FPOINT);

        return Optional.of(
                rawToFloatingPoint(entry.rawValue));
    }

    /**
     * Returns a list of numeric integer values from the store.
     * @param fieldName : name of field to search
     * @return all stored values whose key match provided identifier
     */
    public List<Long> getIntegerListOf(String fieldName) {

        return store.keySet().stream()

                .filter(key -> {
                    Matcher matcher = FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() && matcher.group(1).equals(fieldName);
                })

                .map(key -> this.store.get(key).rawValue)

                .map(TypeHelper::rawToInteger)

                .collect(Collectors.toList());
    }

    /**
     * Returns a list of numeric floating point values from the store.
     * @param fieldName : name of field to search
     * @return all stored values whose key match provided identifier
     */
    public List<Float> getFloatingPointListOf(String fieldName) {
        return store.keySet().stream()

                .filter(key -> {
                    Matcher matcher = FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() && matcher.group(1).equals(fieldName);
                })

                .map(key -> this.store.get(key).rawValue)

                .map(TypeHelper::rawToFloatingPoint)

                .collect(Collectors.toList());
    }

    /**
     * Returns sub-DataStores of items contained by a repeater field.
     * @param repeaterFieldName   : name of repeater field
     */
    public List<DataStore> getRepeatedValues(String repeaterFieldName) {
        Map<Integer, List<String>> groupedKeysByIndex = store.keySet().stream()

                .filter(key -> key.startsWith(repeaterFieldName))

                .collect(Collectors.groupingBy(key -> {
                    Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() ? Integer.valueOf(matcher.group(2)) : 0; // extracts index part
                }));

        List<DataStore> repeatedValues = createEmptyList(groupedKeysByIndex.size());

        for(Integer index : groupedKeysByIndex.keySet()) {

            DataStore subDataStore = repeatedValues.get(index);

            for (String key : groupedKeysByIndex.get(index)) {
                Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                if (matcher.matches()) {
                    subDataStore.getStore().put(matcher.group(3), store.get(key)); // extracts field name part
                }
            }
        }

        return repeatedValues;
    }

    /**
     * @return a String representation of store contents, on JSON format. Entries are ordered by rank.
     */
    public String toJsonString() {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

        this.getStore().entrySet().stream()

                .sorted((mapEntry1, mapEntry2) -> mapEntry1.getValue().rank - mapEntry2.getValue().rank)

                .forEach((mapEntry) -> {
                    String key = mapEntry.getKey();
                    Entry storeEntry = mapEntry.getValue();
                    switch (storeEntry.type) {
                        case TEXT:
                            objectNode.put(key, rawToText(storeEntry.rawValue));
                            break;
                        case FPOINT:
                            objectNode.put(key, rawToFloatingPoint(storeEntry.getRawValue()));
                            break;
                        case INTEGER:
                            objectNode.put(key, rawToInteger(storeEntry.rawValue));
                            break;
                        default:
                            objectNode.put(key, byteArrayToHexRepresentation(storeEntry.rawValue));
                            break;
                    }
                });

        return objectNode.toString();
    }

    /**
     * Replaces current store contents with those in provided JSON String.
     * @param jsonInput : json String containing all values
     */
    public void fromJsonString(String jsonInput) throws IOException {
        ObjectReader reader = new ObjectMapper().reader(Map.class);

        Map<String, Object> intermediateMap = reader.readValue(jsonInput);

        this.getStore().clear();
        intermediateMap.forEach((key, value) -> {

            FileStructureDto.Type type = UNKNOWN;
            byte[] rawValue = new byte[0];

            if (value.getClass() == Double.class) {
                type = FileStructureDto.Type.FPOINT;
                Double doubleValue = (Double) value;
                rawValue = TypeHelper.floatingPoint32ToRaw(doubleValue.floatValue());
            } else if (value.getClass() == Integer.class) {
                type = FileStructureDto.Type.INTEGER;
                rawValue = TypeHelper.integerToRaw((Integer) value);
            } else if (value.getClass() == String.class) {
                String stringValue = (String) value;
                try {
                   rawValue = TypeHelper.hexRepresentationToByteArray(stringValue);
                } catch (IllegalArgumentException e) {
                    type = FileStructureDto.Type.TEXT;
                    rawValue = TypeHelper.textToRaw(stringValue);
                }
            }

            putEntry(key, type, rawValue);
        });
    }

    /**
     * Returns key prefix for repeated (under repeater) field.
     * @param repeaterFieldName : name of parent, repeater field
     * @param index             : item rank in repeater
     * @return a prefix allowing to parse sub-fields.
     */
    public static String generateKeyPrefixForRepeatedField(String repeaterFieldName, long index) {
        return String.format(SUB_FIELD_PREFIX_FORMAT, repeaterFieldName, index);
    }

    private static String generateKeyForRepeatedField(String repeaterFieldName, String repeatedFieldName, long index) {
        String keyPrefix = generateKeyPrefixForRepeatedField(repeaterFieldName, index);
        return keyPrefix + repeatedFieldName;
    }

    private static List<DataStore> createEmptyList(int size) {
        List<DataStore> list = new ArrayList<>(size);

        for (int i = 0 ; i < size ; i++) {
            list.add(new DataStore());
        }

        return list;
    }

    private void putEntry(String key, FileStructureDto.Type type, byte[] rawValue) {
        Entry entry = new Entry(key, type, rawValue, this.store.size());
        this.getStore().put(key, entry);
    }

    Map<String, Entry> getStore() {
        return store;
    }

    /**
     * Represents a store entry to bring more information.
     */
    static class Entry {
        private final String key;
        private final FileStructureDto.Type type;
        private final byte[] rawValue;
        private final int rank;

        Entry(String key, FileStructureDto.Type type, byte[] rawValue, int rank) {
            this.key = key;
            this.type = type;
            this.rawValue = rawValue;
            this.rank = rank;
        }

        Entry(String key, FileStructureDto.Type type, byte[] rawValue) {
            this(key, type, rawValue, 0);
        }

        byte[] getRawValue() {
            return rawValue;
        }

        FileStructureDto.Type getType() {
            return type;
        }

        int getRank() {
            return rank;
        }

        public String getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }
    }
}
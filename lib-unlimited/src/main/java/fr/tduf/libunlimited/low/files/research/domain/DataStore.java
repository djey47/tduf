package fr.tduf.libunlimited.low.files.research.domain;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Place to store and extract data with {@link fr.tduf.libunlimited.low.files.research.parser.GenericParser}
 * and {@link fr.tduf.libunlimited.low.files.research.writer.GenericWriter}
 */
public class DataStore {

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^(?:.*\\.)?(.+)$");              // e.g 'entry_list[1].my_field', 'my_field'

    private static final Pattern SUB_FIELD_NAME_PATTERN = Pattern.compile("^(.+)\\[(\\d+)\\]\\.(.+)$"); // e.g 'entry_list[1].my_field'
    private static final String SUB_FIELD_FORMAT = "%s[%d].%s";

    private final Map<String, byte[]> store = new HashMap<>();

    /**
     * Remove all entries from current store.
     */
    public void clearAll() {
        this.store.clear();
    }

    /**
     * Adds all bytes to the store.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param rawValue  : value to store
     */
    public void addRawValue(String fieldName, byte[] rawValue) {
        this.store.put(fieldName, rawValue);
    }

    /**
     * Adds a String value to the store.
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addString(String fieldName, String value) {
        this.store.put(fieldName, value.getBytes());
    }

    /**
     * Adds a repeated field to the store.
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to storel
     */
    public void addRepeatedStringValue(String repeaterFieldName, String fieldName, int index, String value) {
        String key = String.format(SUB_FIELD_FORMAT, repeaterFieldName, index, fieldName);
        this.store.put(key, value.getBytes());
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
     * @return the stored raw value whose key match provided identifier, or null if it does not exist
     */
    public byte[] getRawValue(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return null;
        }
        return this.store.get(fieldName);
    }

    /**
     * Returns a String value from the store.
     * @param fieldName :   name of field to search
     * @return the stored value whose key match provided identifier, or null if it does not exist
     */
    public String getString(String fieldName) {
        if (!this.store.containsKey(fieldName)) {
            return null;
        }
        return new String(this.store.get(fieldName));
    }

    /**
     * Returns a list of numeric values from the store.
     * @param fieldName : name of field to search
     * @return all stored values whose key match provided identifier
     */
    public List<Long> getNumericListOf(String fieldName) {

        return store.keySet().stream()

                .filter(key -> {
                    Matcher matcher = FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() && matcher.group(1).equals(fieldName);
                })

                .map( store::get)

                .map( bytes -> ByteBuffer.wrap(bytes).getLong() ) // TODO handle other than 4 bytes

                .collect( Collectors.toList() );
    }

    /**
     * Returns a list of name-value pairs contained by a repeater field.
     * @param repeaterFieldName   : name of repeater field
     */
    // TODO return DataStore !!
    public List<Map<String, byte[]>> getRepeatedValuesOf(String repeaterFieldName) {

        Map<Integer, List<String>> groupedKeysByIndex = store.keySet().stream()

                .filter(key -> key.startsWith(repeaterFieldName))

                .collect(Collectors.groupingBy(key -> {
                    Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() ? Integer.valueOf(matcher.group(2)) : 0; // extracts index part
                }));

        List<Map<String, byte[]>> repeatedValues = createEmptyList(groupedKeysByIndex.size());

        for(Integer index : groupedKeysByIndex.keySet()) {

            Map<String, byte[]> valuesMap = repeatedValues.get(index);

            for (String key : groupedKeysByIndex.get(index)) {
                Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                if (matcher.matches()) {
                    valuesMap.put(matcher.group(3), store.get(key));    // extracts field name part
                }
            }
        }

        return repeatedValues;
    }

    private static List<Map<String, byte[]>> createEmptyList(int size) {
        List<Map<String, byte[]>> list = new ArrayList<>(size);

        for (int i = 0 ; i < size ; i++) {
            list.add(new HashMap<>());
        }

        return list;
    }

    Map<String, byte[]> getStore() {
        return store;
    }
}
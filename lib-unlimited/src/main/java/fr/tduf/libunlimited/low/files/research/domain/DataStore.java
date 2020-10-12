package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;
import static fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper.*;
import static fr.tduf.libunlimited.low.files.research.domain.Type.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Place to store and extract data with {@link fr.tduf.libunlimited.low.files.research.rw.GenericParser}
 * and {@link fr.tduf.libunlimited.low.files.research.rw.GenericWriter}
 */
public class DataStore {
    private static final String REPEATER_FIELD_SEPARATOR = ".";

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^(?:.*\\.)?(.+)$");              // e.g 'entry_list[1].my_field', 'my_field'

    private static final String REST_STORE_KEY = "#rest#";
    private static final String SUB_FIELD_PREFIX_FORMAT = "%s[%d]" + REPEATER_FIELD_SEPARATOR;
    private static final String SUB_FIELD_WITH_PARENT_KEY_PREFIX_FORMAT = "%s" + SUB_FIELD_PREFIX_FORMAT;

    private final Map<String, Entry> store = new HashMap<>();

    private final FileStructureDto fileStructure;
    private final int repeatIndex;

    private final LinksContainer linksContainer = new LinksContainer();

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
        this.getLinksContainer().clear();
    }

    /**
     * Adds generic entry to the store
     * @param key       : key under which value must be stored
     * @param type      : value type
     * @param signed    : true indicated numeric value must be signed
     * @param size      : optional, indicates length or size of the value. By default, size of rawValue arry is used.
     * @param rawValue  : value to store
     */
    public void putEntry(String key, Type type, boolean signed, Integer size, byte[] rawValue) {
        Entry entry = new Entry(type, signed, size == null ? rawValue.length : size, rawValue);
        this.getStore().put(key, entry);
    }

    /**
     * @return generic entry, or empty if it does not exist
     */
    public Optional<Entry> fetchEntry(String fieldName, String parentRepeaterKey) {
        return ofNullable(getStore().get(parentRepeaterKey + fieldName));
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
     * Adds provided bytes to the store, as remaining value.
     * @param rawValue  : value to store
     */
    public void addRemainingValue(byte[] rawValue) {
        putEntry(REST_STORE_KEY, UNKNOWN, false, rawValue.length, rawValue);
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
     * Adds an 32bit Integer value to the store.
     *
     * @param fieldName : identifier of field hosting the value, should not exist already
     * @param value     : value to store
     */
    public void addInteger32(String fieldName, long value) {
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
    public void addRepeatedValue(String repeaterFieldName, String fieldName, long index, byte[] valueBytes) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, UNKNOWN, valueBytes);
    }

    /**
     * Adds a repeated field to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param valueBytes        : value to store
     */
    public void addRepeatedValue(String repeaterFieldName, String fieldName, Type fieldType, long index, byte[] valueBytes) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, fieldType, valueBytes);
    }

    /**
     * Adds a repeated field to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedText(String repeaterFieldName, String fieldName, long index, String value) {
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
    public void addRepeatedInteger32(String repeaterFieldName, String fieldName, long index, long value) {
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
    public void addRepeatedInteger(String repeaterFieldName, String fieldName, long index, long value, int length) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, INTEGER, length, TypeHelper.integerToRaw(value));
    }

    /**
     * Adds a repeated field (32bit) to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedFloatingPoint(String repeaterFieldName, String fieldName, int index, float value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, FPOINT, 4, TypeHelper.floatingPoint32ToRaw(value));
    }

    /**
     * Adds a repeated field (16bit) to the store.
     *
     * @param repeaterFieldName : identifier of repeater field
     * @param fieldName         : identifier of field hosting the value
     * @param index             : rank in repeater
     * @param value             : value to store
     */
    public void addRepeatedHalfFloatingPoint(String repeaterFieldName, String fieldName, int index, float value) {
        String key = generateKeyForRepeatedField(repeaterFieldName, fieldName, index);
        addValue(key, FPOINT, 2, TypeHelper.floatingPoint16ToRaw(value));
    }

    /**
     * @return entry count in store.
     */
    public int size() {
        return this.store.size();
    }    
    
    /**
     * @return true if store does not contain any key.
     */
    public boolean isEmpty() {
        return this.store.isEmpty();
    }

    /**
     * Returns all bytes from the store.
     *
     * @param fieldName : identifier of field hosting the value
     * @return the stored raw value whose key match provided identifier, or empty if it does not exist
     */
    public Optional<byte[]> getRawValue(String fieldName) {
        return ofNullable(store.get(fieldName))
                .map(Entry::getRawValue);
    }

    /**
     * Returns all remaining bytes from the store.
     *
     * @return the stored raw value which might remain in contents, or empty if it does not exist
     */
    public Optional<byte[]> getRemainingValue() {
        return getRawValue(REST_STORE_KEY);
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
        checkEntryType(fieldName, entry, TEXT);

        byte[] rawValue = entry.getRawValue();
        return of(
                rawToText(rawValue));
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
        checkEntryType(fieldName, entry, INTEGER);

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
        checkEntryType(fieldName, entry, FPOINT);

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
     * Returns sub-DataStores of items contained by a repeater field (level 1).
     *
     * @param repeaterFieldName : name of repeater field
     */
    public List<DataStore> getRepeatedValues(String repeaterFieldName) {
        return getRepeatedValues(repeaterFieldName, "");
    }

    /**
     * Returns sub-DataStores of items contained by a repeater field under parent repeater(s) (level 2+).
     *
     * @param repeaterFieldName : name of repeater field
     * @param parentRepeaterKey : key of parent repeater(s), e.g "lvl1[0].lvl2[1]."
     */
    public List<DataStore> getRepeatedValues(String repeaterFieldName, String parentRepeaterKey) {
        Map<Integer, List<String>> groupedKeysByIndex = store.keySet().stream()
                .filter(key -> key.startsWith(parentRepeaterKey + repeaterFieldName))
                .collect(Collectors.groupingBy(key -> {
                    int repeaterFieldPosition = key.indexOf(repeaterFieldName);
                    int indexPositionStart = key.indexOf("[", repeaterFieldPosition);
                    int indexPositionEnd = key.indexOf("]", indexPositionStart);

                    if (indexPositionStart == indexPositionEnd || indexPositionStart == -1 || indexPositionEnd == -1) {
                        return 0;
                    }

                    String lastIndexAsString = key.substring(indexPositionStart + 1, indexPositionEnd);
                    return Integer.parseInt(lastIndexAsString);
                }));

        List<DataStore> repeatedValues = createEmptyList(groupedKeysByIndex.size(), this.getFileStructure());

        for (Map.Entry<Integer, List<String>> entry: groupedKeysByIndex.entrySet()) {
            int currentIndex = entry.getKey();
            DataStore subDataStore = repeatedValues.get(currentIndex);

            for (String key : entry.getValue()) {
                String parentKey = String.format(SUB_FIELD_WITH_PARENT_KEY_PREFIX_FORMAT, parentRepeaterKey, repeaterFieldName, currentIndex);
                subDataStore.getStore().put(key.replace(parentKey, ""), store.get(key)); // extracts field name part
                subDataStore.getLinksContainer().populateFromDatastore(this);
            }
        }

        return repeatedValues;
    }

    /**
     * @return target key at address given by provided source fieldname
     */
    public Optional<String> getTargetKeyAtAddress(String sourceFieldName) {
        return getInteger(sourceFieldName)
                .flatMap(address -> getLinksContainer().getTargetFieldKeyWithAddress(address.intValue()));
    }

    /**
     * Takes all data from provided store and append it to current
     * @param otherStore - store to get all data from
     */
    public void merge(DataStore otherStore) {
        LinksContainer currentLinksContainer = this.getLinksContainer();
        Map<Integer, String> otherLinkSources = otherStore.getLinksContainer().getSources();
        Map<Integer, String> otherLinkTargets = otherStore.getLinksContainer().getTargets();

        this.getStore().putAll(otherStore.getStore());
        currentLinksContainer.getSources().putAll(otherLinkSources);
        currentLinksContainer.getTargets().putAll(otherLinkTargets);
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
     * Creates repeated values to target store, within repeated field eventually
     * @param fieldNames                : set of field names whose values will be copied
     * @param targetStore               : can be null (will use current one if so)
     * @param repeaterTargetFieldName   : can be null (will not use repeater if so)
     * @param repeaterTargetIndex       : can be null (will not use repeater if so)
     */
    public void copyFields(Set<String> fieldNames, DataStore targetStore, String repeaterTargetFieldName, Long repeaterTargetIndex) {
        requireNonNull(fieldNames, "A set of field names is required");

        DataStore effectiveTargetStore = targetStore == null ? this : targetStore;

        fieldNames
                .forEach(fieldName -> {
                    Entry sourceEntry = ofNullable(store.get(fieldName))
                            .orElseThrow(() -> new IllegalStateException("Entry not found in current store: " + fieldName));
                    byte[] sourceRawValue = sourceEntry.getRawValue();
                    Type sourceType = sourceEntry.getType();

                    if (repeaterTargetFieldName == null || repeaterTargetIndex == null) {
                        effectiveTargetStore.addValue(fieldName, sourceType, sourceRawValue);
                    } else {
                        effectiveTargetStore.addRepeatedValue(repeaterTargetFieldName, fieldName, sourceType, repeaterTargetIndex, sourceRawValue);
                    }
                });
    }

    /**
     * @return true if specified repeater with provided field name and parent repeater key has sub items
     */
    public boolean repeaterHasSubItems(String repeaterFieldName, String parentRepeaterKey) {
        requireNonNull(repeaterFieldName, "Repeater field name is required");
        requireNonNull(repeaterFieldName, "Parent repeater key is required (may be empty)");

        return parentRepeaterKey.isEmpty() || getStore().keySet().parallelStream()
                .anyMatch(k -> k.startsWith(parentRepeaterKey + repeaterFieldName));
    }

    /**
     * Returns key prefix for repeated (under repeater) field.
     *
     * @param repeaterFieldName : name of parent, repeater field
     * @param index             : item rank in repeater
     * @return a prefix allowing to parse sub-fields.
     */
    public static String generateKeyPrefixForRepeatedField(String repeaterFieldName, long index) {
        return generateKeyPrefixForRepeatedField(repeaterFieldName, index, null);
    }

    /**
     * Returns key prefix for repeated (under repeater) field.
     *
     * @param repeaterFieldName : name of parent, repeater field
     * @param index             : item rank in repeater
     * @param parentRepeaterKey : (optional) key to beb used as prefix if already under a repeater
     * @return a prefix allowing to parse sub-fields.
     */
    public static String generateKeyPrefixForRepeatedField(String repeaterFieldName, long index, String parentRepeaterKey) {
        if (parentRepeaterKey != null && !parentRepeaterKey.isEmpty()) {
            return String.format(SUB_FIELD_WITH_PARENT_KEY_PREFIX_FORMAT, parentRepeaterKey, repeaterFieldName, index);
        }
        return String.format(SUB_FIELD_PREFIX_FORMAT, repeaterFieldName, index);
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

    private static void checkEntryType(String fieldName, Entry entry, Type expectedType) {
        String message = String.format("Invalid type for entry %s: expected %s, actual %s", fieldName, expectedType, entry.getType());
        assertSimpleCondition(() -> expectedType == entry.getType(), message);
    }

    Map<String, Entry> getStore() {
        return store;
    }

    public FileStructureDto getFileStructure() {
        return fileStructure;
    }

    public LinksContainer getLinksContainer() {
        return linksContainer;
    }

}

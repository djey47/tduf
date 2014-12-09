package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Long.valueOf;
import static java.util.Objects.requireNonNull;

/**
 * Helper to read files whose file structure is available as separate asset.
 * Make it possible to extract values from them.
 */
public class GenericParser {

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^(?:.*\\.)?(.+)$");                  // e.g 'entry_list[1].my_field', 'my_field'
    private static final Pattern SUB_FIELD_NAME_PATTERN = Pattern.compile("^(.+)\\[(\\d+)\\]\\.(.+)$");     // e.g 'entry_list[1].my_field'

    private final ByteArrayInputStream inputStream;

    private final FileStructureDto fileStructure;

    private final Map<String, String> store = new HashMap<>();

    private GenericParser(ByteArrayInputStream inputStream, FileStructureDto fileStructure) {
        this.inputStream = inputStream;
        this.fileStructure = fileStructure;
    }

    /**
     * Single entry point for this parser.
     * @param inputStream   : stream containing data to be parsed
     * @param fileStructure : information about data structure
     * @return a {@link GenericParser} instance.
     */
    public static GenericParser load(ByteArrayInputStream inputStream, FileStructureDto fileStructure) {
        requireNonNull(inputStream, "Data stream is required");
        requireNonNull(fileStructure, "Data structure is required");

        return new GenericParser(inputStream, fileStructure);
    }

    /**
     * Extracts file contents according to provided structure.
     */
    public void parse() {
        this.store.clear();
        readFields(fileStructure.getFields(), "");
    }

    /**
     * Returns a list of numeric values from the store.
     * @param fieldName : name of field to search
     * @return all stored values whose key match provided identifier
     */
    public List<Long> getNumericListOf(String fieldName) {

        return store.keySet().stream()

                .filter (key -> {
                    Matcher matcher = FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() && matcher.group(1).equals(fieldName);
                })

                .map(store::get)

                .map(Long::valueOf)

                .collect(Collectors.toList());
    }

    /**
     * Returns a list of name-value pairs contained by a repeater field.
     * @param repeaterFieldName   : name of repeater field
     */
    public List<Map<String, String>> getRepeatedValuesOf(String repeaterFieldName) {

        Map<Integer, List<String>> groupedKeysByIndex = store.keySet().stream()

                .filter(key -> key.startsWith(repeaterFieldName))

                .collect(Collectors.groupingBy(key -> {
                    Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                    return matcher.matches() ? Integer.valueOf(matcher.group(2)) : 0; // extracts index part
                }));

        List<Map<String, String>> repeatedValues = createEmptyList(groupedKeysByIndex.size());

        for(Integer index : groupedKeysByIndex.keySet()) {

            Map<String, String> valuesMap = repeatedValues.get(index);

            for (String key : groupedKeysByIndex.get(index)) {
                Matcher matcher = SUB_FIELD_NAME_PATTERN.matcher(key);
                if (matcher.matches()) {
                    valuesMap.put(matcher.group(3), store.get(key));    // extracts field name part
                }
            }
        }

        return repeatedValues;
    }

    private static List<Map<String, String>> createEmptyList(int size) {
        List<Map<String, String>> list = new ArrayList<>(size);

        for (int i = 0 ; i < size ; i++) {
            list.add(new HashMap<>());
        }

        return list;
    }

    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey) {

        for(FileStructureDto.Field field : fields) {

            String name = field.getName();

            // TODO check null value when required
            Integer length = field.getSize();

            // TODO handle endianness

            FileStructureDto.Type type = field.getType();
            String value = null;

            switch(type) {

                case DELIMITER:
                    inputStream.skip(length);
                    break;

                case NUMBER:
                    byte[] numberAsBytes = new byte[length + 4]; // Prepare long values
                    inputStream.read(numberAsBytes, 4, length); // TODO reverse for low endian
                    ByteBuffer wrapped = ByteBuffer.wrap(numberAsBytes);
                    value = valueOf(wrapped.getLong()).toString();
                    break;

                case TEXT:
                    byte[] textAsBytes = new byte[length];
                    inputStream.read(textAsBytes, 0, length);
                    value = new String(textAsBytes);
                    break;

                case REPEATER:
                    int itemIndex = 0 ;
                    List<FileStructureDto.Field> subFields = field.getSubFields();
                    int subStructureSize = computeStructureSize(subFields);

                    while (inputStream.available() >= subStructureSize      // auto
                            && (length == null || itemIndex < length)) {    // specified

                        String newRepeaterKey = name + '[' + itemIndex + "].";

                        readFields(subFields, newRepeaterKey);

                        itemIndex++;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }

            if (type.isValuedToBeStored()) {
                String key = repeaterKey + name;
                this.store.put(key, value);
            }
        }
    }

    static int computeStructureSize(List<FileStructureDto.Field> fields) {
        return fields.stream()
                .mapToInt(field -> {
                    int actualSize = 0;

                    switch (field.getType()) {
                        case TEXT:
                        case NUMBER:
                        case DELIMITER:
                            actualSize = field.getSize();
                            break;

                        case REPEATER:
                            // TODO Handle automatic (unknown item count)
                            actualSize = computeStructureSize(field.getSubFields()) * field.getSize();
                            break;

                        default:
                            throw new IllegalArgumentException("Unknown field type: " + field.getType());
                    }

                    return actualSize;
                })

                .reduce((left, right) -> left + right)

                .getAsInt();
    }

    Map<String, String> getStore() {
        return store;
    }
}
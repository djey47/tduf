package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.valueOf;
import static java.util.Objects.requireNonNull;

/**
 *
 */
public class GenericParser {

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

                    while (true) {
                        if ( length == null && inputStream.available() < subStructureSize
                            || length != null && itemIndex == length ) {
                            break;
                        }

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
                        // TODO Handle repeater (auto + fixed)

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
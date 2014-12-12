package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Helper to read files whose file structure is available as separate asset.
 * Make it possible to extract values from them.
 */
public abstract class GenericParser<T> {

    private static Class<GenericParser> thisClass = GenericParser.class;

    private final ByteArrayInputStream inputStream;

    private final FileStructureDto fileStructure;

    private final DataStore dataStore = new DataStore();

    protected GenericParser(ByteArrayInputStream inputStream) throws IOException {
        requireNonNull(inputStream, "Data stream is required");
        requireNonNull(getStructureResource(), "Data structure resource is required");

        InputStream fileStructureStream = thisClass.getResourceAsStream(getStructureResource());

        this.inputStream = inputStream;
        this.fileStructure = new ObjectMapper().readValue(fileStructureStream, FileStructureDto.class);
    }

    /**
     * Extracts file contents according to provided structure.
     */
    public T parse() {
        this.dataStore.clearAll();
        readFields(fileStructure.getFields(), "");

        return generate();
    }

    static int computeStructureSize(List<FileStructureDto.Field> fields) {
        return fields.stream()
                .mapToInt(field -> {
                    int actualSize = 0;

                    switch (field.getType()) {
                        case TEXT:
                        case NUMBER:
                        case DELIMITER:
                        case GAP:
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

    /**
     * @return a parsed object instance from provided data.
     */
    protected abstract T generate();

    /**
     * @return location of resource used to describe parsed file structure (mandatory).
     */
    protected abstract String getStructureResource();

    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey) {

        for(FileStructureDto.Field field : fields) {

            String name = field.getName();

            // TODO check null value when required
            Integer length = field.getSize();

            // TODO handle endianness

            FileStructureDto.Type type = field.getType();
            byte[] readValueAsBytes = null;

            switch(type) {

                case GAP:
                    inputStream.skip(length);
                    break;

                case NUMBER:    // TODO handle other than 32 bit
                    readValueAsBytes = new byte[length + 4]; // Prepare long values
                    inputStream.read(readValueAsBytes, 4, length);
                    break;

                case DELIMITER:
                case TEXT:
                    readValueAsBytes = new byte[length];
                    inputStream.read(readValueAsBytes, 0, length);
                    break;

                case REPEATER:
                    int itemIndex = 0 ;
                    List<FileStructureDto.Field> subFields = field.getSubFields();
                    int subStructureSize = computeStructureSize(subFields);

                    while (inputStream.available() >= subStructureSize      // auto
                            && (length == null || itemIndex < length)) {    // specified

                        String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(name, itemIndex);
                        readFields(subFields, newRepeaterKeyPrefix);

                        itemIndex++;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }

            if (type.isValueToBeStored()) {
                String key = repeaterKey + name;
                this.dataStore.addRawValue(key, readValueAsBytes);
            }
        }
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    FileStructureDto getFileStructure() {
        return fileStructure;
    }

    ByteArrayInputStream getInputStream() {
        return inputStream;
    }
}
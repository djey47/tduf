package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Long.valueOf;
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

            //TODO everything should be stored to allow complete rewrite
            if (type.isValuedToBeStored()) {
                String key = repeaterKey + name;
                this.dataStore.add(key, value);
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
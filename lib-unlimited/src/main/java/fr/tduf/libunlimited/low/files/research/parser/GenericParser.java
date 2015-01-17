package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.common.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Helper to read files whose file structure is available as separate asset.
 * Make it possible to extract values from them.
 */
public abstract class GenericParser<T> {

    private static final Class<GenericParser> thisClass = GenericParser.class;

    private static final String DUMP_ENTRY_FORMAT = "%s\t<%s: %d bytes>\t%s\t%s\n";

    private final ByteArrayInputStream inputStream;

    private final FileStructureDto fileStructure;

    private final DataStore dataStore = new DataStore();

    private final StringBuilder dumpBuilder = new StringBuilder();

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
        this.dumpBuilder.setLength(0);

        readFields(fileStructure.getFields(), "");

        return generate();
    }

    /**
     * Returns parsed contents of current file.
     * @return a String with all entries.
     */
    public String dump() {
        return this.dumpBuilder.toString();
    }

    static int computeStructureSize(List<FileStructureDto.Field> fields) {
        return fields.stream()
                .mapToInt(field -> {
                    int actualSize = 0;

                    switch (field.getType()) {
                        case TEXT:
                        case INTEGER:
                        case FPOINT:
                        case DELIMITER:
                        case GAP:
                            actualSize = field.getSize();
                            break;

                        case REPEATER:
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

    // TODO handle endianness
    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey) {

        for(FileStructureDto.Field field : fields) {

            String name = field.getName();
            String key = repeaterKey + name;
            Integer length = field.getSize();

            FileStructureDto.Type type = field.getType();
            byte[] readValueAsBytes = null;
            long parsedCount;

            switch(type) {

                case GAP:
                    parsedCount = inputStream.skip(length);

                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), length, Arrays.toString(new byte[length]), ""));
                    break;

                case INTEGER:
                    readValueAsBytes = new byte[length + 4]; // Prepare long values
                    parsedCount = inputStream.read(readValueAsBytes, 4, length);

                    byte[] displayedBytes = Arrays.copyOfRange(readValueAsBytes, 4, length + 4);
                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), length, Arrays.toString(displayedBytes), TypeHelper.rawToNumeric(readValueAsBytes)));
                    break;

                case FPOINT:
                    readValueAsBytes = new byte[length];
                    parsedCount = inputStream.read(readValueAsBytes, 0, length);

                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), length, Arrays.toString(readValueAsBytes), TypeHelper.rawToFloatingPoint(readValueAsBytes)));
                    break;

                case DELIMITER:
                case TEXT:
                    readValueAsBytes = new byte[length];
                    parsedCount = inputStream.read(readValueAsBytes, 0, length);

                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), length, Arrays.toString(readValueAsBytes), "\"" +  TypeHelper.rawToText(readValueAsBytes) + "\""));
                    break;

                case REPEATER:
                    parsedCount = 0 ;

                    List<FileStructureDto.Field> subFields = field.getSubFields();
                    int subStructureSize = computeStructureSize(subFields);

                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), subStructureSize, ">>", ""));

                    while (inputStream.available() >= subStructureSize      // auto
                            && (length == null || parsedCount < length)) {    // specified

                        String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(name, parsedCount);
                        readFields(subFields, newRepeaterKeyPrefix);

                        parsedCount++;
                    }

                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), subStructureSize * parsedCount, "<<", ""));
                    break;

                case UNKNOWN:
                    if (length == null) {
                        length = inputStream.available();
                    }
                    readValueAsBytes = new byte[length];
                    parsedCount = inputStream.read(readValueAsBytes, 0, length);

                    dumpBuilder.append(String.format(DUMP_ENTRY_FORMAT, key, type.name(), length, Arrays.toString(readValueAsBytes), ""));
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }

            // Check
            assert (parsedCount == Optional.ofNullable(length).orElse((int)parsedCount));

            if (type.isValueToBeStored()) {
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
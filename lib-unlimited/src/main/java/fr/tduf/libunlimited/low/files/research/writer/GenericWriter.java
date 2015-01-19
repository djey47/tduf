package fr.tduf.libunlimited.low.files.research.writer;

import fr.tduf.libunlimited.low.files.research.common.FormulaHelper;
import fr.tduf.libunlimited.low.files.research.common.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

/**
 * Helper to write files whose file structure is available as separate asset.
 */
public abstract class GenericWriter<T> {

    private static Class<GenericWriter> thisClass = GenericWriter.class;

    private final FileStructureDto fileStructure;

    private final DataStore dataStore = new DataStore();

    private final T data;

    // TODO factor structure resource + external file
    protected GenericWriter(T data) throws IOException {
        requireNonNull(data, "Data is required");
        requireNonNull(getStructureResource(), "Data structure resource is required");

        this.data = data;

        InputStream fileStructureStream = thisClass.getResourceAsStream(getStructureResource());
        if (fileStructureStream == null) {
            // Regular file
            File file = new File(getStructureResource());
            this.fileStructure = new ObjectMapper().readValue(file, FileStructureDto.class);
        } else {
            // Resource
            this.fileStructure = new ObjectMapper().readValue(fileStructureStream, FileStructureDto.class);
        }
    }

    /**
     * Writes current data object to byte stream.
     * @return a stream with serialized data according to provided structure
     * @throws NoSuchElementException when at least one value could not be found in store.
     */
    public ByteArrayOutputStream write() throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        fillStore();

        writeFields(this.fileStructure.getFields(), outputStream, "");

        return outputStream;
    }

    // TODO handle endianness
    private boolean writeFields(List<FileStructureDto.Field> fields, ByteArrayOutputStream outputStream, String repeaterKey) throws IOException {
        for(FileStructureDto.Field field : fields) {
            String name = field.getName();
            Integer length = FormulaHelper.resolveToInteger(field.getSizeFormula(), this.dataStore);
            FileStructureDto.Type type = field.getType();

            byte[] valueBytes = null;
            if (type.isValueToBeStored()) {
                String key = repeaterKey + name;
                valueBytes = dataStore.getRawValue(key).orElse(null);
                if (valueBytes == null) {
                    throw new NoSuchElementException("Value does not exist in store for following key: " + key);
                }
            }

            switch (type) {

                case GAP:
                    outputStream.write(ByteBuffer.allocate(length).array());
                    break;

                case UNKNOWN:
                case DELIMITER:
                case TEXT:
                    assert valueBytes != null;
                    outputStream.write(valueBytes, 0, length);
                    break;

                case INTEGER:
                    assert valueBytes != null;

                    if (this.fileStructure.isLittleEndian()) {
                        valueBytes = TypeHelper.changeEndianType(valueBytes);
                        outputStream.write(valueBytes, 0, length);
                    } else {
                        outputStream.write(valueBytes, 4, length);
                    }
                    break;

                case FPOINT:
                    assert valueBytes != null;

                    if (this.fileStructure.isLittleEndian()) {
                        valueBytes = TypeHelper.changeEndianType(valueBytes);
                    }

                    outputStream.write(valueBytes, 0, length);
                    break;

                case REPEATER:
                    int itemIndex = 0;
                    boolean hasMoreFields = true;

                    while (hasMoreFields) {

                        String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(name, itemIndex);
                        try {
                            writeFields(field.getSubFields(), outputStream, newRepeaterKeyPrefix);
                        } catch (NoSuchElementException nsee) {
                            hasMoreFields = false;
                        }

                        itemIndex++;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }
        }

        return true;
    }

    /**
     * To be implemented to fill datastore with contents of a domain Object.
     */
    protected abstract void fillStore();

    /**
     * Can be used: either resource in classpath, or file path.
     * @return location of resource used to describe parsed file structure (mandatory).
     */
    protected abstract String getStructureResource();

    FileStructureDto getFileStructure() {
        return fileStructure;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    protected T getData() {
        return data;
    }
}
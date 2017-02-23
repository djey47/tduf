package fr.tduf.libunlimited.low.files.research.rw;

import fr.tduf.libunlimited.common.helper.AssertorHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.FormulaHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.domain.Type;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;
import static java.util.Objects.requireNonNull;

/**
 * Helper to write files whose file structure is available as separate asset.
 */
public abstract class GenericWriter<T> implements StructureBasedProcessor {

    private final DataStore dataStore;

    private final T data;

    protected GenericWriter(T data) throws IOException {
        requireNonNull(data, "Data is required");
        requireNonNull(getStructureResource(), "Data structure resource is required");

        this.data = data;
        this.dataStore = new DataStore(StructureHelper.retrieveStructureFromLocation(getStructureResource()));
    }

    /**
     * Writes current data object to byte stream.
     * @return a stream with serialized data according to provided structure
     * @throws NoSuchElementException when at least one value could not be found in store.
     */
    public ByteArrayOutputStream write() throws IOException {

        fillStore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeFields(this.getFileStructure().getFields(), outputStream, "");

        return StructureHelper.encryptIfNeeded(outputStream, this.getFileStructure().getCryptoMode());
    }

    /**
     * To be implemented to fill datastore with contents of a domain Object.
     */
    protected abstract void fillStore();

    private boolean writeFields(List<FileStructureDto.Field> fields, ByteArrayOutputStream outputStream, String repeaterKey) throws IOException {
        for(FileStructureDto.Field field : fields) {

            byte[] valueBytes = retrieveValueFromStore(field, repeaterKey);
            Integer length = FormulaHelper.resolveToInteger(field.getSizeFormula(), Optional.of(repeaterKey), this.dataStore);

            Type type = field.getType();
            switch (type) {
                case GAP:
                    writeGapField(length, outputStream);
                    break;

                case UNKNOWN:
                case DELIMITER:
                case TEXT:
                    writeRawValue(valueBytes, length, outputStream);
                    break;

                case INTEGER:
                    writeIntegerValue(valueBytes, length, outputStream);
                    break;

                case FPOINT:
                    writeFloatingPointValue(valueBytes, length, outputStream);
                    break;

                case REPEATER:
                    writeRepeatedFields(field, outputStream);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }
        }

        return true;
    }

    private void writeRepeatedFields(FileStructureDto.Field repeaterField, ByteArrayOutputStream outputStream) throws IOException {
        int itemIndex = 0;
        boolean hasMoreFields = true;

        while (hasMoreFields) {

            String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(repeaterField.getName(), itemIndex);
            try {
                ByteArrayOutputStream temporayOutputStream = new ByteArrayOutputStream();
                writeFields(repeaterField.getSubFields(), temporayOutputStream, newRepeaterKeyPrefix);
                outputStream.write(temporayOutputStream.toByteArray());
            } catch (NoSuchElementException nsee) {
                hasMoreFields = false;
            }

            itemIndex++;
        }
    }

    private byte[] retrieveValueFromStore(FileStructureDto.Field field, String repeaterKey) {
        byte[] valueBytes = null;
        if (field.getType().isValueToBeStored()) {
            String key = repeaterKey + field.getName();
            valueBytes = dataStore.getRawValue(key).orElse(null);
            if (valueBytes == null) {
                throw new NoSuchElementException("Value does not exist in store for following key: " + key);
            }
        }
        return valueBytes;
    }

    private void writeFloatingPointValue(final byte[] valueBytes, Integer length, ByteArrayOutputStream outputStream) {
        assertSimpleCondition(() -> valueBytes != null);

        if (getFileStructure().isLittleEndian()) {
            outputStream.write(TypeHelper.changeEndianType(valueBytes), 0, length);
        } else {
            outputStream.write(valueBytes, 0, length);
        }
    }

    private void writeGapField(Integer length, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write(new byte[length]);
    }

    private void writeRawValue(byte[] valueBytes, Integer length, ByteArrayOutputStream outputStream) throws IOException {
        assertSimpleCondition(() -> valueBytes != null);

        outputStream.write(TypeHelper.fitToSize(valueBytes, length));
    }

    private void writeIntegerValue(final byte[] valueBytes, Integer length, ByteArrayOutputStream outputStream) {
        AssertorHelper.assertSimpleCondition(() -> valueBytes != null);

        if (getFileStructure().isLittleEndian()) {
            outputStream.write(TypeHelper.changeEndianType(valueBytes), 0, length);
        } else {
            outputStream.write(valueBytes, 8 - length, length);
        }
    }

    FileStructureDto getFileStructure() {
        return this.dataStore.getFileStructure();
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    protected T getData() {
        return data;
    }
}

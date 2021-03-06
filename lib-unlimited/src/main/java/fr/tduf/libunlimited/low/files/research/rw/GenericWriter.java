package fr.tduf.libunlimited.low.files.research.rw;

import com.esotericsoftware.minlog.Log;
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

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;
import static java.util.Objects.requireNonNull;

/**
 * Helper to write files whose file structure is available as separate asset.
 */
public abstract class GenericWriter<T> implements StructureBasedProcessor {
    private static final String THIS_CLASS_NAME = GenericWriter.class.getSimpleName();

    private final DataStore dataStore;

    private final T data;

    protected GenericWriter(T data) throws IOException {
        requireNonNull(data, "Data is required");

        FileStructureDto fileStructure;
        if (getStructureResource() == null) {
            fileStructure = requireNonNull(getStructure(), "Data structure object is required");
        } else {
            fileStructure = StructureHelper.retrieveStructureFromLocation(getStructureResource());
        }

        this.data = data;
        this.dataStore = new DataStore(fileStructure);
    }

    /**
     * Writes current data object to byte stream.
     * @return a stream with serialized data according to provided structure
     * @throws NoSuchElementException when at least one value could not be found in store.
     */
    public ByteArrayOutputStream write() throws IOException {

        fillStore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileStructureDto fileStructure = getFileStructure();
        writeFields(fileStructure.getFields(), outputStream, "");

        return StructureHelper.encryptIfNeeded(outputStream, fileStructure.getCryptoMode());
    }

    /**
     * To be implemented to fill datastore with contents of a domain Object.
     */
    protected abstract void fillStore();

    private void writeFields(List<FileStructureDto.Field> fields, ByteArrayOutputStream outputStream, String repeaterKey) throws IOException {
        for(FileStructureDto.Field field : fields) {

            // Check for satisfied condition first
            String condition = field.getCondition();
            String fieldKey = repeaterKey + field.getName();
            boolean isConditionSatisfied = condition == null || FormulaHelper.resolveCondition(condition, repeaterKey, getDataStore());
            if (!isConditionSatisfied) {
                Log.debug(THIS_CLASS_NAME, String.format("Unsatisfied condition for field at key %s: '%s', skipping", fieldKey, condition));
                continue;
            }

            byte[] valueBytes = retrieveValueFromStore(field, repeaterKey);
            Integer length = FormulaHelper.resolveToInteger(field.getSizeFormula(), repeaterKey, this.dataStore);

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
                    
                case CONSTANT:
                    valueBytes = TypeHelper.hexRepresentationToByteArray(field.getConstantValue());
                    writeRawValue(valueBytes, valueBytes.length, outputStream);
                    break;

                case INTEGER:
                    writeIntegerValue(valueBytes, length, outputStream);
                    break;

                case FPOINT:
                    writeFloatingPointValue(valueBytes, length, outputStream);
                    break;

                case REPEATER:
                    writeRepeatedFields(field, repeaterKey, outputStream);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }
        }

        // Handle remaining bytes, if any
        if (repeaterKey.isEmpty()) {
            fetchAndWriteRemainingBytes(outputStream);
        }        
    }
    
    private void writeRepeatedFields(FileStructureDto.Field repeaterField, String parentRepeaterKey, ByteArrayOutputStream outputStream) throws IOException {
        Integer repeatedItemsCount = FormulaHelper.resolveToInteger(repeaterField.getSizeFormula(), parentRepeaterKey, this.dataStore);

        try {
            for (int itemIndex = 0 ; repeatedItemsCount == null || itemIndex < repeatedItemsCount ; itemIndex++) {
                String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(repeaterField.getName(), itemIndex, parentRepeaterKey);
                ByteArrayOutputStream temporayOutputStream = new ByteArrayOutputStream();
                writeFields(repeaterField.getSubFields(), temporayOutputStream, newRepeaterKeyPrefix);
                outputStream.write(temporayOutputStream.toByteArray());
            }
        } catch (NoSuchElementException nsee) {
            // Ignoring exception is normal, no more items in this repeater
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

    private void fetchAndWriteRemainingBytes(ByteArrayOutputStream outputStream) {
        dataStore.getRemainingValue()
                .ifPresent(remainingBytes -> {
                    try {
                        writeRawValue(remainingBytes, null, outputStream);
                    } catch (IOException ioe) {
                        throw new IllegalStateException("Unable to write remaining bytes", ioe);
                    }
                });
    }

    // Visible for testing use
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

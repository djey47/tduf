package fr.tduf.libunlimited.low.files.research.rw;

import fr.tduf.libunlimited.low.files.common.domain.DataStoreProps;
import fr.tduf.libunlimited.low.files.research.common.helper.FormulaHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;
import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.*;
import static java.util.Objects.requireNonNull;

/**
 * Helper to read files whose file structure is available as separate asset.
 * Make it possible to extract values from them.
 */
public abstract class GenericParser<T> implements StructureBasedProcessor {

    private static final String DUMP_START_ENTRY_FORMAT = "%s\t<%s%s: %d bytes>\t%s\t%s\n";
    private static final String DUMP_REPEATER_START_ENTRY_FORMAT = "%s\t<%s>\t>>\n";
    private static final String DUMP_REPEATER_FINISH_ENTRY_FORMAT = "<< %s\t<%s: %d items>\n";
    private static final String DUMP_LABEL_SIGNED = "signed ";
    private static final String DUMP_LABEL_UNSIGNED = "unsigned ";

    private final ByteArrayInputStream inputStream;

    private final DataStore dataStore;

    private final StringBuilder dumpBuilder = new StringBuilder();

    protected GenericParser(ByteArrayInputStream inputStream) throws IOException {
        requireNonNull(inputStream, "Data stream is required");
        requireNonNull(getStructureResource(), "Data structure resource is required");

        FileStructureDto fileStructure = StructureHelper.retrieveStructureFromLocation(getStructureResource());
        this.dataStore = new DataStore(fileStructure);
        this.inputStream = StructureHelper.decryptIfNeeded(inputStream, fileStructure.getCryptoMode());
    }

    /**
     * Extracts file contents according to provided structure.
     */
    public T parse() {
        this.dataStore.clearAll();
        this.dumpBuilder.setLength(0);

        readFields(getFileStructure().getFields(), "");

        return generate();
    }

    /**
     * Returns parsed contents of current file.
     * @return a String with all entries.
     */
    public String dump() {
        return this.dumpBuilder.toString();
    }

    /**
     * @return numeric value from prop info
     */
    public static Optional<Long> getNumeric(DataStore dataStore, DataStoreProps dataProp) {
        return dataStore.getInteger(dataProp.getStoreFieldName());
    }

    /**
     * Update numeric value to prop
     * @param value : Integer or Long value
     */
    public static void setNumeric(Object value, DataStore dataStore, DataStoreProps dataProp) {
        if (!(value instanceof Long) && !(value instanceof Integer)) {
            throw new IllegalArgumentException("Value type is not supported: " + value.getClass());
        }

        long effectiveValue;
        if (value instanceof Integer) {
            effectiveValue = ((Integer)value).longValue();
        } else {
            effectiveValue = (long) value;
        }

        dataStore.addInteger(dataProp.getStoreFieldName(), effectiveValue);
    }

    /**
     * @return a parsed object instance from provided data.
     */
    protected abstract T generate();

    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey) {

        for(FileStructureDto.Field field : fields) {

            String key = repeaterKey + field.getName();
            Integer length = FormulaHelper.resolveToInteger(field.getSizeFormula(), Optional.of(repeaterKey), dataStore);
            ReadResult readResult = readAndDumpValue(key, field, length);

            assertSimpleCondition(() -> length == null || readResult.parsedCount == length, "Structure mismatch for field key: " + key);

            this.dataStore.addValue(key, field.getType(), field.isSigned(), readResult.readValueAsBytes);
        }
    }

    private ReadResult readAndDumpValue(String key, FileStructureDto.Field field, Integer length) {
        ReadResult readResult;
        FileStructureDto.Type type = field.getType();
        switch(type) {
            case GAP:
                readResult = jumpGap(length);
                dumpGap(length, key);
                break;

            case INTEGER:
                readResult = readIntegerValue(length);
                dumpIntegerValue(readResult.readValueAsBytes, length, field.isSigned(), key);
                break;

            case FPOINT:
                readResult = readFloatingPointValue(length);
                dumpFloatingPointValue(readResult.readValueAsBytes, length, key);
                break;

            case DELIMITER:
            case TEXT:
                readResult = readDelimiterOrTextValue(length);
                dumpDelimiterOrTextValue(readResult.readValueAsBytes, length, key, type);
                break;

            case UNKNOWN:
                readResult = readRawValue(length);
                dumpRawValue(readResult.readValueAsBytes, length, key);
                break;

            case REPEATER:
                dumpRepeater(key, Optional.empty());
                readResult = readRepeatedValues(field, length);
                dumpRepeater(key, Optional.of(readResult));
                break;

            default:
                throw new IllegalArgumentException("Unknown field type: " + type);
        }
        return readResult;
    }

    private ReadResult readRepeatedValues(FileStructureDto.Field repeaterField, Integer length) {
        List<FileStructureDto.Field> subFields = repeaterField.getSubFields();

        long parsedCount = 0;
        while (inputStream.available() > 0                        // auto
                && (length == null || parsedCount < length)) {    // specified

            String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(repeaterField.getName(), parsedCount);
            readFields(subFields, newRepeaterKeyPrefix);

            parsedCount++;
        }

        return new ReadResult(parsedCount);
    }

    private ReadResult jumpGap(Integer length) {
        long parsedCount = inputStream.skip(length);

        return new ReadResult(parsedCount, new byte[length]);
    }

    private ReadResult readIntegerValue(Integer length) {
        byte[] readValueAsBytes = new byte[8]; // Prepare long values
        long parsedCount;

        if (this.getFileStructure().isLittleEndian()) {
            parsedCount = inputStream.read(readValueAsBytes, 0, length);
            readValueAsBytes = TypeHelper.changeEndianType(readValueAsBytes);
        } else {
            parsedCount = inputStream.read(readValueAsBytes, 8-length, length);
        }

        return new ReadResult(parsedCount, readValueAsBytes);
    }

    private ReadResult readFloatingPointValue(Integer length) {
        byte[] readValueAsBytes = new byte[length];
        long parsedCount = inputStream.read(readValueAsBytes, 0, length);

        if (this.getFileStructure().isLittleEndian()) {
            readValueAsBytes = TypeHelper.changeEndianType(readValueAsBytes);
        }

        return new ReadResult(parsedCount, readValueAsBytes);
    }

    private ReadResult readDelimiterOrTextValue(Integer length) {
        byte[] readValueAsBytes = new byte[length];
        long parsedCount = inputStream.read(readValueAsBytes, 0, length);

        return new ReadResult(parsedCount, readValueAsBytes);
    }

    private ReadResult readRawValue(Integer length) {
        // Autosize handle
        int actualSize = length == null ? inputStream.available() : length;
        byte[] readValueAsBytes = new byte[actualSize];
        long parsedCount = inputStream.read(readValueAsBytes, 0, actualSize);

        return new ReadResult(parsedCount, readValueAsBytes);
    }

    private void dumpGap(Integer length, String key) {
        dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                GAP.name(),
                length,
                TypeHelper.byteArrayToHexRepresentation(new byte[length]),
                ""));
    }

    private void dumpIntegerValue(byte[] readValueAsBytes, int length, boolean signedValue, String key) {
        byte[] displayedBytes = Arrays.copyOfRange(readValueAsBytes, 8 - length, 8);
        dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT,
                key,
                signedValue ? DUMP_LABEL_SIGNED : DUMP_LABEL_UNSIGNED,
                INTEGER.name(),
                length,
                TypeHelper.byteArrayToHexRepresentation(displayedBytes),
                TypeHelper.rawToInteger(readValueAsBytes, signedValue)));
    }

    private void dumpFloatingPointValue(byte[] readValueAsBytes, Integer length,  String key) {
        dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                FPOINT.name(),
                length,
                TypeHelper.byteArrayToHexRepresentation(readValueAsBytes),
                TypeHelper.rawToFloatingPoint(readValueAsBytes)));
    }

    private void dumpDelimiterOrTextValue(byte[] readValueAsBytes, Integer length, String key, FileStructureDto.Type type) {
        dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                type.name(),
                length == null ? readValueAsBytes.length : length,
                TypeHelper.byteArrayToHexRepresentation(readValueAsBytes),
                "\"" + TypeHelper.rawToText(readValueAsBytes, length) + "\""));
    }

    private void dumpRawValue(byte[] readValueAsBytes, Integer length, String key) {
        dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                UNKNOWN.name(),
                length == null ? readValueAsBytes.length : length,
                TypeHelper.byteArrayToHexRepresentation(readValueAsBytes),
                ""));
    }

    private void dumpRepeater(String key, Optional<ReadResult> readResult) {
        if (readResult.isPresent()) {
            dumpBuilder.append(String.format(DUMP_REPEATER_FINISH_ENTRY_FORMAT,
                    key,
                    REPEATER.name(),
                    readResult.get().parsedCount));
        } else {
            dumpBuilder.append(String.format(DUMP_REPEATER_START_ENTRY_FORMAT,
                    key,
                    REPEATER.name()));
        }
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    FileStructureDto getFileStructure() {
        return this.dataStore.getFileStructure();
    }

    ByteArrayInputStream getInputStream() {
        return inputStream;
    }

    private class ReadResult {
        private final byte[] readValueAsBytes;
        private final long parsedCount;

        ReadResult(long parsedCount, byte[] readValueAsBytes) {
            this.readValueAsBytes = readValueAsBytes;
            this.parsedCount = parsedCount;
        }

        ReadResult(long parsedCount) {
            this(parsedCount, new byte[0]);
        }
    }
}

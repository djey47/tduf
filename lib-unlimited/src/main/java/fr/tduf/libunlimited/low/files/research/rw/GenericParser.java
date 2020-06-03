package fr.tduf.libunlimited.low.files.research.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.common.domain.DataStoreProps;
import fr.tduf.libunlimited.low.files.research.common.helper.FormulaHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.domain.Type;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;
import static fr.tduf.libunlimited.low.files.research.domain.Type.*;
import static java.util.Objects.requireNonNull;

/**
 * Helper to read files whose file structure is available as separate asset.
 * Make it possible to extract values from them.
 */
public abstract class GenericParser<T> implements StructureBasedProcessor {
    private static final String THIS_CLASS_NAME = GenericParser.class.getSimpleName();

    private static final String DUMP_START_ENTRY_FORMAT = "%s\t<%s%s: %d bytes>\t%s\t%s\n";
    private static final String DUMP_REPEATER_START_ENTRY_FORMAT = "%s\t<%s>\t>>\n";
    private static final String DUMP_REPEATER_FINISH_ENTRY_FORMAT = "<< %s\t<%s: %d items>\n";
    private static final String DUMP_LABEL_SIGNED = "signed ";
    private static final String DUMP_LABEL_UNSIGNED = "unsigned ";

    private final XByteArrayInputStream inputStream;

    private final DataStore dataStore;

    private final StringBuilder dumpBuilder = new StringBuilder();

    protected GenericParser(XByteArrayInputStream inputStream) throws IOException {
        requireNonNull(inputStream, "Data stream is required");

        FileStructureDto fileStructure;
        if (getStructureResource() == null) {
            fileStructure = requireNonNull(getStructure(), "Data structure object is required");
        } else {
            fileStructure = StructureHelper.retrieveStructureFromLocation(getStructureResource());
        }

        this.dataStore = new DataStore(fileStructure);
        this.inputStream = StructureHelper.decryptIfNeeded(inputStream, fileStructure.getCryptoMode());
    }

    /**
     * Extracts file contents according to provided structure.
     */
    public T parse() {
        dataStore.clearAll();
        dumpBuilder.setLength(0);
        inputStream.reset();

        readFields(getFileStructure().getFields(), "");

        dataStore.getLinksContainer().validate();

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
     * @return a parsed object instance from provided data.
     */
    protected abstract T generate();

    ReadResult readRawValue(Integer length) {
        int availableBytes = inputStream.available();
        if (availableBytes == 0 && length != null) {
            throw new IllegalArgumentException(String.format("Cannot read raw value of size %d - end of file was reached", length));
        }

        // Autosize handle
        int actualSize = length == null ? availableBytes : length;
        if (actualSize < 0) {
            throw new IllegalArgumentException("Invalid raw value size supplied: " + length);
        }

        byte[] readValueAsBytes = new byte[actualSize];
        long parsedCount = inputStream.read(readValueAsBytes, 0, actualSize);

        return new ReadResult(parsedCount, readValueAsBytes);
    }

    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey) {

        for(FileStructureDto.Field field : fields) {

            String key = repeaterKey + field.getName();

            // First evaluate condition, if any
            String condition = field.getCondition();
            if (condition != null && !FormulaHelper.resolveCondition(condition, repeaterKey, dataStore)) {
                Log.debug(THIS_CLASS_NAME, String.format("Unsatisfied condition '%s' for field: %s, skipping", condition, field.getName()));
                continue;
            }

            Integer length = FormulaHelper.resolveToInteger(field.getSizeFormula(), repeaterKey, dataStore);
            ReadResult readResult = readAndDumpValue(key, field, length);

            String messageFormat = "Structure mismatch for field key: %s (%s) - expected length : %d, parsed: %d";
            long parsedCount = readResult.parsedCount;
            Type type = field.getType();
            assertSimpleCondition(() -> length == null || parsedCount == length,
                    String.format(messageFormat, key, type.toString(), length, parsedCount));

            dataStore.addValue(key, type, field.isSigned(), length, readResult.readValueAsBytes);
        }

        // Handle remaining bytes at level 0
        if (repeaterKey.isEmpty() && inputStream.available() > 0) {
            readAndStoreRemainingBytes();
        }
    }

    private ReadResult readAndDumpValue(String key, FileStructureDto.Field field, Integer length) {
        if (Log.DEBUG) {
            int currentPosition = inputStream.position();
            Log.debug(THIS_CLASS_NAME, String.format("Parsing@0x%08X (%d) bytes...", currentPosition, currentPosition));
        }

        ReadResult readResult;
        Type type = field.getType();
        switch(type) {
            case GAP:
                readResult = jumpGap(field, length);
                dumpGap(length, key);
                break;

            case INTEGER:
                readResult = readIntegerValue(length);
                long readValue = TypeHelper.rawToInteger(readResult.readValueAsBytes, field.isSigned(), length);

                // Links handling
                if (field.isLinkSource()) {
                    handleLinkSource(key, (int) readValue);
                }

                dumpIntegerValue(field, readResult.readValueAsBytes, readValue, length, key);
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

            case CONSTANT:
                readResult = readConstantValue(field);
                dumpConstantValue(readResult.readValueAsBytes, field.getConstantValue(), key);
                break;

            case REPEATER:
                boolean isLinkTarget = field.isLinkTarget();
                dumpRepeaterStart(key, isLinkTarget);
                readResult = readRepeatedValues(field, length, key);
                dumpRepeaterFinish(key, readResult, isLinkTarget);
                break;

            default:
                throw new IllegalArgumentException("Unknown field type: " + type);
        }
        return readResult;
    }

    private ReadResult readRepeatedValues(FileStructureDto.Field repeaterField, Integer length, String parentRepeaterKey) {
        long parsedCount = 0;
        Integer repeaterContentsSize = FormulaHelper.resolveToInteger(repeaterField.getContentsSizeFormula(), parentRepeaterKey, dataStore);
        int endStreamPosition = repeaterContentsSize == null ? 0 : inputStream.position() + repeaterContentsSize;
        while (inputStream.available() > 0                                                          // auto, till EOS
                && (length == null || parsedCount < length)                                         // specified in items count
                && (repeaterContentsSize == null || inputStream.position() < endStreamPosition) ) { // specified in contents bytes

            String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(parentRepeaterKey, parsedCount);

            // Target links support
            if (repeaterField.isLinkTarget()) {
                dataStore.getLinksContainer().registerTarget(newRepeaterKeyPrefix, inputStream.position());
            }

            readFields(repeaterField.getSubFields(), newRepeaterKeyPrefix);

            parsedCount++;
        }

        return new ReadResult(parsedCount);
    }

    private ReadResult jumpGap(FileStructureDto.Field fieldSettings, Integer length) {
        byte[] gapValue = new byte[length];
        return readConstantValue(fieldSettings, gapValue);
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

    private ReadResult readConstantValue(FileStructureDto.Field fieldSettings) {
        String constantValue = fieldSettings.getConstantValue();
        byte[] expectedValueAsBytes = TypeHelper.hexRepresentationToByteArray(constantValue);

        return readConstantValue(fieldSettings, expectedValueAsBytes);
    }

    private ReadResult readConstantValue(FileStructureDto.Field fieldSettings, byte[] expectedValueAsBytes) {
        requireNonNull(expectedValueAsBytes, "Expected values as byte array must be provided");

        byte[] readValueAsBytes = new byte[expectedValueAsBytes.length];
        long parsedCount = inputStream.read(readValueAsBytes, 0, readValueAsBytes.length);

        // Perform check
        if (fieldSettings.isConstantChecked()) {
            if (!Arrays.equals(expectedValueAsBytes, readValueAsBytes)) {
                String actualValueAsString = TypeHelper.byteArrayToHexRepresentation(readValueAsBytes);
                String expectedValueAsString = TypeHelper.byteArrayToHexRepresentation(expectedValueAsBytes);
                throw new IllegalStateException(String.format("Constant check failed for field: %s - expected: %s, read: %s", fieldSettings.getName(), expectedValueAsString, actualValueAsString));
            }
        }

        return new ReadResult(parsedCount, readValueAsBytes);
    }

    private void readAndStoreRemainingBytes() {
        FileStructureDto.Field unknownField = FileStructureDto.Field.builder().withType(UNKNOWN).build();
        ReadResult readResult = readAndDumpValue("#rest#(remaining bytes)", unknownField, inputStream.available());
        dataStore.addRemainingValue(readResult.readValueAsBytes);
    }

    private void handleLinkSource(String fieldKey, int targetAddress) {
        dataStore.getLinksContainer().registerSource(fieldKey, targetAddress);
    }

    private void dumpGap(Integer length, String key) {
        String currentDump = String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                GAP.name(),
                length,
                TypeHelper.byteArrayToHexRepresentation(new byte[length]),
                "");
        updateDump(currentDump);
    }

    private void dumpIntegerValue(FileStructureDto.Field field, byte[] readValueAsBytes, long readValue, Integer length, String key) {
        byte[] displayedBytes = Arrays.copyOfRange(readValueAsBytes, 8 - length, 8);
        String currentDump = String.format(DUMP_START_ENTRY_FORMAT,
                key,
                field.isSigned() ? DUMP_LABEL_SIGNED : DUMP_LABEL_UNSIGNED,
                INTEGER.name(),
                length,
                TypeHelper.byteArrayToHexRepresentation(displayedBytes),
                field.isLinkSource() ? String.format("Link source to @0x%08X (%d)", readValue, readValue) : readValue);
        updateDump(currentDump);
    }

    private void dumpFloatingPointValue(byte[] readValueAsBytes, Integer length,  String key) {
        String currentDump = String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                FPOINT.name(),
                length,
                TypeHelper.byteArrayToHexRepresentation(readValueAsBytes),
                TypeHelper.rawToFloatingPoint(readValueAsBytes));
        updateDump(currentDump);
    }

    private void dumpDelimiterOrTextValue(byte[] readValueAsBytes, Integer length, String key, Type type) {
        int effectiveLength = length == null ? readValueAsBytes.length : length;
        String currentDump = String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                type.name(),
                effectiveLength,
                TypeHelper.byteArrayToHexRepresentation(readValueAsBytes),
                "\"" + TypeHelper.rawToText(readValueAsBytes, effectiveLength) + "\"");
        updateDump(currentDump);
    }

    private void dumpRawValue(byte[] readValueAsBytes, Integer length, String key) {
        String currentDump = String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                UNKNOWN.name(),
                length == null ? readValueAsBytes.length : length,
                TypeHelper.byteArrayToHexRepresentation(readValueAsBytes),
                "");
        updateDump(currentDump);
    }

    private void dumpConstantValue(byte[] readValueAsBytes, String constantValue, String key) {
        int length = readValueAsBytes.length;
        String currentDump = String.format(DUMP_START_ENTRY_FORMAT,
                key,
                "",
                CONSTANT.name(),
                length,
                constantValue,
                "\"" + TypeHelper.rawToText(readValueAsBytes, length) + "\"");
        updateDump(currentDump);
    }

    private void dumpRepeaterStart(String key, boolean isLinkTarget) {
        String linkTargetMention = isLinkTarget ? " (link target)" : "";
        String currentDump = String.format(DUMP_REPEATER_START_ENTRY_FORMAT,
                key,
                REPEATER.name() + linkTargetMention);
        updateDump(currentDump);
    }

    private void dumpRepeaterFinish(String key, ReadResult readResult, boolean isLinkTarget) {
        if (readResult == null) {
            dumpRepeaterStart(key, isLinkTarget);
            return;
        }

        String currentDump = String.format(DUMP_REPEATER_FINISH_ENTRY_FORMAT,
                key,
                REPEATER.name(),
                readResult.parsedCount);
        updateDump(currentDump);
    }

    private void updateDump(String dumpExtract) {
        dumpBuilder.append(dumpExtract);
        if (Log.DEBUG) {
            Log.debug(THIS_CLASS_NAME, dumpExtract);
        }
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    // Visible for testing use
    FileStructureDto getFileStructure() {
        return this.dataStore.getFileStructure();
    }

    ByteArrayInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Encapsulates result of reading in stream
     */
    static class ReadResult {
        private final byte[] readValueAsBytes;
        private final long parsedCount;

        ReadResult(long parsedCount, byte[] readValueAsBytes) {
            this.readValueAsBytes = readValueAsBytes;
            this.parsedCount = parsedCount;
        }

        ReadResult(long parsedCount) {
            this(parsedCount, new byte[0]);
        }

        byte[] getReadValueAsBytes() {
            return this.readValueAsBytes;
        }
    }
}

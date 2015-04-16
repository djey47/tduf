package fr.tduf.libunlimited.low.files.research.rw;

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

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.INTEGER;
import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.REPEATER;
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
     * @return a parsed object instance from provided data.
     */
    protected abstract T generate();

    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey) {

        for(FileStructureDto.Field field : fields) {

            String name = field.getName();
            String key = repeaterKey + name;
            Integer length = FormulaHelper.resolveToInteger(field.getSizeFormula(), repeaterKey, this.dataStore);

            FileStructureDto.Type type = field.getType();
            boolean signedValue = field.isSigned();

            byte[] readValueAsBytes = null;
            long parsedCount;

            switch(type) {

                case GAP:
                    parsedCount = inputStream.skip(length);

                    dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT, key, "", type.name(), length, TypeHelper.byteArrayToHexRepresentation(new byte[length]), ""));
                    break;

                case INTEGER:
                    readValueAsBytes = new byte[8]; // Prepare long values

                    if (this.getFileStructure().isLittleEndian()) {
                        parsedCount = inputStream.read(readValueAsBytes, 0, length);
                        readValueAsBytes = TypeHelper.changeEndianType(readValueAsBytes);
                    } else {
                        parsedCount = inputStream.read(readValueAsBytes, 8-length, length);
                    }

                    dumpIntegerValue(readValueAsBytes, length, signedValue, key);
                    break;

                case FPOINT:
                    readValueAsBytes = new byte[length];
                    parsedCount = inputStream.read(readValueAsBytes, 0, length);

                    if (this.getFileStructure().isLittleEndian()) {
                        readValueAsBytes = TypeHelper.changeEndianType(readValueAsBytes);
                    }

                    dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT, key, "", type.name(), length, TypeHelper.byteArrayToHexRepresentation(readValueAsBytes), TypeHelper.rawToFloatingPoint(readValueAsBytes)));
                    break;

                case DELIMITER:
                case TEXT:
                    readValueAsBytes = new byte[length];
                    parsedCount = inputStream.read(readValueAsBytes, 0, length);

                    dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT, key, "", type.name(), length, TypeHelper.byteArrayToHexRepresentation(readValueAsBytes), "\"" + TypeHelper.rawToText(readValueAsBytes) + "\""));
                    break;

                case REPEATER:
                    parsedCount = readRepeatedValues(field, length, key);
                    break;

                case UNKNOWN:
                    // Autosize handle
                    int actualSize = length == null ? inputStream.available() : length;
                    readValueAsBytes = new byte[actualSize];
                    parsedCount = inputStream.read(readValueAsBytes, 0, actualSize);

                    dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT, key, "", type.name(), length, TypeHelper.byteArrayToHexRepresentation(readValueAsBytes), ""));
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }

            // Check
            assert (parsedCount == Optional.ofNullable(length).orElse((int)parsedCount));

            this.dataStore.addValue(key, type, signedValue, readValueAsBytes);
        }
    }

    private long readRepeatedValues(FileStructureDto.Field repeaterField, Integer length, String key) {

        dumpBuilder.append(String.format(DUMP_REPEATER_START_ENTRY_FORMAT, key, REPEATER.name()));

        List<FileStructureDto.Field> subFields = repeaterField.getSubFields();

        long parsedCount = 0;
        while (inputStream.available() > 0                        // auto
                && (length == null || parsedCount < length)) {    // specified

            String newRepeaterKeyPrefix = DataStore.generateKeyPrefixForRepeatedField(repeaterField.getName(), parsedCount);
            readFields(subFields, newRepeaterKeyPrefix);

            parsedCount++;
        }

        dumpBuilder.append(String.format(DUMP_REPEATER_FINISH_ENTRY_FORMAT, key, REPEATER.name(), parsedCount));

        return parsedCount;
    }

    private void dumpIntegerValue(byte[] readValueAsBytes, Integer length, boolean signedValue, String key) {
        long integerValue = TypeHelper.rawToInteger(readValueAsBytes, signedValue);
        byte[] displayedBytes = Arrays.copyOfRange(readValueAsBytes, 8 - length, 8);

        dumpBuilder.append(String.format(DUMP_START_ENTRY_FORMAT, key, signedValue ? DUMP_LABEL_SIGNED : DUMP_LABEL_UNSIGNED, INTEGER.name(), length, TypeHelper.byteArrayToHexRepresentation(displayedBytes), integerValue));
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
}
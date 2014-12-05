package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayInputStream;
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
     *
     */
    public void parse() {

        Map<String, String> store = new HashMap<>();

        readFields(fileStructure.getFields(), "", store);
    }

    private void readFields(List<FileStructureDto.Field> fields, String repeaterKey, Map<String, String> store) {

        int fieldIndex = 0;
        for(FileStructureDto.Field field : fileStructure.getFields()) {

            String name = field.getName();
            int length = field.getSize();
            FileStructureDto.Type type = field.getType();
            List<FileStructureDto.Field> subFields = field.getSubFields();
            String value = null;

            switch(type) {

                case DELIMITER:
                    inputStream.skip(length);
                    break;

                case INTEGER:
                    value = valueOf(inputStream.read()).toString();
                    break;

                case STRING:
                    byte[] bytes = new byte[length];
                    inputStream.read(bytes, 0, length);
                    value = new String(bytes);
                    break;

                case REPEATER:
                    String newRepeaterKey = name + '[' + fieldIndex + ']';
                    readFields(subFields, newRepeaterKey, store);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown field type: " + type);
            }

            String key = repeaterKey + "." + name;
            store.put(key, value);
        }

    }
}

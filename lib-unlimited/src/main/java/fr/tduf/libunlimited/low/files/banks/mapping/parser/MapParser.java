package fr.tduf.libunlimited.low.files.banks.mapping.parser;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.parser.GenericParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to fetch entry list contained in Bnk1.map file.
 */
// TODO Generify
public class MapParser {

    private final GenericParser parser;

    private MapParser(ByteArrayInputStream inputStream) throws IOException {
        InputStream structureAsStream = getClass().getResourceAsStream("/files/structures/MAP4-map.json");
        FileStructureDto fileStructure = new ObjectMapper().readValue(structureAsStream, FileStructureDto.class);

        parser = GenericParser.load(inputStream, fileStructure);
    }

    /**
     * Single entry point for this parser.
     * @return a {@link MapParser} instance.
     */
    public static MapParser load(ByteArrayInputStream inputStream) throws IOException {
        requireNonNull(inputStream, "A stream containing map contents is required");

        return new MapParser(inputStream);
    }

    /**
     * @return a {@link fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap} instance from provided data.
     */
    public BankMap parse() {
        parser.parse();

        List<Map<String, String>> repeatedValues = parser.getRepeatedValuesOf("entry_list");

        BankMap bankMap = new BankMap();

        for (Map<String, String> values : repeatedValues) {

            long checksum = Long.valueOf(values.get("file_name_hash"));
            long size1 = Long.valueOf(values.get("size_bytes_1"));
            long size2 = Long.valueOf(values.get("size_bytes_2"));

            bankMap.addEntry(checksum, size1, size2);
        }

        return bankMap;
    }

    GenericParser getParser() {
        return parser;
    }
}

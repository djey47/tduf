package fr.tduf.libunlimited.low.files.banks.mapping.parser;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.parser.GenericParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to fetch entry list contained in Bnk1.map file.
 */
public class MapParser extends GenericParser<BankMap> {

    private MapParser(ByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Single entry point for this parser.
     * @param inputStream   : stream containing data to be parsed
     * @return a {@link MapParser} instance.
     */
    public static MapParser load(ByteArrayInputStream inputStream) throws IOException {
        requireNonNull(inputStream, "A stream containing map contents is required");

        return new MapParser(inputStream);
    }

    @Override
    protected BankMap generate() {
        List<Map<String, String>> repeatedValues = getDataStore().getRepeatedValuesOf("entry_list");

        BankMap bankMap = new BankMap();

        for (Map<String, String> values : repeatedValues) {

            long checksum = Long.valueOf(values.get("file_name_hash"));
            long size1 = Long.valueOf(values.get("size_bytes_1"));
            long size2 = Long.valueOf(values.get("size_bytes_2"));

            bankMap.addEntry(checksum, size1, size2);
        }

        return bankMap;
    }

    @Override
    protected String getStructureResource() {
        return "/files/structures/MAP4-map.json";
    }
}
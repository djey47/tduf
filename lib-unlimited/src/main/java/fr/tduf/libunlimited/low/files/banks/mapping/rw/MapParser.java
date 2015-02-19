package fr.tduf.libunlimited.low.files.banks.mapping.rw;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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
        List<DataStore> repeatedValues = getDataStore().getRepeatedValues("entry_list");

        BankMap bankMap = new BankMap();

        bankMap.setTag(getDataStore().getText("tag").get());

        for (DataStore subDataStore : repeatedValues) {

            long checksum = subDataStore.getInteger("file_name_hash").get();
            long size1 = subDataStore.getInteger("size_bytes_1").get();
            long size2 = subDataStore.getInteger("size_bytes_2").get();

            bankMap.addEntry(checksum, size1, size2);

            if (bankMap.getEntrySeparator() == null) {
                bankMap.setEntrySeparator(subDataStore.getRawValue("entry_end").get());
            }
        }

        return bankMap;
    }

    @Override
    protected String getStructureResource() {
        return "/files/structures/MAP4-map.json";
    }
}
package fr.tduf.libunlimited.low.files.banks.mapping.writer;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.writer.GenericWriter;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Helper class to persist {@link fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap} instance to specified Bnk1.map file.
 */
public class MapWriter extends GenericWriter<BankMap> {

    private MapWriter(BankMap bankMap) throws IOException {
        super(bankMap);
    }

    /**
     * Single entry point for this writer.
     * @param bankMap   : mapping data to be written
     * @return a {@link MapWriter} instance.
     */
    public static MapWriter load(BankMap bankMap) throws IOException {
        requireNonNull(bankMap, "Bank mapping is required");

        return new MapWriter(bankMap);
    }

    @Override
    protected void fillStore() {

        getDataStore().addText("tag", "MAP4\0");

        List<BankMap.Entry> sortedEntries = this.getData().getEntries().stream()

                .sorted((entry1, entry2) -> Long.compare(entry1.getHash(), entry2.getHash()))

                .collect(toList());

        int index = 0;
        for(BankMap.Entry entry : sortedEntries) {

            getDataStore().addRepeatedNumericValue("entry_list", "file_name_hash", index, entry.getHash());
            getDataStore().addRepeatedNumericValue("entry_list", "size_bytes_1", index, entry.getSize1());
            getDataStore().addRepeatedNumericValue("entry_list", "size_bytes_2", index, entry.getSize2());
            getDataStore().addRepeatedRawValue("entry_list", "entry_end", index, this.getData().getEntrySeparator());

            index++;
        }
    }

    @Override
    protected String getStructureResource() {
        return "/files/structures/MAP4-map.json";
    }
}
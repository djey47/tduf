package fr.tduf.libunlimited.low.files.banks.mapping.rw;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;
import java.util.List;

import static java.util.Comparator.comparingLong;
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
        List<BankMap.Entry> sortedEntries = this.getData().getEntries().stream()
                .sorted(comparingLong(BankMap.Entry::getHash))
                .collect(toList());

        int index = 0;
        for(BankMap.Entry entry : sortedEntries) {

            getDataStore().addRepeatedInteger32("entry_list", "file_name_hash", index, entry.getHash());
            getDataStore().addRepeatedInteger32("entry_list", "size_bytes_1", index, entry.getSize1());
            getDataStore().addRepeatedInteger32("entry_list", "size_bytes_2", index, entry.getSize2());
            getDataStore().addRepeatedValue("entry_list", "entry_end", index, this.getData().getEntrySeparator());

            index++;
        }
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/MAP4-map.json";
    }

    @Override
    public FileStructureDto getStructure() {
        return null;
    }
}
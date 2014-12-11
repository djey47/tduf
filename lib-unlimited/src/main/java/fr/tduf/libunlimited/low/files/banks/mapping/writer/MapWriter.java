package fr.tduf.libunlimited.low.files.banks.mapping.writer;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.writer.GenericWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to persist {@link fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap} instance to specified Bnk1.map file.
 */
public class MapWriter extends GenericWriter<BankMap> {

    private MapWriter(BankMap bankMap) throws IOException {
        super(bankMap);
    }

    @Override
    public ByteArrayOutputStream write() {
        return super.write();
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
    protected String getStructureResource() {
        return "/files/structures/MAP4-map.json";
    }
}
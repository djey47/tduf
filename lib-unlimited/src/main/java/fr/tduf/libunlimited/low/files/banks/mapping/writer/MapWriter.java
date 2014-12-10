package fr.tduf.libunlimited.low.files.banks.mapping.writer;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.writer.GenericWriter;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to persist {@link fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap} instance to specified Bnk1.map file.
 */
public class MapWriter {

    private final GenericWriter writer;

    private MapWriter(BankMap bankMap) throws IOException {
        InputStream structureAsStream = getClass().getResourceAsStream("/files/structures/MAP4-map.json");
        FileStructureDto fileStructure = new ObjectMapper().readValue(structureAsStream, FileStructureDto.class);

        writer = GenericWriter.load(fileStructure);
    }

    /**
     * @param bankMap
     * @return
     */
    public static MapWriter load(BankMap bankMap) throws IOException {
        requireNonNull(bankMap, "Bank mapping is required");

        return new MapWriter(bankMap);
    }

    /**
     *
     * @return
     */
    public ByteArrayOutputStream write() {
        return writer.write();
    }

     GenericWriter getWriter() {
        return writer;
    }
}
package fr.tduf.libunlimited.low.files.research.writer;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Helper to write files whose file structure is available as separate asset.
 */
public class GenericWriter {

    private final FileStructureDto fileStructure;

    private final Map<String, String> store = new HashMap<>();

    private GenericWriter(FileStructureDto fileStructure) {
        this.fileStructure = fileStructure;
    }

    /**
     *
     * @param fileStructure
     * @return
     */
    public static GenericWriter load(FileStructureDto fileStructure) {
        requireNonNull(fileStructure, "Data structure is required");

        return new GenericWriter(fileStructure);
    }



    /**
     *
     * @return
     */
    public ByteArrayOutputStream write() {
        return null;
    }
}

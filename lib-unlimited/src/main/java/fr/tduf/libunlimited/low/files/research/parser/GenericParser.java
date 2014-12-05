package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.ByteArrayInputStream;

import static java.util.Objects.requireNonNull;

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
}

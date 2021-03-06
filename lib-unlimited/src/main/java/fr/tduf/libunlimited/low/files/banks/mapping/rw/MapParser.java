package fr.tduf.libunlimited.low.files.banks.mapping.rw;

import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to fetch entry list contained in Bnk1.map file.
 */
public class MapParser extends GenericParser<BankMap> {

    private MapParser(XByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Entry point for this parser.
     * @param mapData   : data to be parsed
     * @return a {@link MapParser} instance.
     */
    public static MapParser load(byte[] mapData) throws IOException {
        requireNonNull(mapData, "An array containing map contents is required");

        return new MapParser(new XByteArrayInputStream(mapData));
    }

    /**
     * Entry point for this parser.
     * @param mapFileName  : name of file containing data to be parsed
     * @return a {@link MapParser} instance.
     */
    public static MapParser load(String mapFileName) throws IOException {
        requireNonNull(mapFileName, "A file name is required");

        byte[] mapContents = Files.readAllBytes(Paths.get(mapFileName));

        return load(mapContents);
    }

    @Override
    protected BankMap generate() {
        BankMap bankMap = new BankMap();
        String errorMessageFormat = "Data store sub-entry not found: %s";
        getDataStore().getRepeatedValues("entry_list")
                .forEach(subDataStore -> {
                    long checksum = subDataStore.getInteger("file_name_hash")
                            .orElseThrow(() -> new IllegalStateException(String.format(errorMessageFormat, "file_name_hash")));
                    long size1 = subDataStore.getInteger("size_bytes_1")
                            .orElseThrow(() -> new IllegalStateException(String.format(errorMessageFormat, "size_bytes_1")));
                    long size2 = subDataStore.getInteger("size_bytes_2")
                            .orElseThrow(() -> new IllegalStateException(String.format(errorMessageFormat, "size_bytes_2")));

                    bankMap.addEntry(checksum, size1, size2);

                    if (bankMap.getEntrySeparator() == null) {
                        bankMap.setEntrySeparator(subDataStore.getRawValue("entry_end")
                                .orElseThrow(() -> new IllegalStateException(String.format(errorMessageFormat, "entry_end"))));
                    }
                });

        return bankMap;
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
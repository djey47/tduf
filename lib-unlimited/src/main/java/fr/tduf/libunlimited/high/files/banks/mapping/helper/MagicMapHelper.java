package fr.tduf.libunlimited.high.files.banks.mapping.helper;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper.*;
import static java.util.Objects.requireNonNull;

/**
 * Class providing methods to manage BNK mapping at high level (orchestrates low-level calls).
 */
public class MagicMapHelper {

    /**
     * Fixes specified magic map file for specified TDU banks location.
     * @param magicMapFile  : map file to be fixed
     * @param bankDirectory : directory where TDU bank files are located, e.g 'C:\Test Drive Unlimited\Euro\Bnk'
     * @return list of new file names.
     * @throws IOException
     */
    public static List<String> fixMagicMap(String magicMapFile, String bankDirectory) throws IOException {
        requireNonNull(magicMapFile, "Magic Map file is required.");
        requireNonNull(bankDirectory, "TDU Banks directory is required.");

        List<String> banks = parseBanks(bankDirectory);

        Map<Long, String> checksums = computeChecksums(banks);

        BankMap map = loadBankMap(magicMapFile);

        List<String> newFileNames = new ArrayList<>();
        findNewChecksums(map, checksums)

                .entrySet()

                .forEach((mapEntry) -> {
                    map.addMagicEntry(mapEntry.getKey());
                    newFileNames.add(mapEntry.getValue());
                });

        saveBankMap(map, magicMapFile);

        return newFileNames;
    }

    /**
     * Fixes magic map file at specified TDU banks location.
     * @param bankDirectory : directory where TDU bank files are located, e.g 'C:\Test Drive Unlimited\Euro\Bnk'
     * @return list of new file names.
     * @throws IOException
     */
    public static List<String> fixMagicMap(String bankDirectory) throws IOException {
        String magicMapFile = Paths.get(bankDirectory, MAPPING_FILE_NAME).toString();

        return fixMagicMap(magicMapFile, bankDirectory);
    }

    private static BankMap loadBankMap(String mapFile) throws IOException {
        return MapParser.load(mapFile).parse();
    }

    private static void saveBankMap(BankMap map, String outputMapFile) throws IOException {
        Path mapFilePath = Paths.get(outputMapFile);

        Files.write(mapFilePath, MapWriter.load(map).write().toByteArray());
    }
}
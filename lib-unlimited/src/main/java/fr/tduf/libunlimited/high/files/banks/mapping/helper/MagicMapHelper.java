package fr.tduf.libunlimited.high.files.banks.mapping.helper;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;

import java.io.IOException;
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
     */
    public static List<String> fixMagicMap(String magicMapFile, String bankDirectory) throws IOException {
        requireNonNull(magicMapFile, "Magic Map file is required.");
        requireNonNull(bankDirectory, "TDU Banks directory is required.");

        List<String> banks = parseBanks(bankDirectory);

        Map<Long, String> checksums = computeChecksums(banks);

        BankMap map = loadBankMap(magicMapFile);

        List<String> newFileNames = new ArrayList<>();
        findNewChecksums(map, checksums)
                .forEach((checksum, fileName) -> {
                    map.addMagicEntry(checksum);
                    newFileNames.add(fileName);
                });

        saveBankMap(map, magicMapFile);

        return newFileNames;
    }

    /**
     * Fixes magic map file at specified TDU banks location.
     * @param bankDirectory : directory where TDU bank files are located, e.g 'C:\Test Drive Unlimited\Euro\Bnk'
     * @return list of new file names.
     */
    public static List<String> fixMagicMap(String bankDirectory) throws IOException {
        String magicMapFile = Paths.get(bankDirectory, MAPPING_FILE_NAME).toString();

        return fixMagicMap(magicMapFile, bankDirectory);
    }

    /**
     * Converts given map file to magic map.
     * @param mapFile : map file to be magified
     */
    public static void toMagicMap(String mapFile) throws IOException {
        requireNonNull(mapFile, "Map file name is required.");

        BankMap bankMap = loadBankMap(mapFile);

        bankMap.magifyAll();

        saveBankMap(bankMap, mapFile);
    }
}

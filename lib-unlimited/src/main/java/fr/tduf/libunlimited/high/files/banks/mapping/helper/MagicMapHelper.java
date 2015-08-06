package fr.tduf.libunlimited.high.files.banks.mapping.helper;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper.*;

/**
 * Class providing methods to manage BNK mapping at high level (orchestrates low-level calls).
 */
public class MagicMapHelper {

    /**
     * Fixes specified magic map file for specified TDU banks location.
     * @param magicMapFile  : map file to be fixed
     * @param bankDirectory : directory where TDU bank files are located, e.g 'C:\Test Drive Unlimited\Euro\Bnk'
     * @throws IOException
     */
    public static void fixMagicMap(String magicMapFile, String bankDirectory) throws IOException {
        List<String> banks = parseBanks(bankDirectory);

        Map<Long, String> checksums = computeChecksums(banks);

        BankMap map = loadBankMap(magicMapFile);

        findNewChecksums(map, checksums)

                .keySet()

                .forEach(map::addMagicEntry);

        saveBankMap(map, magicMapFile);
    }

    /**
     * Fixes magic map file at specified TDU banks location.
     * @param bankDirectory : directory where TDU bank files are located, e.g 'C:\Test Drive Unlimited\Euro\Bnk'
     * @throws IOException
     */
    public static void fixMagicMap(String bankDirectory) throws IOException {
        String magicMapFile = Paths.get(bankDirectory, MAPPING_FILE_NAME).toString();

        fixMagicMap(magicMapFile, bankDirectory);
    }

    private static BankMap loadBankMap(String mapFile) throws IOException {
        return MapParser.load(mapFile).parse();
    }

    private static void saveBankMap(BankMap map, String outputMapFile) throws IOException {
        Path mapFilePath = Paths.get(outputMapFile);

        Files.write(mapFilePath, MapWriter.load(map).write().toByteArray());
    }
}
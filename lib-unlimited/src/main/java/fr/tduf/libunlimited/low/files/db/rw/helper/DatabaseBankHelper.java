package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.high.files.banks.BankSupport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Helper class providing methods to handle database bank files.
 */
public class DatabaseBankHelper {

    // TODO replace with a method using Locale values
    static final List<String> databaseFileNames = asList("DB.bnk", "DB_CH.bnk", "DB_FR.bnk", "DB_GE.bnk", "DB_IT.bnk", "DB_JA.bnk", "DB_KO.bnk", "DB_SP.bnk", "DB_US.bnk");

    /**
     * Extracts all TDU database files from specified directory to a temporary location.
     * @param databaseDirectory : directory containing ALL TDU database files.
     * @param bankSupport       : module instance to unpack/repack bnks
     * @return directory where extracted contents are located for further processing.
     */
    public static String unpackDatabaseFromDirectory(String databaseDirectory, BankSupport bankSupport) throws IOException {
        requireNonNull(databaseDirectory, "A database directory is required.");
        requireNonNull(bankSupport, "A module instance for bank support is required.");

        String tempDirectory = createTempDirectory();

        databaseFileNames.stream()

                .map((fileName) -> checkDatabaseFileExists(databaseDirectory, fileName))

                .forEach((validFileName) -> unpackDatabaseAndGroupFiles(validFileName, tempDirectory, bankSupport));

        return tempDirectory;
    }

    /**
     * Repacks all TDU database files from specified directory to target location.
     * @param databaseDirectory : directory containing ALL database files under extracted form.
     * @param targetDirectory   : directory where to place generated BNK files
     * @param bankSupport       : module instance to unpack/repack bnks
     */
    public static void repackDatabaseFromDirectory(String databaseDirectory, String targetDirectory, BankSupport bankSupport) {
        requireNonNull(databaseDirectory, "A database directory is required.");
        requireNonNull(targetDirectory, "A target directory is required.");
        requireNonNull(bankSupport, "A module instance for bank support is required.");

        databaseFileNames

                .forEach((targetBankFileName) -> rebuildFileStructureAndRepackDatabase(databaseDirectory, targetDirectory, targetBankFileName, bankSupport));
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("libUnlimited-databaseBank").toString();
    }

    private static void rebuildFileStructureAndRepackDatabase(String databaseDirectory, String targetDirectory, String bankFileName, BankSupport bankSupport) {
        try {
            String repackedDirectory = prepareFilesToBeRepacked(databaseDirectory, bankFileName);
            bankSupport.packAll(repackedDirectory, Paths.get(targetDirectory, bankFileName).toString());
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to repack database: " + databaseDirectory, ioe);
        }
    }

    // TODO see to move this method to BankSupport (implementation dependent)
    private static String prepareFilesToBeRepacked(String databaseDirectory, String targetBankFileName) throws IOException {
        String repackedDirectory = createTempDirectory();
        String originalBankFileName = "original-" + targetBankFileName;
        Files.copy(Paths.get(databaseDirectory, originalBankFileName), Paths.get(repackedDirectory, originalBankFileName));

        Files.createDirectory(Paths.get(repackedDirectory, targetBankFileName));

        Files.walk(Paths.get(databaseDirectory))

                .filter((filePath) -> {

                    if (targetBankFileName.equalsIgnoreCase("DB.bnk")) {
                        return filePath.toString().endsWith(".db");
                    }

                    String locale = targetBankFileName.substring(2, 4).toLowerCase();
                    return filePath.toString().endsWith("." + locale);
                })

                .forEach((filePath) -> {
                    Path targetPath = Paths.get(repackedDirectory, targetBankFileName, filePath.getFileName().toString());
                    try {
                        Files.copy(filePath, targetPath);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to recreate file structure: " + targetPath, ioe);
                    }
                });
        return repackedDirectory;
    }

    private static String checkDatabaseFileExists(String databaseDirectory, String databaseFileName) {
        Path databaseFilePath = Paths.get(databaseDirectory, databaseFileName);
        String fullFileName = databaseFilePath.toString();
        if (!Files.exists(databaseFilePath)) {
            throw new RuntimeException("Source database file does not exist.", new FileNotFoundException(fullFileName));
        }

        return fullFileName;
    }

    private static void unpackDatabaseAndGroupFiles(String databaseFileName, String targetDirectory, BankSupport bankSupport) {

        String shortDatabaseFileName = Paths.get(databaseFileName).getFileName().toString();

        try {
            String extractedDirectory = createTempDirectory();
            bankSupport.extractAll(databaseFileName, extractedDirectory);

            groupGeneratedFiles(extractedDirectory, targetDirectory);

            groupGeneratedFiles(Paths.get(extractedDirectory, shortDatabaseFileName).toString(), targetDirectory);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to unpack database bank: " + databaseFileName, ioe);
        }
    }

    private static void groupGeneratedFiles(String sourceDirectory, String targetDirectory) throws IOException {
        Files.walk(Paths.get(sourceDirectory))

                .filter((path) -> Files.isRegularFile(path))

                .forEach((originalBankFilePath) -> {
                    String shortOriginalBankFileName = originalBankFilePath.getFileName().toString();
                    try {
                        Files.move(originalBankFilePath, Paths.get(targetDirectory, shortOriginalBankFileName));
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to group file: " + shortOriginalBankFileName, ioe);
                    }
                });
    }
}
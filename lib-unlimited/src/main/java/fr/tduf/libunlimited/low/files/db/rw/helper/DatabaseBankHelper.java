package fr.tduf.libunlimited.low.files.db.rw.helper;

import com.sun.nio.file.ExtendedCopyOption;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Helper class providing methods to handle database bank files.
 */
public class DatabaseBankHelper {

    private static final String DATABASE_BANK_FILE_NAME = "DB.bnk";

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

        getDatabaseBankFileNames().stream()

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

        getDatabaseBankFileNames()

                .forEach((targetBankFileName) -> rebuildFileStructureAndRepackDatabase(databaseDirectory, targetDirectory, targetBankFileName, bankSupport));
    }

    static List<String> getDatabaseBankFileNames() {

        List<String> resourceBankFileNames = asList(DbResourceDto.Locale.values()).stream()

                .map((locale) -> "DB_" + locale.getCode().toUpperCase() + ".bnk")

                .collect(toList());

        List<String> databaseBankFileNames = new ArrayList<>(resourceBankFileNames);
        databaseBankFileNames.add(DATABASE_BANK_FILE_NAME);

        return databaseBankFileNames;
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("libUnlimited-databaseBank").toString();
    }

    private static void rebuildFileStructureAndRepackDatabase(String databaseDirectory, String targetDirectory, String bankFileName, BankSupport bankSupport) {
        try {
            String repackedDirectory = prepareFilesToBeRepacked(databaseDirectory, bankFileName, bankSupport);
            bankSupport.packAll(repackedDirectory, Paths.get(targetDirectory, bankFileName).toString());
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to repack database: " + databaseDirectory, ioe);
        }
    }

    private static String prepareFilesToBeRepacked(String databaseDirectory, String targetBankFileName, BankSupport bankSupport) throws IOException {

        List<Path> repackedPaths = Files.walk(Paths.get(databaseDirectory))

                .filter((filePath) -> {

                    if (targetBankFileName.equalsIgnoreCase(DATABASE_BANK_FILE_NAME)) {
                        return filePath.toString().endsWith(DatabaseReadWriteHelper.EXTENSION_DB_CONTENTS);
                    }

                    String locale = targetBankFileName.substring(3, 5).toLowerCase();
                    return filePath.toString().endsWith("." + locale);
                })

                .collect(toList());

        String repackedDirectory = createTempDirectory();
        bankSupport.prepareFilesToBeRepacked(databaseDirectory, repackedPaths, targetBankFileName, repackedDirectory);

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
                        Files.move(originalBankFilePath, Paths.get(targetDirectory, shortOriginalBankFileName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to group file: " + shortOriginalBankFileName, ioe);
                    }
                });
    }
}
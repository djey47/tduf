package fr.tduf.libunlimited.low.files.db.rw.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_DB_CONTENTS;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Helper class providing methods to handle database bank files.
 */
public class DatabaseBankHelper {

    private static final Class<DatabaseBankHelper> thisClass = DatabaseBankHelper.class;
    private static final String DATABASE_BANK_FILE_NAME = "DB.bnk";

    /**
     * Extracts all TDU database files from specified directory to a temporary location.
     * Optionally, prepares further processing by copying original bank files to targetDirectory.
     *
     * @param databaseDirectory : directory containing ALL TDU database files
     * @param targetDirectory   : directory where to copy original bank files, if provided (can be null)
     * @param bankSupport       : module instance to unpack/repack bnks
     * @return directory where extracted contents are located for further processing.
     */
    public static String unpackDatabaseFromDirectory(String databaseDirectory, String targetDirectory, BankSupport bankSupport) throws IOException {
        requireNonNull(databaseDirectory, "A database directory is required.");
        requireNonNull(bankSupport, "A module instance for bank support is required.");

        String tempDirectory = createTempDirectory();

        getDatabaseBankFileNames().stream()
                .parallel()
                .map((fileName) -> checkDatabaseFileExists(databaseDirectory, fileName))
                .forEach((validFileName) -> unpackDatabaseAndGroupFiles(validFileName, tempDirectory, bankSupport));

        ofNullable(targetDirectory).ifPresent((directory) -> {
            try {
                copyOriginalBankFilesToTargetDirectory(tempDirectory, directory);
            } catch (IOException ioe) {
                throw new RuntimeException("Unable to copy orginal bank files to target directory.", ioe);
            }
        });

        return tempDirectory;
    }

    /**
     * Repacks all TDU database files from specified directory to target location.
     * Optionally, copies original bank files from specified location to source cirectory (extractedDatabaseDirectory).
     * @param extractedDatabaseDirectory    : directory containing ALL database files under extracted form
     * @param targetDirectory               : directory where to place generated BNK files
     * @param originalBanksDirectory        : directory where original bank files are kept, if provided
     * @param bankSupport                   : module instance to unpack/repack bnks
     */
    public static void repackDatabaseFromDirectory(String extractedDatabaseDirectory, String targetDirectory, String originalBanksDirectory, BankSupport bankSupport) {
        requireNonNull(extractedDatabaseDirectory, "A database directory is required.");
        requireNonNull(targetDirectory, "A target directory is required.");
        requireNonNull(bankSupport, "A module instance for bank support is required.");

        ofNullable(originalBanksDirectory).ifPresent((directory) -> {
            try {
                copyOriginalBankFilesToTargetDirectory(directory, extractedDatabaseDirectory);
            } catch (IOException ioe) {
                throw new RuntimeException("Unable to copy orginal bank files to target directory.", ioe);
            }
        });

        getDatabaseBankFileNames()

                .stream()

                .parallel()

                .forEach((targetBankFileName) -> rebuildFileStructureAndRepackDatabase(extractedDatabaseDirectory, targetDirectory, targetBankFileName, bankSupport));
    }

    static List<String> getDatabaseBankFileNames() {

        List<String> resourceBankFileNames = Locale.valuesAsStream()

                .map((locale) -> "DB_" + locale.getCode().toUpperCase() + "." + EXTENSION_BANKS)

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
            prepareFilesToBeRepacked(databaseDirectory, bankFileName);

            Log.debug(thisClass.getSimpleName(), "databaseDirectory: " + databaseDirectory);
            Log.debug(thisClass.getSimpleName(), "targetDirectory: " + targetDirectory);
            Log.debug(thisClass.getSimpleName(), "bankFileName: " + bankFileName);

            bankSupport.packAll(Paths.get(databaseDirectory, bankFileName).toString(), Paths.get(targetDirectory, bankFileName).toString());
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to repack database: " + databaseDirectory, ioe);
        }
    }

    private static void prepareFilesToBeRepacked(String databaseDirectory, String targetBankFileName) throws IOException {

        Path databasePath = Paths.get(databaseDirectory);
        Path repackedBankPath = databasePath.resolve(targetBankFileName);
        Files.walk(databasePath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .forEach((filePath) -> {

                    String fileExtension = FilesHelper.getExtension(filePath.toString());
                    String hierarchy = null;
                    if (targetBankFileName.equalsIgnoreCase(DATABASE_BANK_FILE_NAME)
                            && EXTENSION_DB_CONTENTS.equalsIgnoreCase(fileExtension)) {
                        hierarchy = "4Build/PC/Euro/BDD/Db_encrypted";
                    } else {
                        String locale = targetBankFileName.substring(3, 5);
                        if (locale.equalsIgnoreCase(fileExtension)) {
                            hierarchy = "4Build/PC/Euro/BDD/Lang";
                        }
                    }
                    if (hierarchy != null) {
                        Path fullPath = repackedBankPath.resolve(hierarchy).resolve(filePath.getFileName());
                        try {
                            Files.createDirectories(fullPath.getParent());
                            Files.copy(filePath, fullPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        String originalBankFileName = GenuineBnkGateway.PREFIX_ORIGINAL_BANK_FILE + targetBankFileName;
        Path originalBankFilePath = databasePath.resolve(originalBankFileName);

        Files.createDirectories(repackedBankPath);
        Files.copy(originalBankFilePath, repackedBankPath.resolve(originalBankFileName), StandardCopyOption.REPLACE_EXISTING);
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
        try {
            String extractedDirectory = createTempDirectory();
            bankSupport.extractAll(databaseFileName, extractedDirectory);

            groupFiles(extractedDirectory, targetDirectory);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to unpack database bank: " + databaseFileName, ioe);
        }
    }

    private static void groupFiles(String sourceDirectory, String targetDirectory) throws IOException {
        try {
            Files.walk(Paths.get(sourceDirectory))

                    .filter((path) -> Files.isRegularFile(path))

                    .forEach((filePath) -> {
                        Path shortFileName = filePath.getFileName();
                        try {
                            Files.move(filePath, Paths.get(targetDirectory).resolve(shortFileName), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ioe) {
                            throw new RuntimeException("Unable to group file: " + shortFileName, ioe);
                        }
                    });
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void copyOriginalBankFilesToTargetDirectory(String sourceDirectory, String targetDirectory) throws IOException {
        Files.walk(Paths.get(sourceDirectory), 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((filePath) -> filePath.getFileName().toString().startsWith(GenuineBnkGateway.PREFIX_ORIGINAL_BANK_FILE))

                .filter((filePath) -> EXTENSION_BANKS.equalsIgnoreCase(FilesHelper.getExtension(filePath.toString())))

                .forEach((filePath) -> {
                    Path shortFileName = filePath.getFileName();
                    try {
                        FilesHelper.createDirectoryIfNotExists(targetDirectory);
                        Files.copy(filePath, Paths.get(targetDirectory).resolve(shortFileName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to copy original bank file: " + shortFileName + " to target directory.", ioe);
                    }
                });
    }
}

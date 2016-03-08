package fr.tduf.libunlimited.common.cache;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.createTempDirectory;

/**
 * Helper class to handle caching over database banks and JSON files (prevents from unpacking when unmodified banks)
 */
public class DatabaseBanksCacheHelper {

    private static final String THIS_CLASS_NAME = DatabaseBanksCacheHelper.class.getSimpleName();

    /**
     * Extracts TDU database banks only if necessary (DB.BNK file last modified time > 'last' file or 'last' file not found )
     * @param realDatabasePath  : TDU database path
     * @param bankSupport       : component to handle TDU banks
     * @return directory of freshest JSON database files
     * @throws IOException
     */
    // TODO replace lastModified time method by timestamp written in 'last' file
    public static String unpackDatabaseToJsonWithCacheSupport(Path realDatabasePath, BankSupport bankSupport) throws IOException {
        // TODO externalize directory and file names to constants
        final Path cachePath = realDatabasePath.resolve("json-cache");
        final String jsonDatabaseDirectory = cachePath.toString();
        long lastRepackTime = 0;
        if (Files.exists(cachePath)) {
            Path cacheTimestampFile = cachePath.resolve("last");

            if (Files.exists(cacheTimestampFile)) {
                lastRepackTime = cacheTimestampFile.toFile().lastModified();
            }
        } else {
            FilesHelper.createDirectoryIfNotExists(jsonDatabaseDirectory);
        }

        long databaseBankTime = realDatabasePath.resolve("DB.bnk").toFile().lastModified();
        if (databaseBankTime > lastRepackTime) {
            unpackDatabaseToJson(realDatabasePath.toString(), jsonDatabaseDirectory, bankSupport);
            updateCacheDirectory(realDatabasePath);
        }

        return jsonDatabaseDirectory;
    }

    /**
     *
     * @param realDatabasePath  : TDU database path
     * @param bankSupport       : component to handle TDU banks
     * @throws IOException
     */
    public static void repackDatabaseFromJsonWithCacheSupport(Path realDatabasePath, BankSupport bankSupport) throws IOException {
        final String jsonDatabaseDirectory = realDatabasePath.resolve("json-cache").toString();

        repackJsonDatabase(jsonDatabaseDirectory, realDatabasePath.toString(), bankSupport);

        updateCacheDirectory(realDatabasePath);
    }

    /**
     * Updates timestamp to match last date when database was repacked
     * @param realDatabasePath  : TDU database path
     * @throws IOException
     */
    // TODO replace lastModified time method by timestamp written in 'last' file
    public static void updateCacheDirectory(Path realDatabasePath) throws IOException {
        Path cacheDirectoryPath = realDatabasePath.resolve("json-cache");
        Path lastFilePath = cacheDirectoryPath.resolve("last");

        final File lastFile = lastFilePath.toFile();
        if(lastFile.exists()) {
            Log.debug(THIS_CLASS_NAME, "Database cache timestamp exists, last update: " + lastFile.lastModified());
            Files.delete(lastFilePath);
        }

        Files.createDirectories(cacheDirectoryPath);
        Files.createFile(lastFilePath);
        Log.debug(THIS_CLASS_NAME, "Database cache timestamp recreated at " + System.currentTimeMillis());
    }

    private static List<String> unpackDatabaseToJson(String databaseDirectory, String jsonDatabaseDirectory, BankSupport bankSupport) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Unpacking TDU database: " + databaseDirectory);

        String unpackedDatabaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, Optional.of(jsonDatabaseDirectory), bankSupport);

        Log.info(THIS_CLASS_NAME, "->Unpacked TDU database directory: " + unpackedDatabaseDirectory);

        // TODO do not ignore integrity errors!
        List<String> jsonFiles = JsonGateway.dump(unpackedDatabaseDirectory, jsonDatabaseDirectory, new ArrayList<>(), new LinkedHashSet<>());

        Log.info(THIS_CLASS_NAME, "->Prepared JSON database directory: " + jsonDatabaseDirectory);

        return jsonFiles;
    }

    private static void repackJsonDatabase(String jsonDatabaseDirectory, String databaseDirectory, BankSupport bankSupport) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Converting JSON database: " + jsonDatabaseDirectory);

        String extractedDatabaseDirectory = createTempDirectory();

        JsonGateway.gen(jsonDatabaseDirectory, extractedDatabaseDirectory, new ArrayList<>());

        Log.info(THIS_CLASS_NAME, "->Converted TDU database directory: " + extractedDatabaseDirectory);

        DatabaseBankHelper.repackDatabaseFromDirectory(extractedDatabaseDirectory, databaseDirectory, Optional.of(jsonDatabaseDirectory), bankSupport);

        Log.info(THIS_CLASS_NAME, "->Repacked database: " + extractedDatabaseDirectory + " to " + databaseDirectory);
    }
}

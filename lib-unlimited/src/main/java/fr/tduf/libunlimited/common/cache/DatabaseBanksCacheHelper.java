package fr.tduf.libunlimited.common.cache;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.createTempDirectory;
import static java.lang.Long.valueOf;
import static java.util.Collections.singletonList;

/**
 * Helper class to handle caching over database banks and JSON files (prevents from unpacking when unmodified banks)
 */
public class DatabaseBanksCacheHelper {

    private static final String THIS_CLASS_NAME = DatabaseBanksCacheHelper.class.getSimpleName();
    private static final String DIRECTORY_JSON_CACHE = "json-cache";
    private static final String FILE_LAST_MODIFIED = "last";

    /**
     * Extracts TDU database banks only if necessary (DB.BNK file last modified time > 'last' file or 'last' file not found )
     * @param realDatabasePath  : TDU database path
     * @param bankSupport       : component to handle TDU banks
     * @return directory of freshest JSON database files
     * @throws IOException
     */
    public static String unpackDatabaseToJsonWithCacheSupport(Path realDatabasePath, BankSupport bankSupport) throws IOException {
        final Path cachePath = resolveCachePath(realDatabasePath);
        final String jsonDatabaseDirectory = cachePath.toString();
        long lastRepackTime = 0;
        if (Files.exists(cachePath)) {
            Path cacheTimestampPath = resolveLastFilePath(realDatabasePath);

            if (Files.exists(cacheTimestampPath)) {
                lastRepackTime = readTimestamp(cacheTimestampPath);
            }
        } else {
            FilesHelper.createDirectoryIfNotExists(jsonDatabaseDirectory);
        }

        long databaseBankTime = realDatabasePath.resolve("DB.bnk").toFile().lastModified();
        if (databaseBankTime > lastRepackTime) {
            unpackDatabaseToJson(realDatabasePath.toString(), jsonDatabaseDirectory, bankSupport);
            updateCacheTimestamp(realDatabasePath);
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
        final String jsonDatabaseDirectory = resolveCachePath(realDatabasePath).toString();

        repackJsonDatabase(jsonDatabaseDirectory, realDatabasePath.toString(), bankSupport);

        updateCacheTimestamp(realDatabasePath);
    }

    /**
     * @param realDatabasePath  : TDU database path
     * @throws IOException
     */
    // TODO test and use
    public static void clearCache(Path realDatabasePath) throws IOException {
        Path cachePath = resolveCachePath(realDatabasePath);
        final File cacheDirectory = cachePath.toFile();
        if (cacheDirectory.exists()) {
            FileUtils.deleteDirectory(cacheDirectory);
        }
    }

    /**
     * @return path of JSON database cache
     */
    public static Path resolveCachePath(Path databasePath) {
        return databasePath.resolve(DIRECTORY_JSON_CACHE);
    }

    static void updateCacheTimestamp(Path realDatabasePath) throws IOException {
        Path cachePath = resolveCachePath(realDatabasePath);
        Path lastFilePath = resolveLastFilePath(realDatabasePath);

        final File lastFile = lastFilePath.toFile();
        if(lastFile.exists()) {
            final long timestamp = readTimestamp(lastFilePath);
            Log.debug(THIS_CLASS_NAME, "Database cache timestamp exists, last update at " + timestamp);
            Files.delete(lastFilePath);
        }

        Files.createDirectories(cachePath);
        Files.createFile(lastFilePath);
        final Long timestamp = updateTimestamp(lastFilePath);
        Log.debug(THIS_CLASS_NAME, "Database cache timestamp (re)created at " + timestamp);
    }

    private static long readTimestamp(Path cacheTimestampPath) throws IOException {
        final List<String> contents = Files.readAllLines(cacheTimestampPath);
        return contents.isEmpty() ?
                0 : valueOf(contents.get(0));
    }

    private static long updateTimestamp(Path cacheTimestampPath) throws IOException {
        final Long timestamp = valueOf(System.currentTimeMillis());
        Files.write(cacheTimestampPath, singletonList(timestamp.toString()));
        return timestamp;
    }

    private static Path resolveLastFilePath(Path databasePath) {
        return resolveCachePath(databasePath).resolve(FILE_LAST_MODIFIED);
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

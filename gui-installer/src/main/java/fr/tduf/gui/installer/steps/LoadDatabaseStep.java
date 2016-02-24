package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Unpacks TDU database and loads JSON result.
 */
public class LoadDatabaseStep extends GenericStep {

    private static final String THIS_CLASS_NAME = LoadDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");

        String jsonDatabaseDirectory = handleCacheDirectory();

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);

        setDatabaseContext(new DatabaseContext(allTopicObjects, jsonDatabaseDirectory));
    }

    String handleCacheDirectory() throws IOException {

        Path realDatabasePath = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory());

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
            unpackDatabaseToJson(jsonDatabaseDirectory);
        }

        return jsonDatabaseDirectory;
    }

    List<String> unpackDatabaseToJson(String jsonDatabaseDirectory) throws IOException {
        String databaseDirectory = getInstallerConfiguration().resolveDatabaseDirectory();

        Log.info(THIS_CLASS_NAME, "->Unpacking TDU database: " + databaseDirectory);

        String unpackedDatabaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, Optional.of(jsonDatabaseDirectory), getInstallerConfiguration().getBankSupport());

        Log.info(THIS_CLASS_NAME, "->Unpacked TDU database directory: " + unpackedDatabaseDirectory);

        // TODO do not ignore integrity errors!
        List<String> jsonFiles = JsonGateway.dump(unpackedDatabaseDirectory, jsonDatabaseDirectory, new ArrayList<>(), new LinkedHashSet<>());

        Log.info(THIS_CLASS_NAME, "->Prepared JSON database directory: " + jsonDatabaseDirectory);

        return jsonFiles;
    }
}

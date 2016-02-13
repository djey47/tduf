package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Files;
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

        String jsonDatabaseDirectory = Files.createTempDirectory("guiInstaller").toString();

        unpackDatabaseToJson(jsonDatabaseDirectory);

        // TODO check if all files have been created

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);

        setDatabaseContext(new DatabaseContext(allTopicObjects, jsonDatabaseDirectory));
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

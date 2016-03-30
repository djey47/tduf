package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

public class SaveDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = SaveDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        final String jsonDatabaseDirectory = getDatabaseContext().getJsonDatabaseDirectory();
        final String databaseDirectory = getInstallerConfiguration().resolveDatabaseDirectory();

        Log.info(THIS_CLASS_NAME, "->Saving database to " + jsonDatabaseDirectory) ;
        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(getDatabaseContext().getTopicObjects(), jsonDatabaseDirectory);

        Log.info(THIS_CLASS_NAME, "->Repacking database to " + databaseDirectory) ;
        DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(Paths.get(databaseDirectory), getInstallerConfiguration().getBankSupport());
    }
}

package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Unpacks TDU database and loads JSON result.
 */
public class LoadDatabaseStep extends GenericStep {

    @Override
    protected void perform() throws IOException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");

        Path realDatabasePath = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory());
        String jsonDatabaseDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(realDatabasePath, getInstallerConfiguration().getBankSupport());

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);

        setDatabaseContext(new DatabaseContext(allTopicObjects, jsonDatabaseDirectory));
    }
}

package fr.tduf.gui.common.services;

import fr.tduf.gui.common.DisplayConstants;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Background service to load and check TDU database from banks directory.
 */
public class DatabaseChecker extends AbstractDatabaseService {

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                List<DbDto> databaseObjects = loadedDatabaseObjects.getValue();
                if (databaseObjects == null) {
                    updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "1/3"));
                    String jsonDirectory = resolveJsonDatabaseLocationAndUnpack(databaseLocation.get(), bankSupport.get());

                    updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "2/3"));
                    databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                    jsonDatabaseLocation.setValue(jsonDirectory);
                } else {
                    jsonDatabaseLocation.setValue(databaseLocation.get());
                }

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "3/3"));
                final DatabaseIntegrityChecker checkerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, databaseObjects);
                final Set<IntegrityError> integrityErrorsFromExtensiveCheck = checkerComponent.checkAllContentsObjects();

                loadedDatabaseObjects.setValue(databaseObjects);
                integrityErrors.setValue(integrityErrorsFromExtensiveCheck);

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_DONE, integrityErrorsFromExtensiveCheck.size()));

                return null;
            }
        };
    }

    private static String resolveJsonDatabaseLocationAndUnpack(String realDatabaseLocation, BankSupport bankSupport) throws
            IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        return BankHelper.isPackedDatabase(realDatabasePath) ?
                DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(realDatabasePath, bankSupport) :
                realDatabaseLocation;
    }
}

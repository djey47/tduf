package fr.tduf.gui.common.services;

import fr.tduf.gui.common.DisplayConstants;
import fr.tduf.gui.common.services.tasks.GenericServiceTask;
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

import static fr.tduf.gui.common.DisplayConstants.STATUS_CHECK_FAILED;
import static fr.tduf.gui.common.DisplayConstants.STATUS_FMT_CHECK_DONE;

/**
 * Background service to load and check TDU database from banks directory.
 */
public class DatabaseChecker extends AbstractDatabaseService {

    /**
     * Created for advanced features and easier testing
     */
    class CheckerTask extends GenericServiceTask<Void> {
        @Override
        protected Void call() throws Exception {
            Set<IntegrityError> integrityErrorsFromExtensiveCheck;

            try {
                if (loadedDatabaseObjects.getValue() == null) {
                    updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "1/3"));
                    String jsonDirectory = resolveJsonDatabaseLocationAndUnpack(databaseLocation.get(), bankSupport.get());
                    jsonDatabaseLocation.setValue(jsonDirectory);

                    updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "2/3"));
                    List<DbDto> fullDatabase = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseLocation.getValue());
                    loadedDatabaseObjects.setValue(fullDatabase);
                } else {
                    jsonDatabaseLocation.setValue(databaseLocation.get());
                }

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "3/3"));
                final DatabaseIntegrityChecker checkerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, loadedDatabaseObjects.getValue());
                integrityErrorsFromExtensiveCheck = checkerComponent.checkAllContentsObjects();

                integrityErrors.setValue(integrityErrorsFromExtensiveCheck);
            } catch (Exception e) {
                updateMessage(STATUS_CHECK_FAILED);
                throw e;
            }

            updateMessage(String.format(STATUS_FMT_CHECK_DONE, integrityErrorsFromExtensiveCheck.size()));

            return null;
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new CheckerTask();
    }

    private static String resolveJsonDatabaseLocationAndUnpack(String realDatabaseLocation, BankSupport bankSupport) throws
            IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        return BankHelper.isPackedDatabase(realDatabasePath) ?
                DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(realDatabasePath, bankSupport) :
                realDatabaseLocation;
    }
}

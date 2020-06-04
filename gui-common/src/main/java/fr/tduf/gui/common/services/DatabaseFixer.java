package fr.tduf.gui.common.services;

import fr.tduf.gui.common.DisplayConstants;
import fr.tduf.gui.common.services.tasks.GenericServiceTask;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Background service to fix and save TDU database from banks directory.
 */
public class DatabaseFixer extends AbstractDatabaseService {

    /**
     * Created for advanced features and easier testing
     */
    class FixerTask extends GenericServiceTask<Void> {
        @Override
        protected Void call() throws Exception {
            // TODO [2.0] handle errors like Loader/Saver services
            final Path realDatabasePath = Paths.get(requireNonNull(databaseLocation.get(), "Database location is required."));
            final List<DbDto> databaseObjects = requireNonNull(loadedDatabaseObjects.get(), "Loaded database objects are required.");
            final String jsonDirectory = requireNonNull(jsonDatabaseLocation.get(), "JSON database location is required.");

            updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "1/3"));
            final DatabaseIntegrityFixer fixerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
            Set<IntegrityError> remainingErrors = fixerComponent.fixAllContentsObjects(integrityErrors.get());

            updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "2/3"));
            DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects, jsonDirectory);

            updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "3/3"));
            repackIfNecessary(realDatabasePath.toString(), bankSupport.get());

            integrityErrors.setValue(remainingErrors);

            updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_DONE, remainingErrors.size()));

            return null;
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new FixerTask();
    }

    private static void repackIfNecessary(String realDatabaseLocation, BankSupport bankSupport) throws
            IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        if (BankHelper.isPackedDatabase(realDatabasePath)) {
            DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(realDatabasePath, bankSupport);
        }
    }
}

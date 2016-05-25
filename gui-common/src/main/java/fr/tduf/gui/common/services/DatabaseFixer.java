package fr.tduf.gui.common.services;

import fr.tduf.gui.common.DisplayConstants;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Background service to fix and save TDU database from banks directory.
 */
public class DatabaseFixer extends Service<Set<IntegrityError>> {
    private StringProperty databaseLocation = new SimpleStringProperty();
    private ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();
    private ObjectProperty<Set<IntegrityError>> integrityErrors = new SimpleObjectProperty<>();

    @Override
    protected Task<Set<IntegrityError>> createTask() {
        return new Task<Set<IntegrityError>>() {
            @Override
            protected Set<IntegrityError> call() throws Exception {

                // TODO get loaded database objects from check service instead of reloading all
                updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "1/5"));
                Path realDatabasePath = Paths.get(databaseLocation.get());
                final String jsonDirectory = resolveJsonDatabaseLocationAndUnpack(realDatabasePath.toString(), bankSupport.get());

                updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "2/5"));
                final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "3/5"));
                final DatabaseIntegrityFixer fixerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
                Set<IntegrityError> remainingErrors = fixerComponent.fixAllContentsObjects(integrityErrors.get());

                updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "4/5"));
                DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects, jsonDirectory);

                updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_IN_PROGRESS, "5/5"));
                repackIfNecessary(realDatabasePath.toString(), bankSupport.get());

                updateMessage(String.format(DisplayConstants.STATUS_FMT_FIX_DONE, remainingErrors.size()));

                return remainingErrors;
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

    private static void repackIfNecessary(String realDatabaseLocation, BankSupport bankSupport) throws
            IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        if (BankHelper.isPackedDatabase(realDatabasePath)) {
            DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(realDatabasePath, bankSupport);
        }
    }

    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public ObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }

    public ObjectProperty<Set<IntegrityError>> integrityErrorsProperty() {
        return integrityErrors;
    }
}

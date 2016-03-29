package fr.tduf.gui.installer.services;

import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
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

                updateMessage("Performing database fix 1/5, please wait...");
                Path realDatabasePath = Paths.get(databaseLocation.get());
                final String jsonDirectory =  DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(realDatabasePath, bankSupport.get());

                updateMessage("Performing database fix 2/5, please wait...");
                final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                updateMessage("Performing database fix 3/5, please wait...");
                final DatabaseIntegrityFixer fixerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
                Set<IntegrityError> remainingErrors = fixerComponent.fixAllContentsObjects(integrityErrors.get());

                updateMessage("Performing database fix 4/5, please wait...");
                DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects, jsonDirectory);

                updateMessage("Performing database fix 5/5, please wait...");
                DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(realDatabasePath, bankSupport.get());

                updateMessage("Done fixing database, " + remainingErrors.size() + " error(s) remaining.");

                return remainingErrors;
            }
        };
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

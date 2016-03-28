package fr.tduf.gui.installer.services;

import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Background service to load and check TDU database from banks directory.
 */
public class DatabaseChecker extends Service<Set<IntegrityError>> {
    private StringProperty databaseLocation = new SimpleStringProperty();
    private ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();

    @Override
    protected Task<Set<IntegrityError>> createTask() {
        return new Task<Set<IntegrityError>>() {
            @Override
            protected Set<IntegrityError> call() throws Exception {

                updateMessage("Performing database check 1/3, please wait...");
                String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseLocation.get()), bankSupport.get());

                updateMessage("Performing database check 2/3, please wait...");
                final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                updateMessage("Performing database check 3/3, please wait...");
                final DatabaseIntegrityChecker checkerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, databaseObjects);
                final Set<IntegrityError> integrityErrorsFromExtensiveCheck = checkerComponent.checkAllContentsObjects();

                updateMessage("Done checking database, " + integrityErrorsFromExtensiveCheck.size() + " error(s).");

                return integrityErrorsFromExtensiveCheck;
            }
        };
    }
    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public ObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }
}

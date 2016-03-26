package fr.tduf.gui.installer.services;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.nio.file.Files;
import java.util.*;

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

                updateMessage("Performing database fix 1/7, please wait...");

                String unpackedDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseLocation.get(), Optional.empty(), bankSupport.get());

                updateMessage("Performing database fix 2/7, please wait...");

                final String jsonDirectory = Files.createTempDirectory("guiInstaller-databaseFix").toString();
                JsonGateway.dump(unpackedDirectory, jsonDirectory, new ArrayList<>(), new HashSet<>());

                updateMessage("Performing database fix 3/7, please wait...");

                final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                updateMessage("Performing database fix 4/7, please wait...");

                final DatabaseIntegrityFixer fixerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
                Set<IntegrityError> remainingErrors = fixerComponent.fixAllContentsObjects(integrityErrors.get());

                updateMessage("Performing database fix 5/7, please wait...");

                DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects, jsonDirectory);

                updateMessage("Performing database fix 6/7, please wait...");

                JsonGateway.gen(jsonDirectory, unpackedDirectory, new ArrayList<>());

                updateMessage("Performing database fix 7/7, please wait...");

                DatabaseBankHelper.repackDatabaseFromDirectory(unpackedDirectory, databaseLocation.get(), Optional.empty(), bankSupport.get());

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

package fr.tduf.gui.installer.services;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
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

/**
 * Background service to load TDU database from banks directory.
 */
public class DatabaseLoader extends Service<DatabaseContext> {
    private StringProperty databaseLocation = new SimpleStringProperty();
    private ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();

    @Override
    protected Task<DatabaseContext> createTask() {
        return new Task<DatabaseContext>() {
            @Override
            protected DatabaseContext call() throws Exception {

                updateMessage("Performing database load 1/2, please wait...");
                String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseLocation.get()), bankSupport.get());

                updateMessage("Performing database load 2/2, please wait...");
                final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                updateMessage("Done loading database.");

                return new DatabaseContext(databaseObjects, jsonDirectory);
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

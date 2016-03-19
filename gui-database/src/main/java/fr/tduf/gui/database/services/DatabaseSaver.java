package fr.tduf.gui.database.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.common.helper.BankHelper;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Background service to save database objects to banks or json files
 */
public class DatabaseSaver extends Service<String> {
    private static final String THIS_CLASS_NAME = DatabaseSaver.class.getSimpleName();

    private StringProperty databaseLocation = new SimpleStringProperty();
    private ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();
    private ObjectProperty<List<DbDto>> databaseObjects = new SimpleObjectProperty<>();

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {

                updateMessage("Saving database, please wait...");

                String jsonDatabaseLocation = resolveJsonDatabaseLocation(databaseLocation.get());
                Log.debug(THIS_CLASS_NAME, "jsonDatabaseLocation=" + jsonDatabaseLocation);

                DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects.get(), jsonDatabaseLocation);

                if(!Paths.get(databaseLocation.get()).toAbsolutePath().equals(Paths.get(jsonDatabaseLocation).toAbsolutePath())) {
                    DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(Paths.get(databaseLocation.get()), bankSupport.get());
                }

                updateMessage("Saved database: " + databaseLocation.get());

                return databaseLocation.get();
            }
        };
    }

    private static String resolveJsonDatabaseLocation(String realDatabaseLocation) throws IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        return BankHelper.isPackedDatabase(realDatabasePath) ?
                DatabaseBanksCacheHelper.resolveCachePath(realDatabasePath).toString() :
                realDatabaseLocation;
    }

    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public ObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }

    public ObjectProperty<List<DbDto>> databaseObjectsProperty() {
        return databaseObjects;
    }
}

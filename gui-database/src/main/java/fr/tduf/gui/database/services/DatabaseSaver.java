package fr.tduf.gui.database.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
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

import static fr.tduf.gui.database.common.DisplayConstants.STATUS_FORMAT_SAVED_DATABASE;
import static fr.tduf.gui.database.common.DisplayConstants.STATUS_SAVING;

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

                updateMessage(STATUS_SAVING);

                String databasePath = databaseLocation.get();
                String jsonDatabaseLocation = resolveJsonDatabaseLocation(databasePath);
                Log.debug(THIS_CLASS_NAME, "jsonDatabaseLocation=" + jsonDatabaseLocation);

                DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects.get(), jsonDatabaseLocation);

                if(!Paths.get(databasePath).toAbsolutePath().equals(Paths.get(jsonDatabaseLocation).toAbsolutePath())) {
                    DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(Paths.get(databasePath), bankSupport.get());
                }

                updateMessage(String.format(STATUS_FORMAT_SAVED_DATABASE, databasePath));

                return databasePath;
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

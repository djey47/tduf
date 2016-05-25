package fr.tduf.gui.database.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
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
 * Background service to load database objects from banks or json files
 */
public class DatabaseLoader extends Service<List<DbDto>> {
    private static final String THIS_CLASS_NAME = DatabaseLoader.class.getSimpleName();

    private StringProperty databaseLocation = new SimpleStringProperty();
    private SimpleObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();

    @Override
    protected Task<List<DbDto>> createTask() {
        return new Task<List<DbDto>>() {
            @Override
            protected List<DbDto> call() throws Exception {

                updateMessage("Loading database, please wait...");

                String jsonDatabaseLocation = resolveJsonDatabaseLocationAndUnpack(databaseLocation.get(), bankSupport.get());

                Log.debug(THIS_CLASS_NAME, "jsonDatabaseLocation=" + jsonDatabaseLocation);

                List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseLocation);

                if (databaseObjects.isEmpty()) {
                    throw new IllegalArgumentException("Invalid database location: " + databaseLocation.get());
                }

                updateMessage("Loaded database: " + databaseLocation.get());

                return databaseObjects;
            }
        };
    }

    private static String resolveJsonDatabaseLocationAndUnpack(String realDatabaseLocation, BankSupport bankSupport) throws IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        return BankHelper.isPackedDatabase(realDatabasePath) ?
                DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(realDatabasePath, bankSupport) :
                realDatabaseLocation;
    }

    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public SimpleObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }
}

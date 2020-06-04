package fr.tduf.gui.database.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.services.tasks.GenericServiceTask;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
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

import static fr.tduf.gui.database.common.DisplayConstants.*;

/**
 * Background service to load database objects from banks or json files
 */
public class DatabaseLoader extends Service<List<DbDto>> {
    private static final String THIS_CLASS_NAME = DatabaseLoader.class.getSimpleName();

    private final StringProperty databaseLocation = new SimpleStringProperty();
    private final SimpleObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();

    /**
     * Created for advanced features and easier testing
     */
    class LoaderTask extends GenericServiceTask<List<DbDto>> {
        @Override
        protected List<DbDto> call() throws Exception {
            updateMessage(STATUS_LOADING_DATA);

            String realDatabaseLocation = databaseLocation.get();
            List<DbDto> databaseObjects;

            try {
                String jsonDatabaseLocation = resolveJsonDatabaseLocationAndUnpack(realDatabaseLocation, bankSupport.get());

                Log.debug(THIS_CLASS_NAME, "jsonDatabaseLocation=" + jsonDatabaseLocation);

                databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseLocation);

                if (databaseObjects.isEmpty()) {
                    throw new IllegalArgumentException("Invalid database location: " + realDatabaseLocation);
                }
            } catch (Exception e) {
                updateMessage(String.format(STATUS_FORMAT_NOT_LOADED_DATABASE, realDatabaseLocation));
                throw e;
            }

            updateMessage(String.format(STATUS_FORMAT_LOADED_DATABASE, realDatabaseLocation));

            return databaseObjects;
        }
    }

    /**
     * Exists only for overriding/mocking purposes. Prefer using it.
     * @return same as parent.getValue()
     */
    public List<DbDto> fetchValue() {
        return getValue();
    }

    @Override
    protected Task<List<DbDto>> createTask() {
        return new LoaderTask();
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

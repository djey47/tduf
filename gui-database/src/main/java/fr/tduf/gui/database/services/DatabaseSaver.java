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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.gui.database.common.DisplayConstants.*;
import static java.util.Objects.requireNonNull;

/**
 * Background service to save database objects to banks or json files
 */
public class DatabaseSaver extends Service<String> {
    private static final String THIS_CLASS_NAME = DatabaseSaver.class.getSimpleName();

    private final StringProperty databaseLocation = new SimpleStringProperty();
    private final ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();
    private final ObjectProperty<List<DbDto>> databaseObjects = new SimpleObjectProperty<>();

    /**
     * Created for advanced features and easier testing
     */
    class SaverTask extends Task<String> {
        private final List<String> messageHistory = new ArrayList<>();

        @Override
        protected String call() throws Exception {
            updateMessage(STATUS_SAVING);

            String databasePath = databaseLocation.get();

            try {
                String jsonDatabaseLocation = resolveJsonDatabaseLocation(databasePath);
                Log.debug(THIS_CLASS_NAME, "jsonDatabaseLocation=" + jsonDatabaseLocation);

                DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects.get(), jsonDatabaseLocation);

                if (!Paths.get(databasePath).toAbsolutePath().equals(Paths.get(jsonDatabaseLocation).toAbsolutePath())) {
                    DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(Paths.get(databasePath), bankSupport.get());
                }
            } catch (RuntimeException re) {
                updateMessage(String.format(STATUS_FORMAT_NOT_SAVED_DATABASE, databasePath));
                throw re;
            }

            updateMessage(String.format(STATUS_FORMAT_SAVED_DATABASE, databasePath));

            return databasePath;
        }

        @Override
        protected void updateMessage(String s) {
            requireNonNull(s, "Message cannot be null");

            messageHistory.add(s);
            super.updateMessage(s);
        }

        protected List<String> getMessageHistory() {
            return messageHistory;
        }
    }

    @Override
    protected Task<String> createTask() {
        return new SaverTask();
    }

    private static String resolveJsonDatabaseLocation(String realDatabaseLocation) {
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

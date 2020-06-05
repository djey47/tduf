package fr.tduf.gui.installer.services;

import fr.tduf.gui.common.services.tasks.GenericServiceTask;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.services.tasks.TaskType;
import fr.tduf.gui.installer.steps.GenericStep;
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
public class InstallerDatabaseLoader extends Service<DatabaseContext> {
    private final StringProperty databaseLocation = new SimpleStringProperty();
    private final ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();
    private final ObjectProperty<TaskType> objective = new SimpleObjectProperty<>(TaskType.INSTALL);

    /**
     * Created for advanced features and easier testing
     */
    class InstallerLoaderTask extends GenericServiceTask<DatabaseContext> {
        @Override
        protected DatabaseContext call() throws Exception {
            updateMessage(String.format(DisplayConstants.STATUS_FMT_LOAD_IN_PROGRESS, "1/2"));
            String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseLocation.get()), bankSupport.get());

            updateMessage(String.format(DisplayConstants.STATUS_FMT_LOAD_IN_PROGRESS, "2/2"));
            final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

            if (databaseObjects.isEmpty()) {
                throw new StepException(GenericStep.StepType.LOAD_DATABASE, "Database could not be read", new IllegalArgumentException("Invalid location: " + databaseLocation.get()));
            }

            updateMessage(DisplayConstants.STATUS_LOAD_DONE);

            return new DatabaseContext(databaseObjects, jsonDirectory);
        }
    }

    @Override
    protected Task<DatabaseContext> createTask() {
        return new InstallerLoaderTask();
    }

    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public ObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }

    public ObjectProperty<TaskType> objectiveProperty() {
        return objective;
    }
}

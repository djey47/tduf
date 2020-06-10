package fr.tduf.gui.database.controllers.main;

import fr.tduf.gui.common.controllers.helper.DatabaseOpsHelper;
import fr.tduf.gui.common.services.DatabaseChecker;
import fr.tduf.gui.common.services.DatabaseFixer;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.gui.database.services.DatabaseSaver;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;

import java.util.List;
import java.util.Set;

import static fr.tduf.gui.common.helper.MessagesHelper.getServiceErrorMessage;
import static fr.tduf.gui.database.common.DisplayConstants.TITLE_APPLICATION;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Specialized controller to manage services.
 */

public class MainStageServicesController extends AbstractMainStageSubController {
    private final BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private final DatabaseSaver databaseSaver = new DatabaseSaver();
    private final DatabaseChecker databaseChecker = new DatabaseChecker();

    @SuppressWarnings("FieldMayBeFinal")
    private DatabaseFixer databaseFixer;

    @SuppressWarnings("FieldMayBeFinal")
    DatabaseLoader databaseLoader;

    MainStageServicesController(MainStageController mainStageController) {
        super(mainStageController);

        databaseLoader = new DatabaseLoader();
        databaseFixer = new DatabaseFixer();
    }

    void initServicePropertiesAndListeners() {
        runningServiceProperty.bind(databaseLoader.runningProperty()
                .or(databaseSaver.runningProperty())
                .or(databaseChecker.runningProperty())
                .or(databaseFixer.runningProperty()));

        databaseLoader.stateProperty().addListener(getLoaderStateChangeListener());

        databaseSaver.stateProperty().addListener(getSaverStateChangeListener());

        databaseChecker.stateProperty().addListener(getCheckerStateChangeListener());

        databaseFixer.stateProperty().addListener(getFixerStateChangeListener());
    }

    void handleDatabaseLoaderSuccess() {
        List<DbDto> loadedDatabaseObjects = databaseLoader.fetchValue();
        if (loadedDatabaseObjects.isEmpty()) {
            return;
        }

        List<DbDto> databaseObjects = getDatabaseObjects();
        databaseObjects.clear();
        databaseObjects.addAll(loadedDatabaseObjects);

        initAfterDatabaseLoading();
    }

    void handleDatabaseSaverSuccess() {
        modifiedProperty().setValue(false);

        if(!getApplicationConfiguration().isEditorPluginsEnabled()) {
            return;
        }
        getPluginHandler().triggerOnSaveForAllPLugins();
    }

    private ChangeListener<Worker.State> getFixerStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> remainingErrors = databaseFixer.integrityErrorsProperty().get();
                if (remainingErrors.isEmpty()) {
                    notifyActionTermination(INFORMATION, fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_OK, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_ZERO_ERROR_AFTER_FIX);
                } else {
                    notifyActionTermination(WARNING, fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_REMAINING_ERRORS);
                }
                refreshAllViewComponents();
            } else if (FAILED == newState) {
                notifyActionTermination(ERROR, fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO, getServiceErrorMessage(databaseFixer));
            }
        };
    }

    private ChangeListener<Worker.State> getCheckerStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> integrityErrors = databaseChecker.integrityErrorsProperty().get();
                if (integrityErrors.isEmpty()) {
                    notifyActionTermination(INFORMATION, fr.tduf.gui.common.DisplayConstants.TITLE_SUB_CHECK_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_CHECK_OK, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_ZERO_ERROR);
                } else if (DatabaseOpsHelper.displayCheckResultDialog(integrityErrors, getWindow(), TITLE_APPLICATION)) {
                    fixDatabase();
                }
            } else if (FAILED == newState) {
                notifyActionTermination(ERROR, fr.tduf.gui.common.DisplayConstants.TITLE_SUB_CHECK_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_CHECK_KO, getServiceErrorMessage(databaseChecker));
            }
        };
    }

    private ChangeListener<Worker.State> getSaverStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                handleDatabaseSaverSuccess();
                notifyActionTermination(INFORMATION, DisplayConstants.TITLE_SUB_SAVE, DisplayConstants.MESSAGE_DATABASE_SAVED, databaseSaver.getValue());
            } else if (FAILED == newState) {
                notifyActionTermination(ERROR, DisplayConstants.TITLE_SUB_SAVE, DisplayConstants.MESSAGE_DATABASE_SAVE_KO, getServiceErrorMessage(databaseSaver));
            }
        };
    }

    private ChangeListener<Worker.State> getLoaderStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                handleDatabaseLoaderSuccess();
            } else if (FAILED == newState) {
                notifyActionTermination(ERROR, DisplayConstants.TITLE_SUB_LOAD, DisplayConstants.MESSAGE_DATABASE_LOAD_KO, getServiceErrorMessage(databaseLoader));
            }
        };
    }

    BooleanProperty runningServiceProperty() {
        return runningServiceProperty;
    }

    DatabaseChecker getDatabaseChecker() {
        return databaseChecker;
    }

    DatabaseFixer getDatabaseFixer() {
        return databaseFixer;
    }

    DatabaseLoader getDatabaseLoader() {
        return databaseLoader;
    }

    DatabaseSaver getDatabaseSaver() {
        return databaseSaver;
    }

    void setDatabaseLoader(DatabaseLoader databaseLoaderMock) {
        // For testing use
        databaseLoader = databaseLoaderMock;
    }
}

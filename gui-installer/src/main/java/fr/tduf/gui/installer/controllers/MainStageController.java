package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.gui.common.controllers.helper.DatabaseOpsHelper;
import fr.tduf.gui.common.helper.MessagesHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.common.services.DatabaseChecker;
import fr.tduf.gui.common.services.DatabaseFixer;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.controllers.helper.DealerSlotUserInputHelper;
import fr.tduf.gui.installer.controllers.helper.VehicleSlotUserInputHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.services.InstallerDatabaseLoader;
import fr.tduf.gui.installer.services.StepsCoordinator;
import fr.tduf.gui.installer.services.tasks.TaskType;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static fr.tduf.gui.installer.common.DisplayConstants.*;
import static fr.tduf.gui.installer.common.InstallerConstants.DIRECTORY_DATABASE;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.when;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private SimpleStringProperty tduDirectoryProperty;

    private LongProperty progressProperty = new SimpleLongProperty();
    private BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private InstallerDatabaseLoader databaseLoader = new InstallerDatabaseLoader();
    private DatabaseChecker databaseChecker = new DatabaseChecker();
    private DatabaseFixer databaseFixer = new DatabaseFixer();
    private StepsCoordinator stepsCoordinator = new StepsCoordinator();

    @FXML
    private TextArea readmeTextArea;

    @FXML
    private TextField tduLocationTextField;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar uProgressBar;

    @FXML
    private ProgressBar lProgressBar;

    @Override
    public void init() throws IOException {
        runningServiceProperty.bind(
                databaseChecker.runningProperty()
                        .or(databaseFixer.runningProperty())
                        .or(stepsCoordinator.runningProperty())
                        .or(databaseLoader.runningProperty())
        );
        mouseCursorProperty().bind(
                when(runningServiceProperty)
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );
        uProgressBar.progressProperty().bind(progressProperty);
        lProgressBar.progressProperty().bind(progressProperty);

        initServiceListeners();

        initReadme();

        initActionToolbar();
    }

    @FXML
    private void handleUpdateMagicMapMenuItemAction() throws StepException {
        Log.trace(THIS_CLASS_NAME, "->handleUpdateMagicMapMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        updateMagicMap();
    }

    @FXML
    private void handleResetSlotMenuItemAction() {
        Log.trace(THIS_CLASS_NAME, "->handleResetSlotMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabaseForObjective(TaskType.RESET_SLOT);
    }

    @FXML
    private void handleResetDatabaseCacheMenuItemAction() throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleResetDatabaseCacheMenuItemAction");

        if (!StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            resetDatabaseCache();
        }

    }

    @FXML
    private void handleCheckDatabaseMenuItemAction() {
        Log.trace(THIS_CLASS_NAME, "->handleCheckDatabaseMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        checkDatabase();
    }

    @FXML
    private void handleBrowseTduLocationButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleBrowseTduLocationButtonAction");

        browseForTduDirectory();
    }

    @FXML
    private void handleInstallButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleInstallButtonAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabaseForObjective(TaskType.INSTALL);
    }

    @FXML
    private void handleUninstallButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleUninstallButtonAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabaseForObjective(TaskType.UNINSTALL);
    }

    private void initReadme() throws IOException {
        Path readmePath = Paths.get(
                InstallerConstants.DIRECTORY_ASSETS,
                InstallerConstants.DIRECTORY_README,
                InstallerConstants.FILE_README);
        List<String> lines = Files.readAllLines(readmePath, Charset.defaultCharset());
        String readmeText = String.join(System.lineSeparator(), lines);

        readmeTextArea.setText(readmeText);
    }

    private void initActionToolbar() {
        tduDirectoryProperty = new SimpleStringProperty();

        tduLocationTextField.setPromptText(PROMPT_TEXT_TDU_LOCATION);
        tduLocationTextField.textProperty().bindBidirectional(tduDirectoryProperty);
    }

    private void initServiceListeners() {
        databaseChecker.stateProperty().addListener(getCheckerStateChangeListener());

        databaseFixer.stateProperty().addListener(getFixerStateChangeListener());

        stepsCoordinator.stateProperty().addListener(getCoordinatorStateChangeListener());

        databaseLoader.stateProperty().addListener(getLoaderStateChangeListener());
    }

    private ChangeListener<Worker.State> getLoaderStateChangeListener() {
        return (observable, oldValue, newState) -> {
            TaskType objective = databaseLoader.objectiveProperty().get();
            if (SUCCEEDED == newState) {
                switch(objective) {
                    case INSTALL:
                        install(databaseLoader.getValue());
                        break;
                    case UNINSTALL:
                        uninstall(databaseLoader.getValue());
                        break;
                    case RESET_SLOT:
                        selectAndResetSlot(databaseLoader.getValue());
                        break;
                }
            } else if (FAILED == newState) {
                String effectiveSubTitle;
                switch(objective) {
                    case INSTALL:
                        effectiveSubTitle = TITLE_SUB_INSTALL;
                        break;
                    case UNINSTALL:
                        effectiveSubTitle = TITLE_SUB_UNINSTALL;
                        break;
                    case RESET_SLOT:
                        effectiveSubTitle = TITLE_SUB_RESET_TDUCP_SLOT;
                        break;
                    default:
                        effectiveSubTitle = "";
                }
                handleServiceFailure(databaseLoader.exceptionProperty().get(), effectiveSubTitle, MESSAGE_DB_LOAD_KO);
            }
        };
    }

    private ChangeListener<Worker.State> getCoordinatorStateChangeListener() {
        return (observable, oldValue, newState) -> {
            if (SUCCEEDED == newState) {
                handleCoordinatorSuccess();
            } else if (FAILED == newState) {
                handleCoordinatorFailure();
            }

            if (SUCCEEDED == newState || FAILED == newState) {
                progressProperty.unbind();
                progressProperty.setValue(0);
                statusLabel.textProperty().unbind();
            }
        };
    }

    private ChangeListener<Worker.State> getFixerStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> remainingErrors = databaseFixer.integrityErrorsProperty().get();
                if (remainingErrors.isEmpty()) {
                    notifyActionTermination(INFORMATION, TITLE_SUB_FIX_DB, MESSAGE_DB_FIX_OK, MESSAGE_DB_ZERO_ERROR);
                } else {
                    notifyActionTermination(WARNING, TITLE_SUB_FIX_DB, MESSAGE_DB_FIX_KO, MESSAGE_DB_REMAINING_ERRORS);
                }
            } else if (FAILED == newState) {
                handleServiceFailure(databaseFixer.exceptionProperty().get(), TITLE_SUB_FIX_DB, MESSAGE_DB_FIX_KO);
            }
        };
    }

    private ChangeListener<Worker.State> getCheckerStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> integrityErrors = databaseChecker.integrityErrorsProperty().get();
                if (integrityErrors.isEmpty()) {
                    notifyActionTermination(INFORMATION, TITLE_SUB_CHECK_DB, MESSAGE_DB_CHECK_OK, MESSAGE_DB_ZERO_ERROR);
                } else if (DatabaseOpsHelper.displayCheckResultDialog(integrityErrors, getWindow(), TITLE_APPLICATION)) {
                    fixDatabase();
                }
            } else if (FAILED == newState) {
                handleServiceFailure(databaseChecker.exceptionProperty().get(), TITLE_SUB_CHECK_DB, MESSAGE_DB_CHECK_KO);
            }
        };
    }

    private void browseForTduDirectory() {
        if (runningServiceProperty.get()) {
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (tduDirectoryProperty.getValue() != null) {
            File directory = new File(tduLocationTextField.getText());
            if (directory.exists()) {
                directoryChooser.setInitialDirectory(directory);
            }
        }

        File selectedDirectory = directoryChooser.showDialog(getWindow());
        if (selectedDirectory != null) {
            tduDirectoryProperty.set(selectedDirectory.getPath());
        }
    }

    private void updateMagicMap() throws StepException {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        GenericStep.starterStep(configuration, null)
                .nextStep(GenericStep.StepType.UPDATE_MAGIC_MAP).start();

        notifyActionTermination(INFORMATION, TITLE_SUB_MAP_UPDATE, MESSAGE_UPDATED_MAP, configuration.resolveMagicMapFile());
    }

    private void resetDatabaseCache() throws IOException {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        Path databasePath = Paths.get(configuration.resolveDatabaseDirectory());
        DatabaseBanksCacheHelper.clearCache(databasePath);

        notifyActionTermination(INFORMATION, TITLE_SUB_RESET_DB_CACHE, MESSAGE_DELETED_CACHE, databasePath.toString());
    }

    private void checkDatabase() {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().bind(databaseChecker.messageProperty());

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        databaseChecker.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseChecker.bankSupportProperty().setValue(configuration.getBankSupport());

        databaseChecker.restart();
    }

    private void fixDatabase() {
        // Do not check for service here, as checker may still be in running state.
        statusLabel.textProperty().bind(databaseFixer.messageProperty());

        databaseFixer.fromService(databaseChecker);

        databaseFixer.restart();
    }

    private void loadDatabaseForObjective(TaskType objective) {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        statusLabel.textProperty().bind(databaseLoader.messageProperty());

        databaseLoader.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseLoader.bankSupportProperty().setValue(configuration.getBankSupport());
        databaseLoader.objectiveProperty().setValue(objective);

        databaseLoader.restart();
    }

    private void install(DatabaseContext context) {
        // Do not check for service here, as loader may still be in running state.
        requireNonNull(context, "Database context is required. Please load database first.");

        progressProperty.setValue(-1);

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        try {
            loadCurrentPatch(configuration, context);
        } catch (IOException ioe) {
            StepException se = new StepException(GenericStep.StepType.LOAD_PATCH, MESSAGE_PATCH_LOAD_KO, ioe);
            handleServiceFailure(se, TITLE_SUB_INSTALL, MESSAGE_NOT_INSTALLED);

            progressProperty.setValue(0);
            return;
        }

        try {
            VehicleSlotUserInputHelper.selectAndDefineVehicleSlot(context, getWindow());

            DealerSlotUserInputHelper.selectAndDefineDealerSlot(context, getWindow());
        } catch (Exception e) {
            StepException se = new StepException(GenericStep.StepType.SELECT_SLOTS, MESSAGE_INSTALL_ABORTED, e);
            handleServiceFailure(se, TITLE_SUB_INSTALL, MESSAGE_NOT_INSTALLED);

            progressProperty.setValue(0);
            return;
        }

        statusLabel.textProperty().bind(stepsCoordinator.messageProperty());
        progressProperty.bind(stepsCoordinator.progressProperty());

        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.contextProperty().setValue(context);
        stepsCoordinator.taskTypeProperty().setValue(TaskType.INSTALL);

        stepsCoordinator.restart();
    }

    private void uninstall(DatabaseContext context) {
        // Do not check for service here, as loader may still be in running state.
        requireNonNull(context, "Database context is required. Please load database first.");

        progressProperty.setValue(-1);

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        statusLabel.textProperty().bind(stepsCoordinator.messageProperty());
        progressProperty.bind(stepsCoordinator.progressProperty());

        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.contextProperty().setValue(context);
        stepsCoordinator.taskTypeProperty().setValue(TaskType.UNINSTALL);

        stepsCoordinator.restart();
    }

    private void selectAndResetSlot(DatabaseContext context) {
        // Do not check for service here, as loader may still be in running state.
        requireNonNull(context, "Database context is required. Please load database first.");

        try {
            VehicleSlotUserInputHelper.quickSelectVehicleSlot(VehicleSlotsHelper.SlotKind.TDUCP_NEW, context, getWindow());
        } catch (Exception e) {
            StepException se = new StepException(GenericStep.StepType.SELECT_SLOTS, MESSAGE_OPERATION_ABORTED, e);
            handleServiceFailure(se, TITLE_SUB_RESET_TDUCP_SLOT, MESSAGE_NOT_RESET);
            return;
        }

        progressProperty.setValue(-1);

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        statusLabel.textProperty().bind(stepsCoordinator.messageProperty());
        progressProperty.bind(stepsCoordinator.progressProperty());

        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.contextProperty().setValue(context);
        stepsCoordinator.taskTypeProperty().setValue(TaskType.RESET_SLOT);

        stepsCoordinator.restart();
    }

    private void handleCoordinatorFailure() {
        TaskType currentTask = stepsCoordinator.taskTypeProperty().getValue();
        Throwable currentException = stepsCoordinator.exceptionProperty().get();
        if (TaskType.UNINSTALL == currentTask) {
            handleServiceFailure(currentException, TITLE_SUB_UNINSTALL, MESSAGE_NOT_UNINSTALLED);
        } else if (TaskType.INSTALL == currentTask) {
            handleServiceFailure(currentException, TITLE_SUB_INSTALL, MESSAGE_NOT_INSTALLED);
        } else if(TaskType.RESET_SLOT == currentTask) {
            handleServiceFailure(currentException, TITLE_SUB_RESET_TDUCP_SLOT, MESSAGE_NOT_RESET);
        }
    }

    private void handleServiceFailure(Throwable throwable, String subTitle, String mainMessage) {
        Log.error(THIS_CLASS_NAME, ExceptionUtils.getStackTrace(throwable));

        String stepName = LABEL_STEP_UNKNOWN;
        if (throwable instanceof StepException) {
            stepName = ((StepException) throwable).getStepName();
        }

        notifyActionTermination(ERROR, subTitle, mainMessage, MessagesHelper.getAdvancedErrorMessage(throwable, String.format(FORMAT_MESSAGE_STEP, stepName)));
    }

    private void handleCoordinatorSuccess() {
        TaskType currentTask = stepsCoordinator.taskTypeProperty().getValue();
        if (TaskType.UNINSTALL == currentTask) {
            notifyActionTermination(INFORMATION, TITLE_SUB_UNINSTALL, MESSAGE_UNINSTALLED, "");
        } else if (TaskType.INSTALL == currentTask) {
            notifyActionTermination(INFORMATION, TITLE_SUB_INSTALL, MESSAGE_INSTALLED, "");
        } else if(TaskType.RESET_SLOT == currentTask) {
            notifyActionTermination(INFORMATION, TITLE_SUB_RESET_TDUCP_SLOT, MESSAGE_RESET_SLOT, "");
        }
    }

    private void notifyActionTermination(Alert.AlertType alertType, String subTitle, String message, String description) {
        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(alertType)
                .withTitle(TITLE_APPLICATION + subTitle)
                .withMessage(message)
                .withDescription(description)
                .build();
        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
    }

    private static void loadCurrentPatch(InstallerConfiguration configuration, DatabaseContext context) throws IOException {
        Path patchPath = Paths.get(configuration.getAssetsDirectory(), DIRECTORY_DATABASE);

        File patchFile;
        try (Stream<Path> pathStream = Files.walk(patchPath, 1)) {
            patchFile = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                    .findFirst()
                    .orElseThrow(() -> new IOException(String.format(MESSAGE_FMT_PATCH_NOT_FOUND, DIRECTORY_DATABASE)))
                    .toFile();
        }

        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);

        DatabasePatchProperties patchProperties = PatchPropertiesReadWriteHelper.readDatabasePatchProperties(patchFile);
        if (patchProperties.isEmpty()) {
            throw new IOException(MESSAGE_INVALID_PROPERTIES);
        }

        context.setPatch(patchObject, patchProperties);
    }
}

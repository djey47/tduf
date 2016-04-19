package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Strings;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.controllers.helper.UserInputHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.services.DatabaseChecker;
import fr.tduf.gui.installer.services.DatabaseFixer;
import fr.tduf.gui.installer.services.DatabaseLoader;
import fr.tduf.gui.installer.services.StepsCoordinator;
import fr.tduf.gui.installer.stages.DatabaseCheckStageDesigner;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static com.google.common.io.Files.getFileExtension;
import static fr.tduf.gui.installer.common.InstallerConstants.DIRECTORY_DATABASE;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.nio.file.Files.isRegularFile;
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

    private BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private DatabaseLoader databaseLoader = new DatabaseLoader();
    private DatabaseChecker databaseChecker = new DatabaseChecker();
    private DatabaseFixer databaseFixer = new DatabaseFixer();
    private StepsCoordinator stepsCoordinator = new StepsCoordinator();

    @FXML
    private TextArea readmeTextArea;

    @FXML
    private TextField tduLocationTextField;

    @FXML
    private Label statusLabel;

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

        initServiceListeners();

        initReadme();

        initActionToolbar();
    }

    @FXML
    public void handleUpdateMagicMapMenuItemAction(ActionEvent actionEvent) throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleUpdateMagicMapMenuItemAction");

        if (Strings.isNullOrEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        updateMagicMap();
    }

    @FXML
    public void handleResetDatabaseCacheMenuItemAction(ActionEvent actionEvent) throws IOException, ReflectiveOperationException {
        Log.trace(THIS_CLASS_NAME, "->handleResetDatabaseCacheMenuItemAction");

        if (Strings.isNullOrEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        resetDatabaseCache();
    }

    @FXML
    public void handleCheckDatabaseMenuItemAction(ActionEvent actionEvent) throws IOException, ReflectiveOperationException {
        Log.trace(THIS_CLASS_NAME, "->handleCheckDatabaseMenuItemAction");

        if (Strings.isNullOrEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        checkDatabase();
    }

    @FXML
    public void handleBrowseTduLocationButtonAction(ActionEvent actionEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleBrowseTduLocationButtonAction");

        browseForTduDirectory();
    }

    @FXML
    public void handleInstallButtonAction(ActionEvent actionEvent) throws IOException, ReflectiveOperationException {
        Log.trace(THIS_CLASS_NAME, "->handleInstallButtonAction");

        if (Strings.isNullOrEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabase();
    }

    private void initReadme() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(InstallerConstants.FILE_README), Charset.defaultCharset());
        String readmeText = StringUtils.join(lines, System.lineSeparator());

        readmeTextArea.setText(readmeText);
    }

    private void initActionToolbar() {
        tduDirectoryProperty = new SimpleStringProperty();

        tduLocationTextField.setPromptText(DisplayConstants.PROMPT_TEXT_TDU_LOCATION);
        tduLocationTextField.textProperty().bindBidirectional(tduDirectoryProperty);
    }

    private void initServiceListeners() {
        databaseChecker.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> integrityErrors = databaseChecker.getValue();
                if (integrityErrors.isEmpty()) {
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_CHECK_DB, DisplayConstants.MESSAGE_DB_CHECK_OK, DisplayConstants.MESSAGE_DB_ZERO_ERROR);
                    return;
                }
                if (displayCheckResultDialog(integrityErrors)) {
                    fixDatabase(integrityErrors);
                }
            } else if (FAILED == newState) {
                handleServiceFailure(databaseChecker.exceptionProperty().get(), DisplayConstants.MESSAGE_DB_CHECK_KO);
            }
        });

        databaseFixer.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> remainingErrors = databaseFixer.getValue();
                if (remainingErrors.isEmpty()) {
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_FIX_DB, DisplayConstants.MESSAGE_DB_FIX_OK, DisplayConstants.MESSAGE_DB_ZERO_ERROR);
                } else {
                    CommonDialogsHelper.showDialog(WARNING, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_FIX_DB, DisplayConstants.MESSAGE_DB_FIX_KO, DisplayConstants.MESSAGE_DB_REMAINING_ERRORS);
                }
            } else if (FAILED == newState) {
                handleServiceFailure(databaseFixer.exceptionProperty().get(), DisplayConstants.MESSAGE_DB_FIX_KO);
            }
        });

        stepsCoordinator.stateProperty().addListener((observable, oldValue, newState) -> {
            if (SUCCEEDED == newState) {
                CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_INSTALLED, "");
            } else if (FAILED == newState) {
                handleServiceFailure(stepsCoordinator.exceptionProperty().get(), DisplayConstants.MESSAGE_NOT_INSTALLED);
            }
//            statusLabel.textProperty().unbind();
//            statusLabel.textProperty().setValue("");
        });

        databaseLoader.stateProperty().addListener((observable, oldValue, newState) -> {
            if (SUCCEEDED == newState) {
                install(databaseLoader.getValue());
            } else if (FAILED == newState) {
                handleServiceFailure(databaseLoader.exceptionProperty().get(), DisplayConstants.MESSAGE_DB_LOAD_KO);
            }
        });
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

    private void updateMagicMap() throws Exception {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        GenericStep.starterStep(configuration, null)
                .nextStep(GenericStep.StepType.UPDATE_MAGIC_MAP).start();

        String magicMapFile = configuration.resolveMagicMapFile();
        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_MAP_UPDATE, DisplayConstants.MESSAGE_UPDATED_MAP, magicMapFile);
    }

    private void resetDatabaseCache() throws IOException, ReflectiveOperationException {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        Path databasePath = Paths.get(configuration.resolveDatabaseDirectory());
        DatabaseBanksCacheHelper.clearCache(databasePath);

        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_DB_CACHE, DisplayConstants.MESSAGE_DELETED_CACHE, databasePath.toString());
    }

    private void checkDatabase() {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().bind(databaseChecker.messageProperty());

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        databaseChecker.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseChecker.bankSupportProperty().setValue(configuration.getBankSupport());

        databaseChecker.restart();
    }

    private void fixDatabase(Set<IntegrityError> integrityErrors) {
        // Do not check for service here, as checker may still be in running state.
        statusLabel.textProperty().bind(databaseFixer.messageProperty());

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        databaseFixer.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseFixer.bankSupportProperty().setValue(configuration.getBankSupport());
        databaseFixer.integrityErrorsProperty().setValue(integrityErrors);

        databaseFixer.restart();
    }

    private void loadDatabase() throws IOException, ReflectiveOperationException {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .withMainWindow(getWindow())
                .build();

        statusLabel.textProperty().bind(databaseLoader.messageProperty());

        databaseLoader.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseLoader.bankSupportProperty().setValue(configuration.getBankSupport());

        databaseLoader.restart();
    }

    private void install(DatabaseContext context) {
        // Do not check for service here, as loader may still be in running state.
        requireNonNull(context, "Database context is required. Please load database first.");

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .withMainWindow(getWindow())
                .build();

        try {
            loadCurrentPatch(configuration, context);

            UserInputHelper.selectAndDefineVehicleSlot(context, getWindow());

            UserInputHelper.selectAndDefineDealerSlot(context, getWindow());

            statusLabel.textProperty().bind(stepsCoordinator.messageProperty());

            stepsCoordinator.configurationProperty().setValue(configuration);
            stepsCoordinator.contextProperty().setValue(context);

            stepsCoordinator.restart();
        } catch (Exception e) {
            handleServiceFailure(e, DisplayConstants.MESSAGE_NOT_INSTALLED);
        }
    }

    private boolean displayCheckResultDialog(Set<IntegrityError> integrityErrors) {
        try {
            DatabaseCheckStageController databaseCheckStageController = initDatabaseCheckStageController(getWindow());
            return databaseCheckStageController.initAndShowModalDialog(integrityErrors);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void loadCurrentPatch(InstallerConfiguration configuration, DatabaseContext context) throws IOException {
        Path patchPath = Paths.get(configuration.getAssetsDirectory(), DIRECTORY_DATABASE);
        final Path patchFilePath = Files.walk(patchPath, 1)

                .filter((path) -> isRegularFile(path))

                .filter((path) -> EXTENSION_JSON.equalsIgnoreCase(getFileExtension(path.toString())))

                .findFirst()

                .orElseThrow(() -> new IOException(String.format(DisplayConstants.MESSAGE_FMT_PATCH_NOT_FOUND, DIRECTORY_DATABASE)));

        final File patchFile = patchFilePath.toFile();
        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);

        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);
        if (patchProperties.isEmpty()) {
            throw new IOException(DisplayConstants.MESSAGE_INVALID_PROPERTIES);
        }

        context.setPatch(patchObject, patchProperties);
    }

    private static DatabaseCheckStageController initDatabaseCheckStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DatabaseCheckStageDesigner.init(stage);
    }

    private static void handleServiceFailure(Throwable throwable, String mainMessage) {
        throwable.printStackTrace();

        String stepName = DisplayConstants.LABEL_STEP_UNKNOWN;
        if (throwable instanceof StepException) {
            stepName = ((StepException) throwable).getStepName();
        }

        String causeMessage = "";
        if (throwable.getCause() != throwable) {
            causeMessage = throwable.getCause().getMessage();
        }

        final String errorMessage = String.format(DisplayConstants.MESSAGE_FMT_ERROR, throwable.getMessage(), stepName, causeMessage);
        CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_INSTALL, mainMessage, errorMessage);
    }
}

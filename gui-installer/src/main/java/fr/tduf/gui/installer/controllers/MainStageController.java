package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.gui.common.helper.javafx.CommonDialogsHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.services.DatabaseChecker;
import fr.tduf.gui.installer.stages.DatabaseCheckStageDesigner;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.gui.installer.steps.StepsCoordinator;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static javafx.beans.binding.Bindings.when;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.INFORMATION;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private SimpleStringProperty tduDirectoryProperty;

    private DatabaseChecker databaseChecker = new DatabaseChecker();

    @FXML
    private TextArea readmeTextArea;

    @FXML
    private TextField tduLocationTextField;

    @FXML
    private Label statusLabel;

    @Override
    public void init() throws IOException {
        mouseCursorProperty().bind(
                when(databaseChecker.runningProperty())
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT));

        initServiceListeners();

        initReadme();

        initActionToolbar();
    }

    @FXML
    public void handleUpdateMagicMapMenuItemAction(ActionEvent actionEvent) throws IOException, ReflectiveOperationException {
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

        install();
    }

    private void initReadme() throws IOException {
        File readmeFile = new File(InstallerConstants.FILE_README);

        List<String> lines = Files.readLines(readmeFile, Charset.defaultCharset());
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
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_CHECK_DB, DisplayConstants.MESSAGE_DB_CHECK_OK, DisplayConstants.MESSAGE_DB_ZERO_ERROR_OK);
                } else {
                    try {
                        DatabaseCheckStageController databaseCheckStageController = initDatabaseCheckStageController(getWindow());
                        databaseCheckStageController.initAndShowModalDialog(integrityErrors);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void browseForTduDirectory() {
        // TODO Block if check database service is already running

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

    private void updateMagicMap() throws IOException, ReflectiveOperationException {
        // TODO Block if check database service is already running

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        GenericStep.starterStep(configuration, null, null)
                .nextStep(GenericStep.StepType.UPDATE_MAGIC_MAP).start();

        String magicMapFile = configuration.resolveMagicMapFile();
        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_MAP_UPDATE, DisplayConstants.MESSAGE_UPDATED_MAP, magicMapFile);
    }

    private void resetDatabaseCache() throws IOException, ReflectiveOperationException {
        // TODO Block if check database service is already running

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        Path databasePath = Paths.get(configuration.resolveDatabaseDirectory());
        DatabaseBanksCacheHelper.clearCache(databasePath);

        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_DB_CACHE, DisplayConstants.MESSAGE_DELETED_CACHE, databasePath.toString());
    }

    private void checkDatabase() {
        // TODO Block if check database service is already running

        statusLabel.textProperty().bind(databaseChecker.messageProperty());

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withMainWindow(getWindow())
                .build();

        databaseChecker.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseChecker.bankSupportProperty().setValue(configuration.getBankSupport());

        databaseChecker.restart();
    }

    private void install() throws IOException, ReflectiveOperationException {
        // TODO Block if check database service is running

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .withMainWindow(getWindow())
                .build();

        StepsCoordinator.install(configuration);
        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_INSTALLED, "");
    }

    private static DatabaseCheckStageController initDatabaseCheckStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DatabaseCheckStageDesigner.init(stage);
    }
}

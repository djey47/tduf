package fr.tduf.gui.launcher.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.game.helpers.GameSettingsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.launcher.domain.javafx.GameSettingDataItem;
import fr.tduf.gui.launcher.services.LauncherStepsCoordinator;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.common.game.domain.bin.GameStatus;
import fr.tduf.libunlimited.common.game.domain.bin.LaunchSwitch;
import fr.tduf.libunlimited.common.game.domain.bin.ProcessExitReason;
import fr.tduf.libunlimited.common.game.helper.GameStatusHelper;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static fr.tduf.gui.launcher.common.DisplayConstants.FORMAT_LABEL_EXIT_REASON;
import static fr.tduf.libunlimited.common.game.domain.bin.GameStatus.*;

public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private final LauncherStepsCoordinator stepsCoordinator = new LauncherStepsCoordinator();

    private final ApplicationConfiguration configuration = new ApplicationConfiguration();

    @FXML
    private Label gameVersionLabel;

    @FXML
    private Label gameStatusLabel;

    @FXML
    private Hyperlink forceCloseLink;

    @FXML
    private Label processExitReasonLabel;
    
    @FXML
    private Button runButton;
    
    @FXML
    private TextField gameDirectoryTextField;

    @FXML
    private TreeTableView<GameSettingDataItem> settingsTableView;

    @Override
    protected void init() throws IOException {
        AbstractGuiApp.setMainController(this);
        
        loadAndCheckConfiguration();

        initInfoTab();
        
        initSettingsTab();
    }

    @FXML
    private void handleRunButtonAction() {
        Log.trace(THIS_CLASS_NAME, "handleRunButtonAction");

        runGame();
    }

    @FXML
    private void handleForceCloseLinkAction() {
        Log.trace(THIS_CLASS_NAME, "handleForceCloseLinkAction");
        
        stopGameProcess();
    }

    @FXML
    private void handleBrowseGameLocationButtonAction() {
        Log.trace(THIS_CLASS_NAME, "handleBrowseGameLocationButtonAction");
        
        browseForGameDirectory();
    }    

    /**
     * @throws IOException when configuration can't be saved because of disk IO error
     */
    public void saveConfiguration() throws IOException {
        Log.trace(THIS_CLASS_NAME, "saveConfiguration");
        configuration.store();
    }

    private void loadAndCheckConfiguration() throws IOException {
        configuration.load();

        if (!configuration.getGamePath().isPresent()) {
            // Window instance is not accessible yet
            GameSettingsHelper.askForGameLocationAndUpdateConfiguration(configuration, null);
        }
    }

    private void initInfoTab() {
        String binaryPath = configuration.getGamePath()
                .map(path -> path.resolve(FileConstants.FILE_GAME_EXECUTABLE))
                .map(Path::toString)
                .orElse(null);
        gameVersionLabel.setText(GameStatusHelper.resolveGameVersion(binaryPath).getLabel());
        
        gameStatusLabel.textProperty().bindBidirectional(stepsCoordinator.processStatusProperty(), new StringConverter<GameStatus>() {
            @Override
            public String toString(GameStatus gameStatus) {
                return gameStatus.getLabel();
            }

            @Override
            public GameStatus fromString(String string) {
                return UNKNOWN;
            }
        });
        
        processExitReasonLabel.textProperty().bindBidirectional(stepsCoordinator.processExitReasonProperty(), new StringConverter<ProcessExitReason>() {
            @Override
            public String toString(ProcessExitReason exitReason) {
                return String.format(FORMAT_LABEL_EXIT_REASON, exitReason.getLabel());
            }

            @Override
            public ProcessExitReason fromString(String string) {
                return null;
            }
        });

        forceCloseLink.visibleProperty().set(false);
        processExitReasonLabel.visibleProperty().set(false);
        stepsCoordinator.processStatusProperty().addListener((observable, oldValue, newValue) -> {
            forceCloseLink.visibleProperty().set(newValue == RUNNING);
            processExitReasonLabel.visibleProperty().set(newValue == OFF_ABNORMALLY);
            runButton.disableProperty().set(newValue == RUNNING);
        });
    }

    private void initSettingsTab() {
        TreeTableColumn<GameSettingDataItem, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(250);
        nameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<GameSettingDataItem, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getNameProperty().getValue()));

        TreeTableColumn<GameSettingDataItem, Boolean> enabledColumn = new TreeTableColumn<>("Enabled?");
        enabledColumn.setPrefWidth(75);
        enabledColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<GameSettingDataItem, Boolean> param) -> new ReadOnlyBooleanWrapper(param.getValue().getValue().getEnabledProperty().getValue()));

        settingsTableView.getColumns().addAll(nameColumn, enabledColumn);
        final TreeItem<GameSettingDataItem> root = new TreeItem<>(new GameSettingDataItem("Settings"));
        root.setExpanded(true);
        settingsTableView.setRoot(root);

        String gameDirectory = configuration.getGamePath()
                .map(Path::toString)
                .orElse("");
        gameDirectoryTextField.setDisable(true);
        gameDirectoryTextField.setText(gameDirectory);
    }

    private void browseForGameDirectory() {
        String selectedDirectory = GameSettingsHelper.askForGameLocationAndUpdateConfiguration(configuration, getWindow());
        if (!selectedDirectory.isEmpty()) {
            gameDirectoryTextField.setText(selectedDirectory);
        }
    }    

    private void runGame() {
        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.gameSwitchesProperty().setValue(getSelectedSwitches());
        stepsCoordinator.restart();
    }

    private void stopGameProcess() {
        Process process = stepsCoordinator.gameProcessProperty().getValue();
        if (process == null) {
            return;
        }
        
        process.destroyForcibly();
    }

    private Set<LaunchSwitch> getSelectedSwitches() {
        // TODO get checked switches from tree
        return new HashSet<>(LaunchSwitch.values().length);
    }
}

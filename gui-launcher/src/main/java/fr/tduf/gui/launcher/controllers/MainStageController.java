package fr.tduf.gui.launcher.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.game.helpers.GameSettingsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.launcher.services.LauncherStepsCoordinator;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.common.game.domain.GameStatus;
import fr.tduf.libunlimited.common.game.helper.GameStatusHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import javax.xml.soap.Text;
import java.io.IOException;
import java.nio.file.Path;

import static fr.tduf.libunlimited.common.game.domain.GameStatus.UNKNOWN;

public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private final LauncherStepsCoordinator stepsCoordinator = new LauncherStepsCoordinator();

    private final ApplicationConfiguration configuration = new ApplicationConfiguration();

    @FXML
    private Label gameVersionLabel;

    @FXML
    private Label gameStatusLabel;
    
    @FXML
    private TextField gameDirectoryTextField;

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

    }

    private void initSettingsTab() {
        String gameDirectory = configuration.getGamePath()
                .map(Path::toString)
                .orElse("");
        gameDirectoryTextField.setText(gameDirectory);
    }

    private void runGame() {
        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.restart();
    }
}

package fr.tduf.gui.launcher.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.game.helpers.GameSettingsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.launcher.services.LauncherStepsCoordinator;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private final LauncherStepsCoordinator stepsCoordinator = new LauncherStepsCoordinator();

    private final ApplicationConfiguration configuration = new ApplicationConfiguration();

    @Override
    protected void init() throws IOException {
        AbstractGuiApp.setMainController(this);
        
        loadAndCheckConfiguration();
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

    private void runGame() {
        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.restart();
    }
}

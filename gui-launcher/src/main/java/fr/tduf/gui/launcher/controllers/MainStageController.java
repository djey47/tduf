package fr.tduf.gui.launcher.controllers;

import com.esotericsoftware.minlog.Log;
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
        configuration.load();
    }

    @FXML
    private void handleRunButtonAction() {
        Log.trace(THIS_CLASS_NAME, "handleRunButtonAction");

        runGame();
    }

    private void runGame() {
        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.restart();
    }
}

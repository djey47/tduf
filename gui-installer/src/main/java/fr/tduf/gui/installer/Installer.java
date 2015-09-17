package fr.tduf.gui.installer;

import fr.tduf.gui.installer.stages.MainStageDesigner;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Installer Java FX Application.
 */
public class Installer extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainStageDesigner.init(primaryStage);
        primaryStage.show();
    }

    /**
     * Single application entry point
     * @param args  : arguments passed with command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
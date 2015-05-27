package fr.tduf.gui.database;

import fr.tduf.gui.database.stages.MainStageDesigner;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point.
 */
public class DatabaseEditor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainStageDesigner.init(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

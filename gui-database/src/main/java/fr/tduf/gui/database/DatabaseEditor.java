package fr.tduf.gui.database;

import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.database.stages.MainStageDesigner;
import javafx.stage.Stage;

/**
 * DatabaseEditor Java FX Application.
 */
public class DatabaseEditor extends AbstractGuiApp {

    @Override
    public void startApp(Stage primaryStage) throws Exception {
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

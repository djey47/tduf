package fr.tduf.gui.database;

import fr.tduf.gui.database.stages.MainStageDesigner;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * DatabaseEditor Java FX Application.
 */
public class DatabaseEditor extends Application {

    private static List<String> parameters;

    @Override
    public void start(Stage primaryStage) throws Exception {
        parameters = getParameters().getUnnamed();

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

    /**
     * @return All arguments passed with command line, if any.
     */
    public static List<String> getParameterList() {
        return parameters;
    }
}
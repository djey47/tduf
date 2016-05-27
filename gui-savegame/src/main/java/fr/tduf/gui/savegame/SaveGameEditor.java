package fr.tduf.gui.savegame;

import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.savegame.stages.MainStageDesigner;
import javafx.stage.Stage;

/**
 * SaveGameEditor Java FX Application.
 */
public class SaveGameEditor extends AbstractGuiApp {

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

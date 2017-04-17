package fr.tduf.gui.launcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.launcher.stages.MainStageDesigner;
import javafx.stage.Stage;

import static com.esotericsoftware.minlog.Log.LEVEL_TRACE;

/**
 * Launcher Java FX Application.
 */
public class Launcher extends AbstractGuiApp {

    @Override
    public void startApp(Stage primaryStage) throws Exception {
        Log.set(LEVEL_TRACE);

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

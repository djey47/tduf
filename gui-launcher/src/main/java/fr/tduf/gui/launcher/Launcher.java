package fr.tduf.gui.launcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.launcher.stages.MainStageDesigner;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.esotericsoftware.minlog.Log.LEVEL_TRACE;

/**
 * Launcher Java FX Application.
 */
public class Launcher extends AbstractGuiApp {
    private static final String THIS_CLASS_NAME = Launcher.class.getSimpleName();

    @Override
    protected void startApp(Stage primaryStage) throws Exception {
        Log.set(LEVEL_TRACE);

        MainStageDesigner.init(primaryStage);
        primaryStage.show();
    }

    @Override
    protected EventHandler<WindowEvent> onExitHandler() {
        return event -> {
            Log.trace(THIS_CLASS_NAME, "Exiting launcher...");
        };
    }

    /**
     * Single application entry point
     * @param args  : arguments passed with command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

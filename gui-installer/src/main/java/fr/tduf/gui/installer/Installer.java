package fr.tduf.gui.installer;

import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.installer.stages.MainStageDesigner;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Installer Java FX Application.
 */
public class Installer extends AbstractGuiApp {

    @Override
    protected void startApp(Stage primaryStage) throws Exception {
        MainStageDesigner.init(primaryStage);
        primaryStage.show();
    }

    @Override
    protected EventHandler<WindowEvent> onExitHandler() {
        return event -> {};
    }

    /**
     * Single application entry point
     * @param args : arguments passed with command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

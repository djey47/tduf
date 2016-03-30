package fr.tduf.gui.installer;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.MainStageDesigner;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static fr.tduf.gui.installer.common.InstallerConstants.SWITCH_VERBOSE;

/**
 * Installer Java FX Application.
 */
public class Installer extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        handleLogLevel();

        MainStageDesigner.init(primaryStage);
        primaryStage.show();
    }

    /**
     * Single application entry point
     *
     * @param args : arguments passed with command line.
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void handleLogLevel() {
        final List<String> parameters = getParameters().getUnnamed();
        if (!parameters.isEmpty()
                && SWITCH_VERBOSE.equals(parameters.get(0))) {

            Log.set(Log.LEVEL_TRACE);

        } else {

            Log.set(Log.LEVEL_INFO);

        }
    }
}

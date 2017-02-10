package fr.tduf.gui.common.javafx.application;

import com.esotericsoftware.minlog.Log;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.common.AppConstants.SWITCH_VERBOSE;

/**
 * Parent of all GUI applications. Provides base services (host, logging, params...)
 */
public abstract class AbstractGuiApp extends Application {

    private static List<String> parameters = new ArrayList<>(0);
    private static HostServices hostServicesInstance = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        parameters = getParameters().getUnnamed();
        hostServicesInstance = getHostServices();

        handleLogLevel();

        startApp(primaryStage);
    }

    protected abstract void startApp(Stage primaryStage) throws Exception;

    private void handleLogLevel() {
        Optional<String> potentialVerboseSwitch = getParameters().getUnnamed().stream()
                .filter(SWITCH_VERBOSE::equals)
                .findAny();

        if (potentialVerboseSwitch.isPresent()) {

            Log.set(Log.LEVEL_TRACE);

        } else {

            Log.set(Log.LEVEL_INFO);

        }
    }

    /**
     * @return All arguments passed with command line, if any.
     */
    public static List<String> getCommandLineParameters() {
        return parameters;
    }

    /**
     * @return Host services. Can be null if not loaded yet.
     */
    public static HostServices getHostServicesInstance() {
        return hostServicesInstance;
    }
}

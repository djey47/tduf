package fr.tduf.gui.common.javafx.application;

import com.esotericsoftware.minlog.Log;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.common.AppConstants.SWITCH_VERBOSE;
import static java.util.Objects.requireNonNull;

/**
 * Parent of all GUI applications. Provides base services (host, logging, params...)
 */
public abstract class AbstractGuiApp extends Application {

    private static List<String> parameters = new ArrayList<>(0);
    private static HostServices hostServicesInstance = null;
    private static AbstractGuiController mainController = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        parameters = getParameters().getUnnamed();
        hostServicesInstance = getHostServices();

        handleLogLevel();

        primaryStage.setOnCloseRequest(onExitHandler());

        startApp(primaryStage);
    }

    protected abstract void startApp(Stage primaryStage) throws Exception;

    protected abstract EventHandler<WindowEvent> onExitHandler();
    
    protected <T extends AbstractGuiController> T retrieveMainController(Class<T> controllerClass) {
        return (T) requireNonNull(mainController, "Main controller instance has not been set yet!");
    }

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

    public static void setMainController(AbstractGuiController controller) {
        mainController = controller;
    }    
}

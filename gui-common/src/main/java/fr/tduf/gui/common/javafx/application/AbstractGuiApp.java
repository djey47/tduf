package fr.tduf.gui.common.javafx.application;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.common.AppConstants.SWITCH_VERBOSE;
import static java.util.Objects.requireNonNull;

/**
 * Parent of all GUI applications. Provides base services (host, logging, params...)
 */
public abstract class AbstractGuiApp extends Application {
    private static final String THIS_CLASS_NAME = AbstractGuiApp.class.getSimpleName();

    // Singleton here to allow access from any controller
    private static AbstractGuiApp instance;

    private List<String> parameters = new ArrayList<>(0);
    private HostServices hostServicesInstance = null;
    private AbstractGuiController mainController = null;
    @SuppressWarnings("FieldMayBeFinal") // for mocking
    private ApplicationConfiguration applicationConfiguration;

    public AbstractGuiApp(){
        applicationConfiguration = new ApplicationConfiguration();
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        parameters = getParameters().getUnnamed();
        hostServicesInstance = getHostServices();

        initConfiguration();

        handleLogLevel();

        primaryStage.setOnCloseRequest(onExitHandler());

        startApp(primaryStage);
    }

    protected abstract void startApp(Stage primaryStage) throws Exception;

    protected abstract EventHandler<WindowEvent> onExitHandler();
    
    protected <T extends AbstractGuiController> T retrieveMainController(Class<T> controllerClass) {
        //noinspection unchecked
        return (T) requireNonNull(mainController, "Main controller instance has not been set yet!");
    }

    private void initConfiguration() throws IOException {
        applicationConfiguration.load();
    }

    private void handleLogLevel() {
        Optional<String> potentialVerboseSwitch = getParameters().getUnnamed().stream()
                .filter(SWITCH_VERBOSE::equals)
                .findAny();
        int effectiveLogLevel = Log.LEVEL_INFO;

        if (potentialVerboseSwitch.isPresent()) {
            effectiveLogLevel = Log.LEVEL_TRACE;
            Log.debug(THIS_CLASS_NAME, "/!\\ TRACE mode enabled via verbosecommand line switch /!\\");
        } else if (applicationConfiguration.isEditorDebuggingEnabled()) {
            effectiveLogLevel = Log.LEVEL_DEBUG;
            Log.debug(THIS_CLASS_NAME, "/!\\ DEBUG mode enabled via application configuration /!\\");
        }
        Log.set(effectiveLogLevel);
    }

    /**
     * @return All arguments passed with command line, if any.
     */
    public List<String> getCommandLineParameters() {
        return parameters;
    }

    /**
     * @return Host services. Can be null if not loaded yet.
     */
    public HostServices getHostServicesInstance() {
        return hostServicesInstance;
    }

    /**
     * @return Application configuration
     */
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public void setMainController(AbstractGuiController controller) {
        mainController = controller;
    }

    /**
     * @return GUI application instance singleton
     */
    public static AbstractGuiApp getInstance() {
        return instance;
    }
}

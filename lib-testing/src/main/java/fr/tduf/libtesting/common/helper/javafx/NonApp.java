package fr.tduf.libtesting.common.helper.javafx;

import com.esotericsoftware.minlog.Log;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Fake application allowing to initialize FX toolkit in tests.
 * Must be used in Junit5's @BeforeAll as rules are not supported anymore .
 */
// TODO see to replace with testFX
public class NonApp extends Application {
    private static final Class<NonApp> thisClass = NonApp.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    @Override
    public void start(Stage primaryStage) throws Exception {}

    /**
     * Creates Java FX main thread for testing
     */
    public static void initJavaFX() {
        Thread t = new Thread("JavaFX Init Thread") {
            public void run() {
                Log.info(THIS_CLASS_NAME, "Initializing FX thread...");
                launch(thisClass);
            }
        };
        t.setDaemon(true);
        t.start();
    }
}

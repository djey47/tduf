package fr.tduf.gui.common.javafx.application;

import com.esotericsoftware.minlog.Log;
import com.sun.javafx.application.ParametersImpl;
import fr.tduf.libtesting.common.helper.ConsoleHelper;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

class AbstractGuiAppTest extends ApplicationTest {
    private static final String[] ARGS = new String[]{"arg1", "arg2", "arg3"};
    private static final String[] ARGS_VERBOSE = new String[]{"arg1", "-v", "arg3"};

    @Mock
    private MiniSpy spy;

    @InjectMocks
    private TestApp testApp;

    private OutputStream consoleOutputStream;

    @BeforeEach
    void setUp() {
        initMocks(this);

        consoleOutputStream = ConsoleHelper.hijackStandardOutput();
    }

    @Override
    public void start(Stage stage) throws Exception {}

    @AfterEach
    void tearDown() {
        ConsoleHelper.restoreOutput();
    }

    @Test
    void start_withArgs_should_invokeStartApp_andGetArgs() throws Exception {
        // GIVEN
        ParametersImpl.registerParameters(testApp, createParams(ARGS));

        // WHEN-THEN
        interact(() -> {
            Stage primaryStage =  new Stage();
            try {
                testApp.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            verify(spy).invoke(primaryStage);
        });
        assertThat(TestApp.getCommandLineParameters()).containsExactly(ARGS);
    }

    @Test
    void start_withVerboseSwitch_should_setLogLevelToTrace() throws Exception {
        // GIVEN
        ParametersImpl.registerParameters(testApp, createParams(ARGS_VERBOSE));

        // WHEN
        interact(() -> {
            try {
                testApp.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Log.trace("trace");
        Log.info("info");

        // THEN
        final String consoleContents = ConsoleHelper.finalizeAndGetContents(consoleOutputStream);
        assertThat(consoleContents)
                .contains("trace")
                .contains("info");
    }

    @Test
    void start_withoutVerboseSwitch_should_setLogLevelToInfo() throws Exception {
        // GIVEN
        ParametersImpl.registerParameters(testApp, createParams(ARGS));

        // WHEN
        interact(() -> {
            try {
                testApp.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Log.trace("trace");
        Log.info("info");

        // THEN
        final String consoleContents = ConsoleHelper.finalizeAndGetContents(consoleOutputStream);
        assertThat(consoleContents)
                .doesNotContain("trace")
                .contains("info");
    }

    private static class TestApp extends AbstractGuiApp {

        private final MiniSpy spy;

        private TestApp(MiniSpy spy) {
            this.spy = spy;
        }

        @Override
        protected void startApp(Stage primaryStage) throws Exception {
            spy.invoke(primaryStage);
        }

        @Override
        protected EventHandler<WindowEvent> onExitHandler() {
            return event -> {};
        }
    }

    private interface MiniSpy {
        void invoke(Stage stage);
    }

    private static Application.Parameters createParams(String... args ) {
        return new Application.Parameters() {
            @Override
            public List<String> getRaw() {
                return new ArrayList<>();
            }

            @Override
            public List<String> getUnnamed() {
                return asList(args);
            }


            @Override
            public Map<String, String> getNamed() {
                return new HashMap<>();
            }
        };
    }
}

package fr.tduf.gui.common.javafx.application;

import com.esotericsoftware.minlog.Log;
import com.sun.javafx.application.ParametersImpl;
import fr.tduf.libtesting.common.helper.ConsoleHelper;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AbstractGuiAppTest {
    private static final String[] ARGS = new String[]{"arg1", "arg2", "arg3"};
    private static final String[] ARGS_VERBOSE = new String[]{"arg1", "-v", "arg3"};

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Mock
    private MiniSpy spy;

    @InjectMocks
    private TestApp testApp;

    private OutputStream consoleOutputStream;

    @Before
    public void setUp() {
        consoleOutputStream = ConsoleHelper.hijackStandardOutput();
    }

    @After
    public void tearDown() {
        ConsoleHelper.restoreOutput();
    }

    @Test
    public void start_withArgs_should_invokeStartApp_andGetArgs() throws Exception {
        // GIVEN
        ParametersImpl.registerParameters(testApp, createParams(ARGS));
        final Stage primaryStage = new Stage();

        // WHEN
        testApp.start(primaryStage);

        // THEN
        verify(spy).invoke(primaryStage);

        assertThat(TestApp.getCommandLineParameters()).containsExactly(ARGS);
    }

    @Test
    public void start_withVerboseSwitch_should_setLogLevelToTrace() throws Exception {
        // GIVEN
        ParametersImpl.registerParameters(testApp, createParams(ARGS_VERBOSE));

        // WHEN
        testApp.start(new Stage());
        Log.trace("trace");
        Log.info("info");

        // THEN
        final String consoleContents = ConsoleHelper.finalizeAndGetContents(consoleOutputStream);
        assertThat(consoleContents)
                .contains("trace")
                .contains("info");
    }

    @Test
    public void start_withoutVerboseSwitch_should_setLogLevelToInfo() throws Exception {
        // GIVEN
        ParametersImpl.registerParameters(testApp, createParams(ARGS));

        // WHEN
        testApp.start(new Stage());
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

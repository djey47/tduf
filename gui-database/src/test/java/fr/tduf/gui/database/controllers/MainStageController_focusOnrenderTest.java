package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.stages.MainStageDesigner;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class MainStageController_focusOnrenderTest {

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_TRACE);
    }

    @Test
    public void display() throws Exception {
        // GIVEN-WHEN
        initMainStageController().showAndWait();
    }

    private static Stage initMainStageController() throws IOException {
        Stage stage = new Stage();

        MainStageDesigner.init(stage);

        return stage;
    }
}

package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.MainStageDesigner;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

@Disabled
class MainStageControllerTest extends ApplicationTest{
    @Override
    public void start(Stage stage) throws Exception {}

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_TRACE);
    }

    @Test
    void display() throws Exception {
        // GIVEN-WHEN
        interact(() -> {
            try {
                initMainStageController().showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static Stage initMainStageController() throws IOException {
        Stage stage = new Stage();

        MainStageDesigner.init(stage);

        return stage;
    }
}

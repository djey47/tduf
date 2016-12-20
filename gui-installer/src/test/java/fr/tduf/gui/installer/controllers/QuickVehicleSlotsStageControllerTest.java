package fr.tduf.gui.installer.controllers;


import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.QuickVehicleSlotsStageDesigner;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * To display stage without running whole application.
 */
@Ignore
public class QuickVehicleSlotsStageControllerTest {
    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    private List<DbDto> databaseObjects;

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_TRACE);

        databaseObjects = DatabaseHelper.createDatabaseForReadOnly();
    }

    @Test
    public void display() throws Exception {
        // GIVEN
        QuickVehicleSlotsStageController controller = initQuickSlotsBrowserStageController(null);

        // WHEN
        controller.initAndShowModalDialog(BulkDatabaseMiner.load(databaseObjects));

        // THEN
        System.out.println("Selected slot: " + controller.selectedSlotProperty().getValue());
    }

    private static QuickVehicleSlotsStageController initQuickSlotsBrowserStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return QuickVehicleSlotsStageDesigner.init(stage);
    }
}

package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.exceptions.AbortedInteractiveStepException;
import fr.tduf.gui.installer.stages.QuickVehicleSlotsStageDesigner;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.util.List;

/**
 * To display stage without running whole application.
 */
@Disabled
class QuickVehicleSlotsStageControllerTest extends ApplicationTest {
    private List<DbDto> databaseObjects;

    @Override
    public void start(Stage stage) throws Exception {

    }

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_TRACE);

        databaseObjects = DatabaseHelper.createDatabase();
    }

    @Test
    void display() throws Exception {
        // GIVEN-WHEN-THEN
        interact(() -> {
            try {
                final QuickVehicleSlotsStageController controller = initQuickSlotsBrowserStageController();
                controller.initAndShowModalDialog(BulkDatabaseMiner.load(databaseObjects));
                System.out.println("Selected slot: " + controller.selectedSlotProperty().getValue());
            } catch (AbortedInteractiveStepException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static QuickVehicleSlotsStageController initQuickSlotsBrowserStageController() throws IOException {
        Stage stage = new Stage();
        stage.initOwner(null);

        return QuickVehicleSlotsStageDesigner.init(stage);
    }
}

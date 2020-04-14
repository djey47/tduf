package fr.tduf.gui.installer.controllers;


import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.VehicleSlotsStageDesigner;
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
@Disabled("Interactive testing - can't be asserted automatically")
class VehicleSlotsStageControllerTest extends ApplicationTest {
    private List<DbDto> databaseObjects;

    @Override
    public void start(Stage stage) {

    }

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_TRACE);

        databaseObjects = DatabaseHelper.createDatabase();
    }

    @Test
    void display() {
        // GIVEN-WHEN
        interact(() -> initSlotsBrowserStageController().initAndShowModalDialog(BulkDatabaseMiner.load(databaseObjects)));
    }

    private static VehicleSlotsStageController initSlotsBrowserStageController() throws IOException {
        Stage stage = new Stage();
        stage.initOwner(null);

        return VehicleSlotsStageDesigner.init(stage);
    }
}

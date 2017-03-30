package fr.tduf.gui.installer.controllers;


import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.DealerSlotsStageDesigner;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.stage.Stage;
import javafx.stage.Window;
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
class DealerSlotsStageControllerTest extends ApplicationTest {
    private List<DbDto> databaseObjects;

    @Override
    public void start(Stage stage) throws Exception {}

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_TRACE);

        databaseObjects = DatabaseHelper.createDatabaseForReadOnly();
    }

    @Test
    void display() throws Exception {
        // GIVEN-WHEN
        interact(() -> initDealerSlotsStageController(null).initAndShowModalDialog(BulkDatabaseMiner.load(databaseObjects)));
    }

    private static DealerSlotsStageController initDealerSlotsStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DealerSlotsStageDesigner.init(stage);
    }
}

package fr.tduf.gui.installer.controllers;


import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.stages.DealerSlotsStageDesigner;
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
public class DealerSlotsStageControllerTest {
    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    private List<DbDto> databaseObjects;

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_TRACE);

        databaseObjects = InstallerTestsHelper.createDatabaseForReadOnly();
    }

    @Test
    public void display() throws Exception {
        // GIVEN-WHEN
        initDealerSlotsStageController(null).initAndShowModalDialog(BulkDatabaseMiner.load(databaseObjects));
    }

    private static DealerSlotsStageController initDealerSlotsStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DealerSlotsStageDesigner.init(stage);
    }
}

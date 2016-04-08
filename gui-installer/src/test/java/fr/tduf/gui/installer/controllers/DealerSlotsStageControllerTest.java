package fr.tduf.gui.installer.controllers;


import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.DealerSlotsStageDesigner;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * To display stage without running whole application.
 */
@Ignore
public class DealerSlotsStageControllerTest {
    private static final Class<SlotsBrowserStageControllerTest> thisClass = SlotsBrowserStageControllerTest.class;

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    private List<DbDto> databaseObjects;

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_TRACE);

        Path jsonDatabasePath = Paths.get(thisClass.getResource("/db-json").getFile());
        databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabasePath.toString());
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
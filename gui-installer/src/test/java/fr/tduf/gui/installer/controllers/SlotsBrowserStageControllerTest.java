package fr.tduf.gui.installer.controllers;


import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.stages.SlotsBrowserStageDesigner;
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
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * To display stage without running whole application.
 */
@Ignore
public class SlotsBrowserStageControllerTest {

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
    public void display_whenEmptySlotRef() throws Exception {
        // GIVEN-WHEN
        initSlotsBrowserStageController(null).initAndShowModalDialog(empty(), BulkDatabaseMiner.load(databaseObjects));
    }

    @Test
    public void display_whenProvidedSlotRef() throws Exception {
        // GIVEN-WHEN
        initSlotsBrowserStageController(null).initAndShowModalDialog(Optional.of("698882776"), BulkDatabaseMiner.load(databaseObjects));
    }

    private static SlotsBrowserStageController initSlotsBrowserStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return SlotsBrowserStageDesigner.init(stage);
    }
}

package fr.tduf.gui.common.controllers.helper;

import fr.tduf.gui.common.controllers.DatabaseCheckStageController;
import fr.tduf.gui.common.stages.DatabaseCheckStageDesigner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Set;

public class DatabaseOpsHelper {
    public static boolean displayCheckResultDialog(Set<IntegrityError> integrityErrors, Window window, String applicationTitle) {
        try {
            DatabaseCheckStageController databaseCheckStageController = initDatabaseCheckStageController(window, applicationTitle);
            return databaseCheckStageController.initAndShowModalDialog(integrityErrors);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static DatabaseCheckStageController initDatabaseCheckStageController(Window mainWindow, String applicationTitle) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DatabaseCheckStageDesigner.init(stage, applicationTitle);
    }

    private DatabaseOpsHelper() {}
}

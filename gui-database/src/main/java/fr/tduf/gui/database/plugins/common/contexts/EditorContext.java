package fr.tduf.gui.database.plugins.common.contexts;

import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import javafx.stage.Window;

/**
 * Contains application global information for correct plugin execution
 */
public class EditorContext {
    private String databaseLocation;
    private String gameLocation;

    private MainStageController mainStageController;
    private MainStageChangeDataController changeDataController;
    private Window mainWindow;

    private BulkDatabaseMiner miner;

    public MainStageChangeDataController getChangeDataController() {
        return changeDataController;
    }

    public void setChangeDataController(MainStageChangeDataController changeDataController) {
        this.changeDataController = changeDataController;
    }

    public void setDatabaseLocation(String databaseLocation) {
        this.databaseLocation = databaseLocation;
    }

    public String getDatabaseLocation() {
        return databaseLocation;
    }

    public void setMainWindow(Window mainWindow) {
        this.mainWindow = mainWindow;
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public String getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(String gameLocation) {
        this.gameLocation = gameLocation;
    }

    public BulkDatabaseMiner getMiner() {
        return miner;
    }

    public void setMiner(BulkDatabaseMiner miner) {
        this.miner = miner;
    }

    public MainStageController getMainStageController() {
        return mainStageController;
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }
}

package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.cameras.CamerasContext;
import fr.tduf.gui.database.plugins.iks.IKsContext;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Window;

/**
 * Contains all information for correct plugin execution
 */
public class EditorContext implements PluginContext {
    private DbDto.Topic currentTopic;
    private int fieldRank;
    private boolean fieldReadOnly;
    private String databaseLocation;
    private MainStageChangeDataController changeDataController;
    private Window mainWindow;

    private StringProperty rawValueProperty;
    private StringProperty errorMessageProperty;
    private BooleanProperty errorProperty;

    private CamerasContext camerasContext = new CamerasContext();
    private IKsContext iksContext = new IKsContext();

    @Override
    public void reset() {
        camerasContext.reset();
    }

    public DbDto.Topic getCurrentTopic() {
        return currentTopic;
    }

    public void setCurrentTopic(DbDto.Topic currentTopic) {
        this.currentTopic = currentTopic;
    }

    public int getFieldRank() {
        return fieldRank;
    }

    public void setFieldRank(int fieldRank) {
        this.fieldRank = fieldRank;
    }

    public boolean isFieldReadOnly() {
        return fieldReadOnly;
    }

    public StringProperty getRawValueProperty() {
        return rawValueProperty;
    }

    public MainStageChangeDataController getChangeDataController() {
        return changeDataController;
    }

    void setChangeDataController(MainStageChangeDataController changeDataController) {
        this.changeDataController = changeDataController;
    }

    public void setFieldReadOnly(boolean fieldReadOnly) {
        this.fieldReadOnly = fieldReadOnly;
    }

    public void setRawValueProperty(StringProperty rawValueProperty) {
        this.rawValueProperty = rawValueProperty;
    }

    public CamerasContext getCamerasContext() {
        return camerasContext;
    }

    public void setDatabaseLocation(String databaseLocation) {
        this.databaseLocation = databaseLocation;
    }

    public String getDatabaseLocation() {
        return databaseLocation;
    }

    public StringProperty getErrorMessageProperty() {
        return errorMessageProperty;
    }

    public void setErrorMessageProperty(StringProperty errorMessageProperty) {
        this.errorMessageProperty = errorMessageProperty;
    }

    public BooleanProperty getErrorProperty() {
        return errorProperty;
    }

    public void setErrorProperty(BooleanProperty errorProperty) {
        this.errorProperty = errorProperty;
    }

    public void setMainWindow(Window mainWindow) {
        this.mainWindow = mainWindow;
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public IKsContext getIKsContext() {
        return iksContext;
    }
}

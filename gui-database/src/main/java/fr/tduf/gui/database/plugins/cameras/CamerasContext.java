package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext implements PluginContext {
    private String binaryFileLocation = null;
    private boolean pluginLoaded = false;

    private StringProperty errorMessageProperty = null;
    private BooleanProperty errorProperty = null;

    @Override
    public void reset() {
        binaryFileLocation = null;
        pluginLoaded = false;

        errorMessageProperty = null;
        errorProperty = null;
    }

    void setBinaryFileLocation(String binaryFileLocation) {
        this.binaryFileLocation = binaryFileLocation;
    }

    String getBinaryFileLocation() {
        return binaryFileLocation;
    }

    void setPluginLoaded(boolean pluginLoaded) {
        this.pluginLoaded = pluginLoaded;
    }

    boolean isPluginLoaded() {
        return pluginLoaded;
    }

    StringProperty getErrorMessageProperty() {
        return errorMessageProperty;
    }

    void setErrorMessageProperty(StringProperty errorMessageProperty) {
        this.errorMessageProperty = errorMessageProperty;
    }

    BooleanProperty getErrorProperty() {
        return errorProperty;
    }

    void setErrorProperty(BooleanProperty errorProperty) {
        this.errorProperty = errorProperty;
    }
}

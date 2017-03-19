package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

public class MappingContext implements PluginContext {
    private boolean pluginLoaded = false;
    private String binaryFileLocation;

    private StringProperty errorMessageProperty = null;
    private BooleanProperty errorProperty = null;

    @Override
    public void reset() {
        pluginLoaded = false;
    }

    void setPluginLoaded(boolean pluginLoaded) {
        this.pluginLoaded = pluginLoaded;
    }

    boolean isPluginLoaded() {
        return pluginLoaded;
    }

    void setBinaryFileLocation(String binaryFileLocation) {
        this.binaryFileLocation = binaryFileLocation;
    }

    String getBinaryFileLocation() {
        return binaryFileLocation;
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

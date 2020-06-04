package fr.tduf.gui.database.plugins.common.contexts;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * To be extended by any plugin context
 */
public abstract class AbstractPluginContext {
    private boolean pluginLoaded = false;

    private String binaryFileLocation;

    private StringProperty errorMessageProperty = null;

    private BooleanProperty errorProperty = null;

    /**
     * Sets context to default
     */
    public void reset() {
        pluginLoaded = false;
        binaryFileLocation = null;
        errorMessageProperty = null;
        errorProperty = null;
    }

    public void setPluginLoaded(boolean pluginLoaded) {
        this.pluginLoaded = pluginLoaded;
    }

    public boolean isPluginLoaded() {
        return pluginLoaded;
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

    public void setBinaryFileLocation(String binaryFileLocation) {
        this.binaryFileLocation = binaryFileLocation;
    }

    public String getBinaryFileLocation() {
        return binaryFileLocation;
    }
}

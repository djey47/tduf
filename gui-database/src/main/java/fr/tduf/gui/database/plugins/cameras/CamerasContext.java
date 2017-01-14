package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.PluginContext;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext implements PluginContext {
    private String binaryFileLocation = null;
    private boolean pluginLoaded = false;

    private final Property<CamerasParser> camerasParserProperty = new SimpleObjectProperty<>();

    @Override
    public void reset() {
        binaryFileLocation = null;
        pluginLoaded = false;
        camerasParserProperty.setValue(null);
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

    Property<CamerasParser> getCamerasParserProperty() {
        return camerasParserProperty;
    }
}

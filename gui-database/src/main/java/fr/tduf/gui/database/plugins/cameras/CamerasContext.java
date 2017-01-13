package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext {
    private String binaryFileLocation;
    private boolean pluginLoaded = false;

    private final Property<CamerasParser> camerasParserProperty = new SimpleObjectProperty<>();
    private final Property<CameraInfo> currentCameraSetProperty = new SimpleObjectProperty<>();
    private final Property<CameraInfo.CameraView> currentViewProperty = new SimpleObjectProperty<>();

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

    Property<CameraInfo.CameraView> getCurrentViewProperty() {
        return currentViewProperty;
    }

    Property<CameraInfo> getCurrentCameraSetProperty() {
        return currentCameraSetProperty;
    }
}

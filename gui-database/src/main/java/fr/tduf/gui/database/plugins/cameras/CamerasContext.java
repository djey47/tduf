package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext {
    private String binaryFileLocation;
    private boolean pluginLoaded = false;
    private CamerasParser camerasParser;
    private final List<CameraInfo> allCameras = new ArrayList<>();
    private final Property<ViewKind> viewTypeProperty = new SimpleObjectProperty<>();

    List<CameraInfo> getAllCameras() {
        return allCameras;
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

    CamerasParser getCamerasParser() {
        return camerasParser;
    }

    void setCamerasParser(CamerasParser camerasParser) {
        this.camerasParser = camerasParser;
    }

    Property<ViewKind> getViewTypeProperty() {
        return viewTypeProperty;
    }
}

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
// TODO restrict visibility
public class CamerasContext {
    private String binaryFileLocation;
    private List<CameraInfo> allCameras = new ArrayList<>();
    private boolean pluginLoaded = false;
    private CamerasParser camerasParser;
    private Property<ViewKind> viewTypeProperty = new SimpleObjectProperty<>();

    public List<CameraInfo> getAllCameras() {
        return allCameras;
    }

    public void setAllCameras(List<CameraInfo> allCameras) {
        this.allCameras = allCameras;
    }

    public void setBinaryFileLocation(String binaryFileLocation) {
        this.binaryFileLocation = binaryFileLocation;
    }

    public String getBinaryFileLocation() {
        return binaryFileLocation;
    }

    public void setPluginLoaded(boolean pluginLoaded) {
        this.pluginLoaded = pluginLoaded;
    }

    public boolean isPluginLoaded() {
        return pluginLoaded;
    }

    public CamerasParser getCamerasParser() {
        return camerasParser;
    }

    public void setCamerasParser(CamerasParser camerasParser) {
        this.camerasParser = camerasParser;
    }

    public Property<ViewKind> getViewTypeProperty() {
        return viewTypeProperty;
    }

    public void setViewTypeProperty(Property<ViewKind> viewTypeProperty) {
        this.viewTypeProperty = viewTypeProperty;
    }
}

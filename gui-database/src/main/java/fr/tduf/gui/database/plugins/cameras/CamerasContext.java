package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext {
    private String binaryFileLocation;
    private List<CameraInfo> allCameras = new ArrayList<>();

    public List<CameraInfo> getAllCameras() {
        return allCameras;
    }

    public void setAllCameras(List<CameraInfo> allCameras) {
        this.allCameras = allCameras;
    }

    public void setBinaryFileLocation(String binaryFileLocation) {
        this.binaryFileLocation = binaryFileLocation;
    }
}

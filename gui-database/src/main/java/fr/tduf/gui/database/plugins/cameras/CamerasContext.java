package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;

import java.util.List;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext {
    private List<CameraInfo> allCameras;

    public List<CameraInfo> getAllCameras() {
        return allCameras;
    }

    public void setAllCameras(List<CameraInfo> allCameras) {
        this.allCameras = allCameras;
    }
}

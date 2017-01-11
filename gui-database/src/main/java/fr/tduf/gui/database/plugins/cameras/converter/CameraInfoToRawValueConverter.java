package fr.tduf.gui.database.plugins.cameras.converter;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class CameraInfoToRawValueConverter extends StringConverter<CameraInfo> {
    private static final String THIS_CLASS_NAME = CameraInfoToRawValueConverter.class.getSimpleName();

    private final ObservableList<CameraInfo> allCameras;

    public CameraInfoToRawValueConverter(ObservableList<CameraInfo> allCameras) {
        this.allCameras = allCameras;
    }

    @Override
    public String toString(CameraInfo cameraInfo) {
        if (cameraInfo == null) {
            return DisplayConstants.LABEL_CAMERA_RAW_VALUE_DEFAULT;
        }
        return Long.toString(cameraInfo.getCameraIdentifier());
    }

    @Override
    public CameraInfo fromString(String cameraIdentifierAsString) {
        try {
            long cameraId = Long.valueOf(cameraIdentifierAsString);
            return allCameras.stream()
                    .filter(camera -> camera.getCameraIdentifier() == cameraId)
                    .findAny()
                    .orElse(null);
        } catch (NumberFormatException nfe) {
            Log.error(THIS_CLASS_NAME, "Unable to resolve camera info from raw value: " + cameraIdentifierAsString, nfe);
            return null;
        }
    }
}

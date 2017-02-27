package fr.tduf.gui.database.plugins.cameras.converter;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class CameraInfoToRawValueConverter extends StringConverter<CameraSetInfo> {
    private static final String THIS_CLASS_NAME = CameraInfoToRawValueConverter.class.getSimpleName();

    private final ObservableList<CameraSetInfo> allCameras;

    public CameraInfoToRawValueConverter(ObservableList<CameraSetInfo> allCameras) {
        this.allCameras = allCameras;
    }

    @Override
    public String toString(CameraSetInfo cameraSetInfo) {
        if (cameraSetInfo == null) {
            return DisplayConstants.LABEL_CAMERA_RAW_VALUE_DEFAULT;
        }
        return Long.toString(cameraSetInfo.getCameraIdentifier());
    }

    @Override
    public CameraSetInfo fromString(String cameraIdentifierAsString) {
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

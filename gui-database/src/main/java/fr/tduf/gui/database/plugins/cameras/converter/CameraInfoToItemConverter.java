package fr.tduf.gui.database.plugins.cameras.converter;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_CAMERA_ITEM;

public class CameraInfoToItemConverter extends StringConverter<CameraInfo> {
    @Override
    public String toString(CameraInfo cameraInfo) {
        // TODO add metadata
        return String.format(LABEL_FORMAT_CAMERA_ITEM, cameraInfo.getCameraIdentifier(), cameraInfo.getViews().size());
    }

    @Override
    public CameraInfo fromString(String cameraIdentifierAsString) {
        return null;
    }
}

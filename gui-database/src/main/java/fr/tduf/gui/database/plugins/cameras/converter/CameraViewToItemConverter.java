package fr.tduf.gui.database.plugins.cameras.converter;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_VIEW_ITEM;

public class CameraViewToItemConverter extends StringConverter<CameraInfo.CameraView> {
    @Override
    public String toString(CameraInfo.CameraView cameraView) {
        return String.format(LABEL_FORMAT_VIEW_ITEM, cameraView.getType().getInternalId(), cameraView.getType().name());
    }

    @Override
    public CameraInfo.CameraView fromString(String cameraIdentifierAsString) {
        return null;
    }
}

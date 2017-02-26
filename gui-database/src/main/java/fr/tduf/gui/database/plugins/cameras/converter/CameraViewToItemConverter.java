package fr.tduf.gui.database.plugins.cameras.converter;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraViewEnhanced;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_VIEW_ITEM;

public class CameraViewToItemConverter extends StringConverter<CameraViewEnhanced> {
    @Override
    public String toString(CameraViewEnhanced cameraView) {
        return String.format(LABEL_FORMAT_VIEW_ITEM, cameraView.getKind().getInternalId(), cameraView.getKind().name());
    }

    @Override
    public CameraViewEnhanced fromString(String cameraIdentifierAsString) {
        return null;
    }
}

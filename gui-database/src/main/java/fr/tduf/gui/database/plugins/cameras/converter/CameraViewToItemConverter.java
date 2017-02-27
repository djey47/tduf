package fr.tduf.gui.database.plugins.cameras.converter;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_VIEW_ITEM;

public class CameraViewToItemConverter extends StringConverter<CameraView> {
    @Override
    public String toString(CameraView cameraView) {
        return String.format(LABEL_FORMAT_VIEW_ITEM, cameraView.getKind().getInternalId(), cameraView.getKind().name());
    }

    @Override
    public CameraView fromString(String cameraIdentifierAsString) {
        return null;
    }
}

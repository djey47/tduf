package fr.tduf.gui.database.plugins.cameras.converter;

import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.libunlimited.high.files.db.common.helper.IKHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_CAMERA_ITEM;

public class CameraInfoToItemConverter extends StringConverter<CameraInfo> {
    private final IKHelper cameraRefHelper;

    public CameraInfoToItemConverter(IKHelper cameraRefHelper) {
        this.cameraRefHelper = cameraRefHelper;
    }

    @Override
    public String toString(CameraInfo cameraInfo) {
        long cameraIdentifier = cameraInfo.getCameraIdentifier();
        String vehicleName = cameraRefHelper.getCameraReference().getOrDefault(cameraIdentifier, DisplayConstants.LABEL_NEW_SET);
        return String.format(LABEL_FORMAT_CAMERA_ITEM, vehicleName, cameraIdentifier, cameraInfo.getViews().size());
    }

    @Override
    public CameraInfo fromString(String cameraIdentifierAsString) {
        return null;
    }
}

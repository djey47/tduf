package fr.tduf.gui.database.plugins.cameras.converter;

import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.libunlimited.high.files.db.common.helper.CameraAndIKHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_CAMERA_ITEM;

public class CameraInfoToItemConverter extends StringConverter<CameraSetInfo> {
    private final CameraAndIKHelper cameraRefHelper;

    public CameraInfoToItemConverter(CameraAndIKHelper cameraRefHelper) {
        this.cameraRefHelper = cameraRefHelper;
    }

    @Override
    public String toString(CameraSetInfo cameraSetInfo) {
        long cameraIdentifier = cameraSetInfo.getCameraIdentifier();
        String vehicleName = cameraRefHelper.getCameraReference().getOrDefault(cameraIdentifier, DisplayConstants.LABEL_NEW_SET);
        return String.format(LABEL_FORMAT_CAMERA_ITEM, vehicleName, cameraIdentifier, cameraSetInfo.getViews().size());
    }

    @Override
    public CameraSetInfo fromString(String cameraIdentifierAsString) {
        return null;
    }
}

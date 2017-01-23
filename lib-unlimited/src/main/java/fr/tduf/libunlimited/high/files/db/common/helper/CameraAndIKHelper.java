package fr.tduf.libunlimited.high.files.db.common.helper;

import java.util.Map;

/**
 * Gives access to embedded cameras and inverse kinematics reference
 */
public class CameraAndIKHelper extends MetaDataHelper {
    public Map<Integer, String> getIKReference() {
        return databaseMetadataObject.getIKs();
    }

    public Map<Long, String> getCameraReference() {
        return databaseMetadataObject.getCameras();
    }
}

package fr.tduf.libunlimited.high.files.db.common.helper;

import java.util.Map;

// TODO Rename to CameraAndIKHelper
public class IKHelper extends MetaDataHelper {
    // TODO Rename to getIKReference
    public Map<Integer, String> getReference() {
        return databaseMetadataObject.getIKs();
    }

    public Map<Long, String> getCameraReference() {
        return databaseMetadataObject.getCameras();
    }
}

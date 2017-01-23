package fr.tduf.libunlimited.high.files.db.common.helper;

import java.util.Map;

public class IKHelper extends MetaDataHelper {
    public Map<Integer, String> getReference() {
        return this.databaseMetadataObject.getIKs();
    }
}

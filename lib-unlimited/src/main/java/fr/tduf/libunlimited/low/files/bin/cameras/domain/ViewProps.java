package fr.tduf.libunlimited.low.files.bin.cameras.domain;

/**
 * All handled view properties
 */
// TODO extend data store enum type (field name support)
public enum ViewProps {
    TYPE("type");

    private String storeFieldName;

    ViewProps(String storeFieldName) {
        this.storeFieldName = storeFieldName;
    }

    public String getStoreFieldName() {
        return storeFieldName;
    }

    @Override
    public String toString() {
        return storeFieldName;
    }
}

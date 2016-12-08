package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import java.util.stream.Stream;

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

    public static Stream<ViewProps> valuesStream() {
        return Stream.of(values());
    }

    @Override
    public String toString() {
        return storeFieldName;
    }
}

package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.EnumMap;

/**
 * Parsed views settings from cameras database
 */
public class CameraViewEnhanced {
    private DataStore originalDataStore;

    private ViewKind kind;

    private int cameraSetId;

    private String label;
    private String name;

    private EnumMap<ViewProps, Object> settings;

    private CameraViewEnhanced() {}

    public static CameraViewEnhancedBuilder builder() {
        return new CameraViewEnhancedBuilder();
    }

    public EnumMap<ViewProps, Object> getSettings() {
        return settings;
    }

    public static class CameraViewEnhancedBuilder {
        private DataStore originalDataStore;
        private int setId;
        private ViewKind kind;
        private String label;
        private String name;

        public CameraViewEnhancedBuilder fromDatastore(DataStore originalDataStore) {
            this.originalDataStore = originalDataStore;
            return this;
        }

        public CameraViewEnhancedBuilder forCameraSetId(int setId) {
            this.setId = setId;
            return this;
        }

        public CameraViewEnhancedBuilder ofKind(ViewKind kind) {
            this.kind = kind;
            return this;
        }

        public CameraViewEnhancedBuilder withLabel(String label) {
            this.label = label;
            return this;
        }

        public CameraViewEnhancedBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CameraViewEnhanced build() {
            CameraViewEnhanced cameraViewEnhanced = new CameraViewEnhanced();

            cameraViewEnhanced.cameraSetId = setId;
            cameraViewEnhanced.kind = kind;
            cameraViewEnhanced.label = label;
            cameraViewEnhanced.name = name;
            cameraViewEnhanced.originalDataStore = originalDataStore;

            return cameraViewEnhanced;
        }
    }
}

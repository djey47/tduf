package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.apache.commons.lang3.SerializationUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.EnumMap;

import static java.util.Objects.requireNonNull;

/**
 * Parsed views settings from cameras database
 */
public class CameraViewEnhanced {
    @JsonIgnore
    private DataStore originalDataStore;

    private ViewKind kind;

    private int cameraSetId;

    private String label;

    private String name;

    private EnumMap<ViewProps, Object> settings;

    private CameraViewEnhanced() {}

    /**
     * @return unique way to get a view instance
     */
    public static CameraViewEnhancedBuilder builder() {
        return new CameraViewEnhancedBuilder();
    }

    /**
     * @return a real copy of current view, for specified set identifier
     */
    public CameraViewEnhanced cloneForNewViewSet(int setIdentifier) {
        return builder()
                .forCameraSetId(setIdentifier)
                .fromDatastore(originalDataStore.copy())
                .ofKind(kind)
                .withLabel(label)
                .withName(name)
                .withSettings(cloneSettings(settings))
                .build();
    }

    private EnumMap<ViewProps, Object> cloneSettings(EnumMap<ViewProps, Object> sourceSettings) {
        EnumMap<ViewProps, Object> targetSettings = new EnumMap<>(ViewProps.class);
        sourceSettings.entrySet()
                .forEach(settingsEntry -> {
                    Serializable clonedValue = SerializationUtils.clone((Serializable) settingsEntry.getValue());
                    targetSettings.put(settingsEntry.getKey(), clonedValue);
                });
        return targetSettings;
    }

    public EnumMap<ViewProps, Object> getSettings() {
        return settings;
    }

    public int getCameraSetId() {
        return cameraSetId;
    }

    public ViewKind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public DataStore getOriginalDataStore() {
        return originalDataStore;
    }

    public static class CameraViewEnhancedBuilder {
        private DataStore originalDataStore;
        private int setId;
        private ViewKind kind;
        private String label;
        private String name;
        private EnumMap<ViewProps, Object> settings;

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

        public CameraViewEnhancedBuilder withSettings(EnumMap<ViewProps, Object> settings) {
            this.settings = settings;
            return this;
        }

        public CameraViewEnhanced build() {
            CameraViewEnhanced cameraViewEnhanced = new CameraViewEnhanced();

            cameraViewEnhanced.cameraSetId = setId;
            cameraViewEnhanced.kind = (kind == null ? ViewKind.Unknown : kind);
            cameraViewEnhanced.label = (label == null ? "" : label);
            cameraViewEnhanced.name = (name == null ? "" : name);
            cameraViewEnhanced.originalDataStore = requireNonNull(originalDataStore, "Original data store is required");
            cameraViewEnhanced.settings = requireNonNull(settings, "Settings are required");

            return cameraViewEnhanced;
        }
    }
}

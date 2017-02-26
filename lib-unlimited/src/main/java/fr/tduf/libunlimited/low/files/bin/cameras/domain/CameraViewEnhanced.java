package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.apache.commons.lang3.SerializationUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * Parsed views settings from cameras database
 */
// TODO add use sets info (source and target)
public class CameraViewEnhanced {
    @JsonIgnore
    private DataStore originalDataStore;

    private ViewKind kind;

    private ViewKind usedKind;

    private int cameraSetId;

    private Integer usedCameraSetId;

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

    /**
     * @return a new instance with properties from another camera view
     */
    public static CameraViewEnhanced from(ViewKind kind, int usedSetIdentifier, ViewKind usedViewKind) {
        return CameraViewEnhanced.builder()
                .ofKind(kind)
                .usingSettingsFrom(usedSetIdentifier, usedViewKind)
                .build();
    }

    /**
     * @return a new instance with specified properties
     */
    // TODO set identifier ??
    public static CameraViewEnhanced fromProps(EnumMap<ViewProps, Object> viewProps, ViewKind viewKind) {
        return CameraViewEnhanced.builder()
                .ofKind(viewKind)
                .withSettings(viewProps)
                .build();
    }

    /**
     * @return a new instance from patch properties
     */
    // TODO set identifier ??
    public static CameraViewEnhanced fromPatchProps(EnumMap<ViewProps, String> patchProps, ViewKind viewKind) {
        //noinspection Convert2Diamond (type args needed by compiler)
        EnumMap<ViewProps, Object> props = new EnumMap<ViewProps, Object>(
                patchProps.entrySet().stream()
                        .collect(toMap(
                                Map.Entry::getKey,
                                entry -> Long.valueOf(entry.getValue()))
                        ));
        return CameraViewEnhanced.builder()
                .ofKind(viewKind)
                .withSettings(props)
                .build();
    }

    void setUsedSettings(CameraViewEnhanced usedView) {
        usedCameraSetId = usedView.usedCameraSetId;
        usedKind = usedView.usedKind;
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

    public ViewKind getUsedKind() {
        return usedKind;
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

    public Integer getUsedCameraSetId() {
        return usedCameraSetId;
    }

    public static class CameraViewEnhancedBuilder {
        private DataStore originalDataStore;
        private int setId;
        private ViewKind kind;
        private String label;
        private String name;
        private EnumMap<ViewProps, Object> settings;
        private ViewKind usedKind;
        private int usedSetId;

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

        public CameraViewEnhancedBuilder usingSettingsFrom(int usedSetIdentifier, ViewKind usedViewKind) {
            this.usedSetId = usedSetIdentifier;
            this.usedKind = usedViewKind;
            return this;
        }

        public CameraViewEnhanced build() {
            CameraViewEnhanced cameraViewEnhanced = new CameraViewEnhanced();

            cameraViewEnhanced.cameraSetId = setId;
            cameraViewEnhanced.kind = (kind == null ? ViewKind.Unknown : kind);
            cameraViewEnhanced.label = (label == null ? "" : label);
            cameraViewEnhanced.name = (name == null ? "" : name);
            cameraViewEnhanced.originalDataStore = originalDataStore;
            cameraViewEnhanced.settings = settings;
            cameraViewEnhanced.usedCameraSetId = usedSetId;
            cameraViewEnhanced.usedKind = usedKind;

            return cameraViewEnhanced;
        }

    }
}

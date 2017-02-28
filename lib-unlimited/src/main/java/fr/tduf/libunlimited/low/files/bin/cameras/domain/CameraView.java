package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.apache.commons.lang3.SerializationUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Parsed views settings from cameras database
 */
public class CameraView {
    @JsonIgnore
    private DataStore originalDataStore;

    private ViewKind kind;

    private ViewKind usedKind;

    private int cameraSetId;

    private Integer usedCameraSetId;

    private String label;

    private String name;

    private EnumMap<ViewProps, Object> settings;

    private CameraView() {}

    /**
     * @return unique way to get a view instance
     */
    public static CameraViewBuilder builder() {
        return new CameraViewBuilder();
    }

    /**
     * @return a real copy of current view, for specified set identifier
     */
    public CameraView cloneForNewViewSet(int setIdentifier) {
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
    public static CameraView from(ViewKind kind, int usedSetIdentifier, ViewKind usedViewKind) {
        return CameraView.builder()
                .ofKind(kind)
                .usingSettingsFrom(usedSetIdentifier, usedViewKind)
                .build();
    }

    /**
     * @return a new instance with specified properties
     */
    // TODO set identifier ??
    public static CameraView fromProps(EnumMap<ViewProps, Object> viewProps, ViewKind viewKind) {
        return CameraView.builder()
                .ofKind(viewKind)
                .withSettings(viewProps)
                .build();
    }

    /**
     * @return a new instance from patch properties
     */
    // TODO set identifier ??
    public static CameraView fromPatchProps(EnumMap<ViewProps, String> patchProps, ViewKind viewKind) {
        //noinspection Convert2Diamond (type args needed by compiler)
        EnumMap<ViewProps, Object> props = new EnumMap<ViewProps, Object>(
                patchProps.entrySet().stream()
                        .collect(toMap(
                                Map.Entry::getKey,
                                entry -> Long.valueOf(entry.getValue()))
                        ));
        return CameraView.builder()
                .ofKind(viewKind)
                .withSettings(props)
                .build();
    }

    void setUsedSettings(CameraView usedView) {
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

    public static class CameraViewBuilder {
        private DataStore originalDataStore;
        private int setId;
        private ViewKind kind;
        private String label;
        private String name;
        private EnumMap<ViewProps, Object> settings;
        private ViewKind usedKind;
        private int usedSetId;

        public CameraViewBuilder fromDatastore(DataStore originalDataStore) {
            this.originalDataStore = originalDataStore;
            return this;
        }

        public CameraViewBuilder forCameraSetId(int setId) {
            this.setId = setId;
            return this;
        }

        public CameraViewBuilder ofKind(ViewKind kind) {
            this.kind = kind;
            return this;
        }

        public CameraViewBuilder withLabel(String label) {
            this.label = label;
            return this;
        }

        public CameraViewBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CameraViewBuilder withSettings(EnumMap<ViewProps, Object> settings) {
            this.settings = settings;
            return this;
        }

        CameraViewBuilder usingSettingsFrom(int usedSetIdentifier, ViewKind usedViewKind) {
            this.usedSetId = usedSetIdentifier;
            this.usedKind = usedViewKind;
            return this;
        }

        public CameraView build() {
            CameraView cameraView = new CameraView();

            cameraView.cameraSetId = setId;
            cameraView.kind = (kind == null ? ViewKind.Unknown : kind);
            cameraView.label = (label == null ? "" : label);
            cameraView.name = (name == null ? "" : name);
            cameraView.originalDataStore = originalDataStore;
            cameraView.settings = settings;
            cameraView.usedCameraSetId = usedSetId;
            cameraView.usedKind = usedKind;

            return cameraView;
        }

    }
}

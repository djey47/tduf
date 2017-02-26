package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Brings all information about a single camera (view set)
 */
// TODO rename to CameraSetInfo
public class CameraInfo {
    // TODO set to int
    private long cameraIdentifier;
    private List<CameraViewEnhanced> views;

    private CameraInfo() {}

    public static CameraInfoBuilder builder() {
        return new CameraInfoBuilder();
    }

    public long getCameraIdentifier() {
        return cameraIdentifier;
    }

    public List<CameraViewEnhanced> getViews() {
        return views;
    }

    @JsonIgnore
    public Map<ViewKind, CameraViewEnhanced> getViewsByKind() {
        return getViews().stream()
                .collect(toMap(CameraViewEnhanced::getKind, v -> v));
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("cameraIdentifier", cameraIdentifier)
                .toString();
    }

    public static class CameraInfoBuilder {
        private long cameraIdentifier;
        private List<CameraViewEnhanced> views = new ArrayList<>();

        public CameraInfoBuilder forIdentifier(long cameraIdentifier) {
            this.cameraIdentifier = cameraIdentifier;
            return this;
        }

        public CameraInfoBuilder addView(CameraViewEnhanced view) {
            views.add(view);
            return this;
        }

        public CameraInfoBuilder withViews(List<CameraViewEnhanced> allViews) {
            views.addAll(allViews);
            return this;
        }

        public CameraInfoBuilder withUsedViews(List<CameraViewEnhanced> allViews, List<CameraViewEnhanced> usedViews) {
            views.addAll(allViews);
            views
                    .forEach(view -> usedViews.stream()
                            .filter(usedView -> usedView.getKind() == view.getKind())
                            .filter(usedView -> usedView.getUsedCameraSetId() != 0L)
                            .findAny()
                            .ifPresent(view::setUsedSettings));
            return this;
        }

        public CameraInfo build() {
            final CameraInfo cameraInfo = new CameraInfo();

            cameraInfo.cameraIdentifier = cameraIdentifier;
            cameraInfo.views = views;

            return cameraInfo;
        }
    }

    /**
     * Gathers all information about a particular view
     */
    // TODO delete
    public static class CameraView {
        private ViewKind type;
        private long sourceCameraIdentifier;
        private ViewKind sourceType;
        private EnumMap<ViewProps, Object> settings;

        private CameraView() {}

        public static CameraView from(ViewKind type, long sourceCameraIdentifier, ViewKind sourceType) {
            CameraView cameraView = new CameraView();

            cameraView.type = type;
            cameraView.sourceCameraIdentifier = sourceCameraIdentifier;
            cameraView.sourceType = sourceType;

            return cameraView;
        }

        public static CameraView fromProps(EnumMap<ViewProps, Object> viewProps, ViewKind viewKind) {
            CameraView cameraView = new CameraView();

            cameraView.type = viewKind;
            cameraView.settings = viewProps;

            return cameraView;
        }

        // FIXME Must be rewritten with correct parser/writer use
        public static CameraView fromPatchProps(EnumMap<ViewProps, String> patchViewProps, ViewKind cameraViewKind) {
            CameraView cameraView = new CameraView();

            cameraView.type = cameraViewKind;
            //noinspection Convert2Diamond (type args needed by compiler)
            cameraView.settings = new EnumMap<ViewProps, Object>(
                    patchViewProps.entrySet().stream()
                            .collect(toMap(
                                    Map.Entry::getKey,
                                    entry -> Long.valueOf(entry.getValue())
                                    )
                            )
            );

            return cameraView;
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("type", type)
                    .toString();
        }

        public ViewKind getType() {
            return type;
        }

        public long getSourceCameraIdentifier() {
            return sourceCameraIdentifier;
        }

        public ViewKind getSourceType() {
            return sourceType;
        }

        public EnumMap<ViewProps, Object> getSettings() {
            return settings;
        }

    }
}

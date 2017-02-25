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
 * Brings all information about a camera (view set)
 */
// TODO Use as configuration parameter only
public class CameraInfo {
    private long cameraIdentifier;
    private List<CameraView> views;

    private CameraInfo() {}

    public static CameraInfoBuilder builder() {
        return new CameraInfoBuilder();
    }

    // TODO set to int
    public long getCameraIdentifier() {
        return cameraIdentifier;
    }

    public List<CameraView> getViews() {
        return views;
    }

    @JsonIgnore
    public Map<ViewKind, CameraView> getViewsByKind() {
        return getViews().stream()
                .collect(toMap(CameraInfo.CameraView::getType, v -> v));
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
        private List<CameraView> views = new ArrayList<>();

        public CameraInfoBuilder forIdentifier(long cameraIdentifier) {
            this.cameraIdentifier = cameraIdentifier;
            return this;
        }

        public CameraInfoBuilder addView(CameraView view) {
            views.add(view);
            return this;
        }

        public CameraInfoBuilder withViews(List<CameraView> allViews) {
            views.addAll(allViews);
            return this;
        }

        public CameraInfoBuilder withUsedViews(List<CameraView> allViews, List<CameraView> usedViews) {
            views.addAll(allViews);
            views
                    .forEach(view -> usedViews.stream()
                            .filter(usedView -> usedView.getType() == view.getType())
                            .filter(usedView -> usedView.getSourceCameraIdentifier() != 0L)
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

        void setUsedSettings(CameraView usedView) {
            sourceCameraIdentifier = usedView.sourceCameraIdentifier;
            sourceType = usedView.sourceType;
        }
    }
}

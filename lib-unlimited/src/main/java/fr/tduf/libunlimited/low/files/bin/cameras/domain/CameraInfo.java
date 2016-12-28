package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Brings all information about a camera (view set)
 */
public class CameraInfo {
    private long cameraIdentifier;
    private List<CameraView> views;

    private CameraInfo() {}

    public static CameraInfoBuilder builder() {
        return new CameraInfoBuilder();
    }

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
        private EnumMap<ViewProps, ?> settings;

        private CameraView() {}

        public static CameraView from(ViewKind type, long sourceCameraIdentifier, ViewKind sourceType) {
            CameraView cameraView = new CameraView();

            cameraView.type = type;
            cameraView.sourceCameraIdentifier = sourceCameraIdentifier;
            cameraView.sourceType = sourceType;

            return cameraView;
        }

        public static CameraView fromProps(EnumMap<ViewProps, ?> viewProps) {
            CameraView cameraView = new CameraView();

            cameraView.type = (ViewKind) viewProps.get(ViewProps.TYPE);
            cameraView.settings = viewProps;

            return cameraView;
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

        public EnumMap<ViewProps, ?> getSettings() {
            return settings;
        }

        void setUsedSettings(CameraView usedView) {
            sourceCameraIdentifier = usedView.sourceCameraIdentifier;
            sourceType = usedView.sourceType;
        }
    }
}

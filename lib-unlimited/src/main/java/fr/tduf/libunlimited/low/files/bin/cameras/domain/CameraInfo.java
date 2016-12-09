package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Brings all information about a camera (view set)
 */
public class CameraInfo {
    private int cameraIdentifier;
    private List<CameraView> views;

    private CameraInfo() {}

    public static CameraInfoBuilder builder() {
        return new CameraInfoBuilder();
    }

    public int getCameraIdentifier() {
        return cameraIdentifier;
    }

    public List<CameraView> getViews() {
        return views;
    }

    public static class CameraInfoBuilder {
        private int cameraIdentifier;
        private List<CameraView> views = new ArrayList<>();

        // TODO set to long
        public CameraInfoBuilder forIdentifier(int cameraIdentifier) {
            this.cameraIdentifier = cameraIdentifier;
            return this;
        }

        public CameraInfoBuilder addView(CameraView view) {
            views.add(view);
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
        private GenuineCamViewsDto.GenuineCamViewDto.Type type;
        private int sourceCameraIdentifier;
        private GenuineCamViewsDto.GenuineCamViewDto.Type sourceType;
        private EnumMap<ViewProps, ?> settings;

        private CameraView() {}

        public static CameraView from(GenuineCamViewsDto.GenuineCamViewDto.Type type, int sourceCameraIdentifier, GenuineCamViewsDto.GenuineCamViewDto.Type sourceType) {
            CameraView cameraView = new CameraView();

            cameraView.type = type;
            cameraView.sourceCameraIdentifier = sourceCameraIdentifier;
            cameraView.sourceType = sourceType;

            return cameraView;
        }

        public static CameraView fromProps(EnumMap<ViewProps, ?> viewProps) {
            CameraView cameraView = new CameraView();

            cameraView.type = (GenuineCamViewsDto.GenuineCamViewDto.Type) viewProps.get(ViewProps.TYPE);
            cameraView.settings = viewProps;

            return cameraView;
        }

        public GenuineCamViewsDto.GenuineCamViewDto.Type getType() {
            return type;
        }

        public int getSourceCameraIdentifier() {
            return sourceCameraIdentifier;
        }

        public GenuineCamViewsDto.GenuineCamViewDto.Type getSourceType() {
            return sourceType;
        }

        public EnumMap<ViewProps, ?> getSettings() {
            return settings;
        }
    }
}

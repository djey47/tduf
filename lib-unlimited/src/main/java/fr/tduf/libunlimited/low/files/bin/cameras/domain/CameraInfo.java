package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;

import java.util.EnumMap;
import java.util.Map;

/**
 * Brings all information about a camera (view set)
 */
public class CameraInfo {
    private int cameraIdentifier;
    private Map<GenuineCamViewsDto.GenuineCamViewDto.Type, CameraView> viewSets;

    private CameraInfo() {}

    public static CameraInfoBuilder builder() {
        return new CameraInfoBuilder();
    }

    public int getCameraIdentifier() {
        return cameraIdentifier;
    }

    public Map<GenuineCamViewsDto.GenuineCamViewDto.Type, CameraView> getViewSets() {
        return viewSets;
    }

    public static class CameraInfoBuilder {
        private int cameraIdentifier;
        private Map<GenuineCamViewsDto.GenuineCamViewDto.Type, CameraView> viewSets = new EnumMap<>(GenuineCamViewsDto.GenuineCamViewDto.Type.class);

        public CameraInfoBuilder forIdentifier(int cameraIdentifier) {
            this.cameraIdentifier = cameraIdentifier;
            return this;
        }

        public CameraInfoBuilder addView(CameraView view) {
            viewSets.put(view.type, view);
            return this;
        }

        public CameraInfo build() {
            final CameraInfo cameraInfo = new CameraInfo();

            cameraInfo.cameraIdentifier = cameraIdentifier;
            cameraInfo.viewSets = viewSets;

            return cameraInfo;
        }
    }

    public static class CameraView {
        private GenuineCamViewsDto.GenuineCamViewDto.Type type;
        private int sourceCameraIdentifier;
        private GenuineCamViewsDto.GenuineCamViewDto.Type sourceType;

        private CameraView() {}

        public static CameraView from(GenuineCamViewsDto.GenuineCamViewDto.Type type, int sourceCameraIdentifier, GenuineCamViewsDto.GenuineCamViewDto.Type sourceType) {
            CameraView cameraView = new CameraView();

            cameraView.type = type;
            cameraView.sourceCameraIdentifier = sourceCameraIdentifier;
            cameraView.sourceType = sourceType;

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
    }
}

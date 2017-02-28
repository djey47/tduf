package fr.tduf.libunlimited.high.files.bin.cameras.interop.dto;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * In/out object from .net cli, CAM-I and CAM-C commands.
 */
@JsonTypeName("genuineCamViews")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenuineCamViewsDto {

    private GenuineCamViewsDto() {}

    @JsonProperty("views")
    private List<GenuineCamViewDto> views = new ArrayList<>();

    public List<GenuineCamViewDto> getViews() {
        return views;
    }

    /**
     * Recommended factory.
     * @param views : views to use
     * @return a new object with existing views
     */
    public static GenuineCamViewsDto withViews(List<GenuineCamViewDto> views) {
        GenuineCamViewsDto genuineCamViews = new GenuineCamViewsDto();
        genuineCamViews.views.addAll(views);
        return genuineCamViews;
    }

    @JsonTypeName("genuineCamView")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class  GenuineCamViewDto {
        @JsonProperty("type")
        private ViewKind viewType;

        @JsonProperty("cameraId")
        private int cameraId;

        @JsonProperty("viewId")
        private int viewId;

        @JsonProperty("customized")
        private boolean customized;

        public ViewKind getViewType() {
            return viewType;
        }

        public int getCameraId() {
            return cameraId;
        }

        public int getViewId() {
            return viewId;
        }

        public boolean isCustomized() {
            return customized;
        }

        public void setViewType(ViewKind viewType) {
            this.viewType = viewType;
        }

        public void setCameraId(int cameraId) {
            this.cameraId = cameraId;
        }

        public void setViewId(int viewId) {
            this.viewId = viewId;
        }

        public void setCustomized(boolean customized) {
            this.customized = customized;
        }
    }
}

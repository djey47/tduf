package fr.tduf.libunlimited.high.files.bin.cameras.interop.dto;

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

    @JsonProperty("views")
    private List<GenuineCamViewDto> views = new ArrayList<>();

    public List<GenuineCamViewDto> getViews() {
        return views;
    }

    @JsonTypeName("genuineCamView")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class  GenuineCamViewDto {
        /** All handled view kinds **/
        public enum Type {
            Bumper, Bumper_Back,
            Cockpit, Cockpit_Back,
            Follow_Far, Follow_Far_Back,
            Follow_Large, Follow_Large_Back,
            Follow_Near, Follow_Near_Back,
            Hood, Hood_Back,
            Unknown
        }

        @JsonProperty("type")
        private Type viewType;

        @JsonProperty("cameraId")
        private int cameraId;

        @JsonProperty("viewId")
        private int viewId;

        @JsonProperty("customized")
        private boolean customized;

        public Type getViewType() {
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

        public void setViewType(Type viewType) {
            this.viewType = viewType;
        }

        public void setCameraId(int cameraId) {
            this.cameraId = cameraId;
        }

        public void setViewId(int viewId) {
            this.viewId = viewId;
        }
    }
}

package fr.tduf.libunlimited.high.files.bin.cameras.interop.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
            Follow_Near(20), Follow_Near_Back(40),
            Follow_Far(21), Follow_Far_Back(41),
            Bumper(22), Bumper_Back(42),
            Cockpit(23), Cockpit_Back(43),
            Hood(24), Hood_Back(44),
            Follow_Large(25), Follow_Large_Back(45),
            Unknown(0);

            private int internalId;

            Type(int internalId) {
                this.internalId = internalId;
            }

            public static Type fromInternalId(int internalId) {
                return Stream.of(values())
                        .filter(type -> type.internalId == internalId)
                        .findAny()
                        .<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException("Unknown view type identifier: " + internalId));
            }

            public int getInternalId() {
                return internalId;
            }
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

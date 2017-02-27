package fr.tduf.libunlimited.low.files.bin.cameras.dto;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Specifies configuration for cameras re-use (legacy TDUMT feature)
 */
@JsonTypeName("cameraInfo")
public class SetConfigurationDto {
    @JsonProperty("cameraIdentifier")
    private int setIdentifier;

    @JsonProperty("views")
    private List<ViewConfigurationDto> viewsConfiguration;

    private SetConfigurationDto() {}

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
                .append("setIdentifier", setIdentifier)
                .toString();
    }

    public List<ViewConfigurationDto> getViews() {
        return viewsConfiguration;
    }

    public int getSetIdentifier() {
        return setIdentifier;
    }

    public static SetConfigurationDtoBuilder builder() {
        return new SetConfigurationDtoBuilder();
    }

    public static class SetConfigurationDtoBuilder {
        private int setIdentifier;
        private final List<ViewConfigurationDto> views = new ArrayList<>();

        public SetConfigurationDtoBuilder forIdentifier(int setIdentifier) {
            this.setIdentifier = setIdentifier;
            return this;
        }

        public SetConfigurationDtoBuilder addView(CameraView view) {
            this.views.add(ViewConfigurationDto.builder()
                    .forKind(view.getKind())
                    .withSettings(view.getSettings())
                    .build());

            return this;
        }

        public SetConfigurationDtoBuilder withViews(List<CameraView> views) {
            this.views.clear();
            this.views.addAll(views.stream()
                    .map(view -> ViewConfigurationDto.builder()
                            .withUsedSetIdentifier(view.getUsedCameraSetId())
                            .witUsedViewKind(view.getUsedKind())
                            .withSettings(view.getSettings())
                            .build())
                    .collect(toList()));
            return this;
        }

        public SetConfigurationDto build() {
            SetConfigurationDto setConfigurationDto = new SetConfigurationDto();

            setConfigurationDto.setIdentifier = setIdentifier;
            setConfigurationDto.viewsConfiguration = views;

            return setConfigurationDto;
        }
    }
}

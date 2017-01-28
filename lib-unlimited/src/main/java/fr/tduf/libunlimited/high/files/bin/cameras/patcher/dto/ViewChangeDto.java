package fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

@JsonTypeName("setChange")
@JsonSerialize(include = NON_NULL)
public class ViewChangeDto {

    @JsonProperty("viewKind")
    private ViewKind cameraViewKind;

    @JsonProperty("properties")
    private EnumMap<ViewProps, String> viewProps;

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
        return reflectionToString(this);
    }

    private ViewChangeDto() {
    }

    /**
     * Allows to generate custom instances.
     */
    public static ViewChangeDto.ViewChangeDtoBuilder builder() {
        return new ViewChangeDto.ViewChangeDtoBuilder();
    }

    public static class ViewChangeDtoBuilder {
        private ViewKind cameraViewKind;
        private EnumMap<ViewProps, String> viewProps = new EnumMap<>(ViewProps.class);

        public ViewChangeDtoBuilder addProp(ViewProps prop, String value) {
            this.viewProps.put(prop, value);
            return this;
        }

        public ViewChangeDtoBuilder withProps(EnumMap<ViewProps, String> props) {
            viewProps.putAll(props);
            return this;
        }

        public ViewChangeDtoBuilder forViewKind(ViewKind viewKind) {
            this.cameraViewKind = viewKind;
            return this;
        }

        public ViewChangeDto build() {
            ViewChangeDto viewChangeDto = new ViewChangeDto();

            viewChangeDto.cameraViewKind = requireNonNull(cameraViewKind, "Camera view kind is required");
            viewChangeDto.viewProps = viewProps;

            return viewChangeDto;
        }
    }

    public ViewKind getCameraViewKind() {
        return cameraViewKind;
    }

    public EnumMap<ViewProps, String> getViewProps() { return viewProps; }
}

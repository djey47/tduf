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
    private Map<ViewProps, Object> viewProps;

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
        private Map<ViewProps, Object> viewProps = new HashMap<>(ViewProps.values().length);

        public void addProp(ViewProps prop, Object value) {
            this.viewProps.put(prop, value);
        }

        public void forViewKind(ViewKind viewKind) {
            this.cameraViewKind = viewKind;
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

    public Map<ViewProps, Object> getViewProps() { return viewProps; }
}

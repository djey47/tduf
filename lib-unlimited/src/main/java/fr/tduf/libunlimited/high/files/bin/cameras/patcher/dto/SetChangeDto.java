package fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.tduf.libunlimited.high.files.common.patcher.helper.PlaceholderResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("setChange")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetChangeDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("changes")
    private List<ViewChangeDto> viewChanges;

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

    private SetChangeDto() {
    }

    /**
     * Allows to generate custom instances.
     */
    public static SetChangeDto.SetChangeDtoBuilder builder() {
        return new SetChangeDto.SetChangeDtoBuilder();
    }

    public static class SetChangeDtoBuilder {
        private long setIdentifier;
        private String setIdentifierPlaceholder;
        private List<ViewChangeDto> viewChanges = new ArrayList<>();

        public SetChangeDtoBuilder addChanges(Collection<ViewChangeDto> viewChanges) {
            this.viewChanges.addAll(viewChanges);
            return this;
        }

        public SetChangeDtoBuilder withSetIdentifier(long setIdentifier) {
            this.setIdentifier = setIdentifier;
            return this;
        }

        public SetChangeDtoBuilder withPlaceholderForSetIdentifier(String placeholderValue) {
            this.setIdentifierPlaceholder = String.format(PlaceholderResolver.FORMAT_PLACEHOLDER, placeholderValue);
            return this;
        }

        public SetChangeDto build() {
            SetChangeDto setChangeDto = new SetChangeDto();

            setChangeDto.id = setIdentifierPlaceholder == null ?
                    Long.toString(setIdentifier)
                    :
                    setIdentifierPlaceholder;

            setChangeDto.viewChanges = viewChanges;

            return setChangeDto;
        }
    }

    public String getId() {
        return id;
    }

    public List<ViewChangeDto> getChanges() {
        return viewChanges;
    }

    public void overrideId(String identifier) {
        this.id = identifier;
    }
}

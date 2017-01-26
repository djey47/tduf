package fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

@JsonTypeName("setChange")
@JsonSerialize(include = NON_NULL)
public class SetChangeDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("changes")
    private List<SetChangeDto> setChanges;

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
        private List<SetChangeDto> viewChanges = new ArrayList<>();

        public void addChanges(Collection<SetChangeDto> setChanges) {
            this.viewChanges.addAll(setChanges);
        }

        public void withSetIdentifier(long setIdentifier) {
            this.setIdentifier = setIdentifier;
        }

        public SetChangeDto build() {
            SetChangeDto setChangeDto = new SetChangeDto();

            setChangeDto.id = Long.toString(requireNonNull(setIdentifier, "Identifier of camera set is required"));
            setChangeDto.setChanges = viewChanges;

            return setChangeDto;
        }
    }

    public String getId() {
        return id;
    }

    public List<SetChangeDto> getChanges() {
        return setChanges;
    }
}

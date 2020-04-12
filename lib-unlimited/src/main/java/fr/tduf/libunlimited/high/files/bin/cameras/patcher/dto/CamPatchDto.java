package fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * Structure for camera patch, to be applied onto Cameras.bin file
 */
@JsonTypeName("camPatch")
@JsonSerialize(include = NON_NULL)
public class CamPatchDto {

    @JsonProperty("changes")
    private List<SetChangeDto> changes = new ArrayList<>();

    @JsonProperty("comment")
    private String comment;

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

    private CamPatchDto() {
    }

    /**
     * Allows to generate custom instances.
     */
    public static CamPatchDto.CamPatchDtoBuilder builder() {
        return new CamPatchDto.CamPatchDtoBuilder();
    }

    public static class CamPatchDtoBuilder {
        private List<SetChangeDto> setChanges = new ArrayList<>();
        private String comment;

        public CamPatchDtoBuilder addChanges(Collection<SetChangeDto> setChanges) {
            this.setChanges.addAll(setChanges);
            return this;
        }

        public CamPatchDtoBuilder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public CamPatchDto build() {
            CamPatchDto camPatchDto = new CamPatchDto();

            camPatchDto.changes = setChanges;
            camPatchDto.comment = comment;

            return camPatchDto;
        }
    }

    public String getComment() {
        return comment;
    }

    public List<SetChangeDto> getChanges() {
        return changes;
    }
}

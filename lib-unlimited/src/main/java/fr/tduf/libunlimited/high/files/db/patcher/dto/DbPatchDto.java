package fr.tduf.libunlimited.high.files.db.patcher.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.comparator.DbChangeDtoRenderComparator;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.renderComparator;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a patch to be applied to TDU database (TDUF format)
 */
@JsonTypeName("dbPatch")
@JsonSerialize()
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DbPatchDto {

    @JsonProperty("changes")
    private List<DbChangeDto> changes = new ArrayList<>();

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

    private DbPatchDto() {
    }

    /**
     * Allows to generate custom instances.
     */
    public static DbPatchDtoBuilder builder() {
        return new DbPatchDtoBuilder();
    }

    public static class DbPatchDtoBuilder {
        private List<DbChangeDto> changes = new ArrayList<>();
        private String comment = null;

        public DbPatchDtoBuilder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public DbPatchDtoBuilder addChanges(Collection<DbChangeDto> changes) {
            this.changes.addAll(changes);
            return this;
        }

        public DbPatchDto build() {
            DbPatchDto patchObject = new DbPatchDto();

            changes.sort(renderComparator());
            patchObject.changes = changes;
            patchObject.comment = comment;

            return patchObject;
        }
    }

    public String getComment() {
        return comment;
    }

    public List<DbChangeDto> getChanges() {
        return changes;
    }

    /**
     * A patch instruction.
     */
    @JsonTypeName("dbChange")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DbChangeDto {

        private static final String FORMAT_PLACEHOLDER = "{%s}";

        @JsonProperty("type")
        private ChangeTypeEnum type;

        @JsonProperty("onlyAdd")
        private Boolean strictMode;

        @JsonProperty("topic")
        private DbDto.Topic topic;

        @JsonProperty("locale")
        private Locale locale;

        @JsonProperty("ref")
        private String ref;

        @JsonProperty("filter")
        private List<DbFieldValueDto> filterCompounds;

        @JsonProperty("value")
        private String value;

        @JsonProperty("values")
        private List<String> values;

        @JsonProperty("partialValues")
        private List<DbFieldValueDto> partialValues;

        @JsonProperty("direction")
        private DirectionEnum direction;

        @JsonProperty("steps")
        private Integer steps;

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

        public Locale getLocale() {
            return locale;
        }

        public ChangeTypeEnum getType() {
            return type;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public DbDto.Topic getTopic() {
            return topic;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        public List<String> getValues() {
            return values;
        }

        public List<DbFieldValueDto> getPartialValues() {
            return partialValues;
        }

        public void setPartialValues(List<DbFieldValueDto> partialValues) {
            this.partialValues = partialValues;
        }

        public List<DbFieldValueDto> getFilterCompounds() {
            return filterCompounds;
        }

        public void setFilterCompounds(List<DbFieldValueDto> filterCompounds) {
            this.filterCompounds = filterCompounds;
        }

        public DirectionEnum getDirection() {
            return direction;
        }

        public Integer getSteps() {
            return steps;
        }

        @JsonIgnore
        public boolean isPartialChange() {
            return partialValues != null;
        }

        @JsonIgnore
        public boolean isStrictMode() {
            return (strictMode != null && strictMode);
        }

        public static DbChangeDtoBuilder builder() {
            return new DbChangeDtoBuilder();
        }

        /**
         * Allows to build custom instances.
         */
        public static class DbChangeDtoBuilder {
            private Locale locale;
            private String value;
            private List<String> entryValues;
            private List<DbFieldValueDto> filterCompounds;
            private List<DbFieldValueDto> partialEntryValues;
            private String reference;
            private DbDto.Topic topic;
            private ChangeTypeEnum type;
            private Boolean strictMode;

            public DbChangeDtoBuilder withType(ChangeTypeEnum type) {
                this.type = type;
                return this;
            }

            public DbChangeDtoBuilder enableStrictMode(boolean strictMode) {
                this.strictMode = strictMode ? true : null;
                return this;
            }

            public DbChangeDtoBuilder forTopic(DbDto.Topic topic) {
                this.topic = topic;
                return this;
            }

            public DbChangeDtoBuilder asReference(String entryReference) {
                this.reference = entryReference;
                return this;
            }

            public DbChangeDtoBuilder asReferencePlaceholder(String name) {
                this.reference = String.format(FORMAT_PLACEHOLDER, name);
                return this;
            }

            public DbChangeDtoBuilder withEntryValues(List<String> entryValues) {
                this.entryValues = entryValues;
                return this;
            }

            public DbChangeDtoBuilder filteredBy(List<DbFieldValueDto> filterCompounds) {
                this.filterCompounds = filterCompounds;
                return this;
            }

            public DbChangeDtoBuilder withPartialEntryValues(List<DbFieldValueDto> partialEntryValues) {
                this.partialEntryValues = partialEntryValues;
                return this;
            }

            public DbChangeDtoBuilder withValue(String value) {
                this.value = value;
                return this;
            }

            public DbChangeDtoBuilder withValuePlaceholder(String name) {
                this.value = String.format(FORMAT_PLACEHOLDER, name);
                return this;
            }

            public DbChangeDtoBuilder forLocale(Locale locale) {
                this.locale = locale;
                return this;
            }

            public DbChangeDto build() {
                if (partialEntryValues != null && entryValues != null) {
                    throw new IllegalStateException("Conflict in change: can't have partialEntryValues and entryValues at the same time");
                }

                DbChangeDto changeObject = new DbChangeDto();

                changeObject.type = requireNonNull(type, "Instruction type is required");
                changeObject.topic = requireNonNull(topic, "Instruction topic is required");
                changeObject.ref = reference;
                changeObject.values = entryValues;
                changeObject.filterCompounds = filterCompounds;
                changeObject.partialValues = partialEntryValues;
                changeObject.value = value;
                changeObject.locale = locale;
                changeObject.strictMode = strictMode;

                return changeObject;
            }
        }

        public static DbChangeDtoRenderComparator renderComparator() {
            return new DbChangeDtoRenderComparator();
        }

        /**
         * All supported database changes.
         */
        public enum ChangeTypeEnum {
            MOVE(0), UPDATE(3), DELETE(1), UPDATE_RES(4), DELETE_RES(2);

            private final int renderPriority;

            ChangeTypeEnum(int renderPriority) {
                this.renderPriority = renderPriority;
            }

            public int getRenderPriority() {
                return renderPriority;
            }
        }

        /**
         * All supported move directions
         */
        public enum DirectionEnum {
            UP, DOWN
        }
    }
}

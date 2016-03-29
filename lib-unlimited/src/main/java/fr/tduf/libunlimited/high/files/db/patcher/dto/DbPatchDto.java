package fr.tduf.libunlimited.high.files.db.patcher.dto;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.comparator.DbChangeDtoRenderComparator;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.renderComparator;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * Represents a patch to be applied to TDU database (TDUF format)
 */
@JsonTypeName("dbPatch")
@JsonSerialize(include = NON_NULL)
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

    private DbPatchDto() {}

    /**
     * Allows to generate custom instances.
     */
    public static DbPatchDtoBuilder builder() {
        return new DbPatchDtoBuilder() {
            private List<DbChangeDto> changes = new ArrayList<>();
            private String comment = null;

            @Override
            public DbPatchDtoBuilder withComment(String comment) {
                this.comment = comment;
                return this;
            }

            @Override
            public DbPatchDtoBuilder addChanges(Collection<DbChangeDto> changes) {
                this.changes.addAll(changes);
                return this;
            }

            @Override
            public DbPatchDto build() {
                DbPatchDto patchObject = new DbPatchDto();

                changes.sort(renderComparator());
                patchObject.changes = changes;
                patchObject.comment = comment;

                return patchObject;
            }
        };
    }

    public String getComment() {
        return comment;
    }

    public interface DbPatchDtoBuilder {
        DbPatchDtoBuilder withComment(String comment);

        DbPatchDtoBuilder addChanges(Collection<DbChangeDto> changes);

        DbPatchDto build();
    }

    public List<DbChangeDto> getChanges() {
        return changes;
    }

    /**
     * A patch instruction.
     */
    @JsonTypeName("dbChange")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = NON_NULL)
    public static class DbChangeDto {

        private static final String FORMAT_PLACEHOLDER = "{%s}";

        @JsonProperty("type")
        private ChangeTypeEnum type;

        @JsonProperty("onlyAdd")
        private Boolean strictMode;

        @JsonProperty("topic")
        private DbDto.Topic topic;

        @JsonProperty("locale")
        private DbResourceDto.Locale locale;

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

        public DbResourceDto.Locale getLocale() {
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
            return new DbChangeDtoBuilder() {
                private DbResourceDto.Locale locale;
                private String value;
                private List<String> entryValues;
                private List<DbFieldValueDto> filterCompounds;
                private List<DbFieldValueDto> partialEntryValues;
                private String reference;
                private DbDto.Topic topic;
                private ChangeTypeEnum type;
                private DirectionEnum moveDirection;
                private Integer moveSteps;
                private Boolean strictMode;

                @Override
                public DbChangeDtoBuilder withType(ChangeTypeEnum type) {
                    this.type = type;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder enableStrictMode(boolean strictMode) {
                    this.strictMode = strictMode ? true : null;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder forTopic(DbDto.Topic topic) {
                    this.topic = topic;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder asReference(String entryReference) {
                    this.reference = entryReference;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder asReferencePlaceholder(String name) {
                    this.reference = String.format(FORMAT_PLACEHOLDER, name);
                    return this;
                }

                @Override
                public DbChangeDtoBuilder withEntryValues(List<String> entryValues) {
                    this.entryValues = entryValues;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder filteredBy(List<DbFieldValueDto> filterCompounds) {
                    this.filterCompounds = filterCompounds;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder withPartialEntryValues(List<DbFieldValueDto> partialEntryValues) {
                    this.partialEntryValues = partialEntryValues;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder withValue(String value) {
                    this.value = value;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder withValuePlaceholder(String name) {
                    this.value = String.format(FORMAT_PLACEHOLDER, name);
                    return this;
                }

                @Override
                public DbChangeDtoBuilder forLocale(DbResourceDto.Locale locale) {
                    this.locale = locale;
                    return this;
                }

                @Override
                public DbChangeDtoBuilder moveForDirection(DirectionEnum direction, OptionalInt steps) {
                    this.moveDirection = direction;
                    this.moveSteps = steps.orElse(1);
                    return this;
                }

                @Override
                public DbChangeDto build() {
                    if (partialEntryValues != null && entryValues != null) {
                        throw new IllegalStateException("Conflict in change: can't have partialEntryValues and entryValues at the same time");
                    }

                    DbChangeDto changeObject = new DbChangeDto();

                    changeObject.type = type;
                    changeObject.topic = topic;
                    changeObject.ref = reference;
                    changeObject.values = entryValues;
                    changeObject.filterCompounds = filterCompounds;
                    changeObject.partialValues = partialEntryValues;
                    changeObject.value = value;
                    changeObject.locale = locale;
                    changeObject.direction = moveDirection;
                    changeObject.steps = moveSteps;
                    changeObject.strictMode = strictMode;

                    return changeObject;
                }
            };
        }

        public static DbChangeDtoRenderComparator renderComparator() {
            return new DbChangeDtoRenderComparator();
        }

        /**
         * Allows to build custom instances.
         */
        public interface DbChangeDtoBuilder {
            DbChangeDto build();

            DbChangeDtoBuilder withType(ChangeTypeEnum update);

            DbChangeDtoBuilder enableStrictMode(boolean strictMode);

            DbChangeDtoBuilder forTopic(DbDto.Topic topic);

            DbChangeDtoBuilder asReference(String entryReference);

            DbChangeDtoBuilder asReferencePlaceholder(String name);

            DbChangeDtoBuilder withEntryValues(List<String> entryValues);

            DbChangeDtoBuilder filteredBy(List<DbFieldValueDto> filterCompounds);

            DbChangeDtoBuilder withPartialEntryValues(List<DbFieldValueDto> partialEntryValues);

            DbChangeDtoBuilder withValue(String resourceValue);

            DbChangeDtoBuilder withValuePlaceholder(String name);

            DbChangeDtoBuilder forLocale(DbResourceDto.Locale locale);

            DbChangeDtoBuilder moveForDirection(DirectionEnum direction, OptionalInt steps);
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

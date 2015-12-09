package fr.tduf.libunlimited.high.files.db.patcher.dto;

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

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.renderComparator;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a patch to be applied to TDU database (TDUF format)
 */
@JsonTypeName("dbPatch")
public class DbPatchDto {

    @JsonProperty("changes")
    private List<DbChangeDto> changes = new ArrayList<>();

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

                return patchObject;
            }
        };
    }

    public interface DbPatchDtoBuilder {
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
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class DbChangeDto {
        @JsonProperty("type")
        private ChangeTypeEnum type;

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

        public DbDto.Topic getTopic() {
            return topic;
        }

        public String getValue() {
            return value;
        }

        public List<String> getValues() {
            return values;
        }

        public List<DbFieldValueDto> getPartialValues() {
            return partialValues;
        }

        public List<DbFieldValueDto> getFilterCompounds() {
            return filterCompounds;
        }

        @JsonIgnore
        public boolean isPartialChange() {
            return partialValues != null;
        }

        public static DbChangeDtoBuilder builder() {
            return new DbChangeDtoBuilder() {
                private DbResourceDto.Locale locale;
                private String value;
                private List<String> entryValues;
                private List<DbFieldValueDto> partialEntryValues;
                private String reference;
                private DbDto.Topic topic;
                private ChangeTypeEnum type;

                @Override
                public DbChangeDtoBuilder withType(ChangeTypeEnum type) {
                    this.type = type;
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
                public DbChangeDtoBuilder withEntryValues(List<String> entryValues) {
                    this.entryValues = entryValues;
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
                public DbChangeDtoBuilder forLocale(DbResourceDto.Locale locale) {
                    this.locale = locale;
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
                    changeObject.partialValues = partialEntryValues;
                    changeObject.value = value;
                    changeObject.locale = locale;

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

            DbChangeDtoBuilder forTopic(DbDto.Topic topic);

            DbChangeDtoBuilder asReference(String entryReference);

            DbChangeDtoBuilder withEntryValues(List<String> entryValues);

            DbChangeDtoBuilder withPartialEntryValues(List<DbFieldValueDto> partialEntryValues);

            DbChangeDtoBuilder withValue(String resourceValue);

            DbChangeDtoBuilder forLocale(DbResourceDto.Locale locale);
        }

        /**
         * All supported database changes.
         */
        public enum ChangeTypeEnum {
            UPDATE(2), DELETE(0), UPDATE_RES(3), DELETE_RES(1);

            private final int renderPriority;

            ChangeTypeEnum(int renderPriority) {
                this.renderPriority = renderPriority;
            }

            public int getRenderPriority() {
                return renderPriority;
            }
        }

        // TODO move to dedicated class in common package
        /**
         * An entry field value for selection and changes in update/delete instructions.
         */
        @JsonTypeName("dbFieldChangeValue")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public static class DbFieldValueDto {
            @JsonProperty("rank")
            private final int rank;

            @JsonProperty("value")
            private final String value;

            /** For Jackson **/
            private DbFieldValueDto() {
                this(0, null);
            }

            private DbFieldValueDto(int rank, String value) {
                this.rank = rank;
                this.value = value;
            }

            public static DbFieldValueDto fromCouple(int rank, String value) {
                return new DbFieldValueDto(rank, value);
            }

            public int getRank() {
                return rank;
            }

            public String getValue() {
                return value;
            }

            @Override
            public String toString() { return reflectionToString(this); }

            @Override
            public boolean equals(Object o) { return reflectionEquals(this, o); }

            @Override
            public int hashCode() { return reflectionHashCode(this); }
        }
    }
}

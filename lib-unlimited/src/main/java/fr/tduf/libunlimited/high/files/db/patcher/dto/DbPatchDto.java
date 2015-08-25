package fr.tduf.libunlimited.high.files.db.patcher.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

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

    private  DbPatchDto() {}

    /**
     * Allows to generate custom instances.
     */
    public static DbPatchDtoBuilder builder() {
        return new DbPatchDtoBuilder() {
            private List<DbChangeDto> changes = new ArrayList<>();

            @Override
            public DbPatchDtoBuilder addChanges(List<DbChangeDto> changes) {
                this.changes.addAll(changes);
                return this;
            }

            @Override
            public DbPatchDto build() {
                DbPatchDto patchObject = new DbPatchDto();

                patchObject.changes = this.changes;

                return patchObject;
            }
        };
    }

    public interface DbPatchDtoBuilder {
        DbPatchDtoBuilder addChanges(List<DbChangeDto> changes);

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

        @JsonProperty("item")
        private String item;

        @JsonProperty("value")
        private String value;

        @JsonProperty("values")
        private List<String> values;

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

        public static DbChangeDtoBuilder builder() {
            return new DbChangeDtoBuilder() {
                private DbResourceDto.Locale locale;
                private String value;
                private List<String> entryValues;
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
                    DbChangeDto changeObject = new DbChangeDto();

                    changeObject.type = type;
                    changeObject.topic = topic;
                    changeObject.ref = reference;
                    changeObject.values = entryValues;
                    changeObject.value = value;
                    changeObject.locale = locale;

                    return changeObject;
                }
            };
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

            DbChangeDtoBuilder withValue(String resourceValue);

            DbChangeDtoBuilder forLocale(DbResourceDto.Locale locale);
        }

        /**
         * All supported database changes.
         */
        public enum ChangeTypeEnum {
            UPDATE, DELETE, UPDATE_RES, DELETE_RES
        }
    }
}
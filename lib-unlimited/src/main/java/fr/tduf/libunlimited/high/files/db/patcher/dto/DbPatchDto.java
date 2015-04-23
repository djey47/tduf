package fr.tduf.libunlimited.high.files.db.patcher.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patch to be applied to TDU database (TDUF format)
 */
@JsonTypeName("dbPatch")
public class DbPatchDto {
    @JsonProperty("changes")
    private List<DbChangeDto> changes = new ArrayList<>();


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

        /**
         * All supported database changes.
         */
        public enum ChangeTypeEnum {
            UPDATE, DELETE, UPDATE_RES, DELETE_RES
        }
    }
}
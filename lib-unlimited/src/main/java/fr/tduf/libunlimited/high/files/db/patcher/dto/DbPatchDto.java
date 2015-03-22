package fr.tduf.libunlimited.high.files.db.patcher.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;

/**
 * Represents a patch to be applied to TDU database (TDUF format)
 */
@JsonTypeName("dbPatch")
public class DbPatchDto {
    @JsonProperty("changes")
    private List<DbChangeDto> changes;

    /**
     * A patch instruction.
     */
    @JsonTypeName("dbChange")
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

        /**
         * All supported database changes.
         */
        public enum ChangeTypeEnum {
            UPDATE, UPDATE_ALL, DELETE, UPDATE_RES, DELETE_RES
        }
    }
}
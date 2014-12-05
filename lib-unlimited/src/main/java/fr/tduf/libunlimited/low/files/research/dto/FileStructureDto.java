package fr.tduf.libunlimited.low.files.research.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

/**
 * Represents contents of structure file
 */
@JsonTypeName("fileStructure")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class FileStructureDto implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("littleEndian")
    private Boolean littlEndian;

    @JsonProperty("fields")
    private List<Field> fields;

    /**
     * @return builder, used to generate custom values.
     */
    public static FileStructureDtoBuilder builder() {
        return new FileStructureDtoBuilder() {
            @Override
            public FileStructureDto build() {
                return new FileStructureDto();
            }
        };
    }

    private FileStructureDto() {}

    /**
     * Represents a field in structure.
     */
    @JsonTypeName("fileStructureField")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private class Field {

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private Type type;

        @JsonProperty("size")
        private Integer size;

        @JsonProperty("subFields")
        private List<Field> subFields;
    }

    /**
     * Describes all field types.
     */
    private enum Type {
        STRING,
        INTEGER,
        REPEATER,
        DELIMITER;
    }

    public interface FileStructureDtoBuilder {
        FileStructureDto build();
    }
}
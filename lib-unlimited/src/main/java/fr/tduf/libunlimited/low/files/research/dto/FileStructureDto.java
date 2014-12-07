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
    private Boolean littleEndian;

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

    public List<Field> getFields() {
        return fields;
    }

    /**
     * Represents a field in structure.
     */
    public interface FileStructureDtoBuilder {
        FileStructureDto build();
    }

    @JsonTypeName("fileStructureField")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Field implements Serializable {

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private Type type;

        @JsonProperty("size")
        private Integer size;

        @JsonProperty("subFields")
        private List<Field> subFields;

        private Field() {}

        public Integer getSize() {
            return size;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }
        public List<Field> getSubFields() {
            return subFields;
        }

    }
    /**
     * Describes all field types.
     */
    public enum Type {
        TEXT(true),
        NUMBER(true),
        REPEATER(false),
        DELIMITER(false);

        /**
         * Indicates if this type of value will be stored to allow requests on it.
         */
        private final boolean valuedToBeStored;

        Type(boolean valueToBeStored) {
            this.valuedToBeStored = valueToBeStored;
        }

        public boolean isValuedToBeStored() {
            return valuedToBeStored;
        }
    }
}
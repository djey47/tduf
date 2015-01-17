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

    // TODO Check correct usage
    @JsonProperty("littleEndian")
    private Boolean littleEndian;

    @JsonProperty("fields")
    private List<Field> fields;

    /**
     * @return builder, used to generate custom values.
     */
    public static FileStructureDtoBuilder builder() {
        return FileStructureDto::new;
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
        private String sizeFormula;

        @JsonProperty("subFields")
        private List<Field> subFields;

        private Field() {}

        public static FieldBuilder builder() {
            return new FieldBuilder() {
                private List<Field> subFields;
                private Type type;
                private String sizeFormula;
                private String name;

                @Override
                public FieldBuilder forName(String name) {
                    this.name = name;
                    return this;
                }

                @Override
                public FieldBuilder withType(Type type) {
                    this.type = type;
                    return this;
                }

                @Override
                public FieldBuilder ofSize(String sizeFormula) {
                    this.sizeFormula = sizeFormula;
                    return this;
                }

                @Override
                public FieldBuilder withSubFields(List<Field> subFields) {
                    this.subFields = subFields;
                    return this;
                }

                @Override
                public FieldBuilder ofSubItemCount(String countFormula) {
                    this.sizeFormula = countFormula;
                    return this;
                }

                @Override
                public Field build() {
                    Field field = new Field();

                    field.name = this.name;
                    field.sizeFormula = this.sizeFormula;
                    field.type = this.type;
                    field.subFields = this.subFields;

                    return field;
                }
            };
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", sizeFormula=" + sizeFormula +
                    '}';
        }

        public String getSizeFormula() {
            return sizeFormula;
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

        public interface FieldBuilder {

            FieldBuilder forName(String name_hash);

            FieldBuilder withType(Type type);

            FieldBuilder ofSize(String sizeFormula);

            FieldBuilder withSubFields(List<Field> subFields);

            FieldBuilder ofSubItemCount(String countFormula);

            Field build();
        }
    }

    /**
     * Describes all field types.
     */
    public enum Type {
        /**
         * Values to be understood, still.
         */
        UNKNOWN(true),
        /**
         * Value as string
         */
        TEXT(true),
        /**
         * Numeric Integer value. Currently handled: 32bit only.
         */
        INTEGER(true),
        /**
         * Numeric Floating Point value. Currently handled: 32bit only.
         */
        FPOINT(true),
        /**
         * Delimiter with particular value
         */
        DELIMITER(true),
        /**
         * Allow to repeat a sub-structure
         */
        REPEATER(false),
        /**
         * Hole in the file. Only contains zeros.
         */
        GAP(false);

        /**
         * Indicates if this type of value will be stored to allow requests on it.
         */
        private final boolean valuedToBeStored;

        Type(boolean valueToBeStored) {
            this.valuedToBeStored = valueToBeStored;
        }

        public boolean isValueToBeStored() {
            return valuedToBeStored;
        }
    }
}
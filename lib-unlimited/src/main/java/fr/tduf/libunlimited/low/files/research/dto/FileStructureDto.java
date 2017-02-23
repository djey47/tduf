package fr.tduf.libunlimited.low.files.research.dto;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.Type;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.low.files.research.domain.Type.CONSTANT;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

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

    @JsonProperty("cryptoMode")
    private Integer cryptoMode;

    @JsonProperty("fields")
    private List<Field> fields;

    /**
     * @return builder, used to generate custom values.
     */
    public static FileStructureDtoBuilder builder() {
        return new FileStructureDtoBuilder() {
            private List<Field> fields = new ArrayList<>();

            @Override
            public FileStructureDtoBuilder addFields(List<Field> fields) {
                this.fields.addAll(fields);
                return this;
            }

            @Override
            public FileStructureDto build() {
                FileStructureDto fileStructureDto = new FileStructureDto();

                fileStructureDto.fields = this.fields;

                return fileStructureDto;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    private FileStructureDto() {}

    public List<Field> getFields() {
        return fields;
    }

    public boolean isLittleEndian() {
        return littleEndian != null && littleEndian;
    }

    public Integer getCryptoMode() {
        return cryptoMode;
    }

    /**
     * Represents a field in structure.
     */
    public interface FileStructureDtoBuilder {

        FileStructureDtoBuilder addFields(List<Field> fields);

        FileStructureDto build();
    }

    @JsonTypeName("fileStructureField")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Field implements Serializable {
        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private Type type;

        @JsonProperty("signed")
        private Boolean signed;

        @JsonProperty("size")
        private String sizeFormula;

        @JsonProperty("constantValue")
        private String constantValue;

        @JsonProperty("subFields")
        private List<Field> subFields;

        private Field() {}

        public static FieldBuilder builder() {
            return new FieldBuilder() {
                private byte[] constantValueAsByteArray;
                private boolean signed;
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
                public FieldBuilder withConstantValue(byte[] value) {
                    this.type = CONSTANT;
                    this.constantValueAsByteArray = value;
                    this.sizeFormula = Integer.toString(value.length);
                    return this;
                }

                @Override
                public FieldBuilder signed(boolean isSigned) {
                    this.signed = isSigned;
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
                    field.signed = this.signed;
                    field.constantValue = TypeHelper.byteArrayToHexRepresentation(constantValueAsByteArray);

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

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
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

        public boolean isSigned() {
            return signed != null && signed;
        }

        public String getConstantValue() {
            return constantValue;
        }

        // TODO get rid of it!
        public interface FieldBuilder {

            FieldBuilder forName(String name_hash);

            FieldBuilder withType(Type type);

            FieldBuilder withConstantValue(byte[] value);

            FieldBuilder signed(boolean isSigned);

            FieldBuilder ofSize(String sizeFormula);

            FieldBuilder withSubFields(List<Field> subFields);

            FieldBuilder ofSubItemCount(String countFormula);

            Field build();
        }
    }

}
package fr.tduf.libunlimited.low.files.research.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.Type;

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
@JsonSerialize()
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileStructureDto implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("littleEndian")
    private Boolean littleEndian;

    @JsonProperty("cryptoMode")
    private Integer cryptoMode;

    @JsonProperty("fields")
    private List<Field> fields;

    @JsonIgnore
    @JsonProperty("comment")
    private String comment;

    /**
     * @return builder, used to generate custom values.
     */
    public static FileStructureDtoBuilder builder() {
        return new FileStructureDtoBuilder();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
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

    public static class FileStructureDtoBuilder {
        private List<Field> fields = new ArrayList<>();

        public FileStructureDtoBuilder addFields(List<Field> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public FileStructureDto build() {
            FileStructureDto fileStructureDto = new FileStructureDto();

            fileStructureDto.fields = this.fields;

            return fileStructureDto;
        }
    }

    @JsonTypeName("fileStructureField")
    @JsonSerialize
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        @JsonIgnore
        @JsonProperty("comment")
        private String comment;

        private Field() {}

        public static FieldBuilder builder() {
            return new FieldBuilder();
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", sizeFormula=" + sizeFormula +
                    '}';
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
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

        public static class FieldBuilder {
            private byte[] constantValueAsByteArray;
            private boolean signed;
            private List<Field> subFields;
            private Type type;
            private String sizeFormula;
            private String name;

            public FieldBuilder forName(String name) {
                this.name = name;
                return this;
            }

            public FieldBuilder withType(Type type) {
                this.type = type;
                return this;
            }

            public FieldBuilder withConstantValue(byte[] value) {
                this.type = CONSTANT;
                this.constantValueAsByteArray = value;
                this.sizeFormula = Integer.toString(value.length);
                return this;
            }

            public FieldBuilder signed(boolean isSigned) {
                this.signed = isSigned;
                return this;
            }

            public FieldBuilder ofSize(String sizeFormula) {
                this.sizeFormula = sizeFormula;
                return this;
            }

            public FieldBuilder withSubFields(List<Field> subFields) {
                this.subFields = subFields;
                return this;
            }

            public FieldBuilder ofSubItemCount(String countFormula) {
                this.sizeFormula = countFormula;
                return this;
            }

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
        }
    }
}

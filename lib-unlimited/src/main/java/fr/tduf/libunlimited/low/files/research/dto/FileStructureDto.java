package fr.tduf.libunlimited.low.files.research.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
        private final List<Field> fields = new ArrayList<>();

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

        @JsonProperty("contentsSize")
        private String contentsSizeFormula;

        @JsonProperty("constantValue")
        private String constantValue;

        @JsonProperty("constantChecked")
        private Boolean constantChecked;

        @JsonProperty("subFields")
        private List<Field> subFields;

        @JsonProperty("condition")
        private String condition;

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
                    ", signed=" + signed +
                    ", sizeFormula=" + sizeFormula +
                    ", contentsSizeFormula=" + contentsSizeFormula +
                    ", constantValue=" + constantValue +
                    ", constantChecked=" + constantChecked +
                    ", condition=" + condition +
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

        public String getContentsSizeFormula() {
            return contentsSizeFormula;
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

        public boolean isConstantChecked() {
            return constantChecked == null || constantChecked;
        }

        public String getCondition() { return condition; }

        public static class FieldBuilder {
            private byte[] constantValueAsByteArray;
            private boolean signed;
            private List<Field> subFields;
            private Type type;
            private String sizeFormula;
            private String contentsSizeFormula;
            private String name;
            private String condition;
            private boolean constantChecked;

            public FieldBuilder forName(String name) {
                this.name = name;
                return this;
            }

            public FieldBuilder withType(Type type) {
                this.type = type;
                return this;
            }

            private FieldBuilder withConstantValue(byte[] value, boolean checked) {
                this.type = CONSTANT;
                this.constantValueAsByteArray = value;
                this.sizeFormula = Integer.toString(value.length);
                this.constantChecked = checked;
                return this;
            }

            public FieldBuilder withConstantValueChecked(byte[] value) {
                return this.withConstantValue(value, true);
            }

            public FieldBuilder withConstantValueUnchecked(byte[] value) {
                return this.withConstantValue(value, false);
            }

            public FieldBuilder signed(boolean isSigned) {
                this.signed = isSigned;
                return this;
            }

            public FieldBuilder ofSize(String sizeFormula) {
                this.sizeFormula = sizeFormula;
                return this;
            }

            public FieldBuilder ofContentsSize(String contentsSizeFormula) {
                this.contentsSizeFormula = contentsSizeFormula;
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

            public FieldBuilder atCondition(String condition) {
                this.condition = condition;
                return this;
            }

            public Field build() {
                Field field = new Field();

                field.name = this.name;
                field.sizeFormula = this.sizeFormula;
                field.contentsSizeFormula = this.contentsSizeFormula;
                field.type = this.type;
                field.subFields = this.subFields;
                field.signed = this.signed;
                field.constantValue = TypeHelper.byteArrayToHexRepresentation(constantValueAsByteArray);
                field.constantChecked = this.constantChecked;
                field.condition = this.condition;

                return field;
            }
        }
    }
}

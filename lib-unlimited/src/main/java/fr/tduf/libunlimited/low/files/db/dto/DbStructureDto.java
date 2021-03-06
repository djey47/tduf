package fr.tduf.libunlimited.low.files.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents structure of TDU database topic
 */
@JsonTypeName("dbStructure")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DbStructureDto implements Serializable {
    @JsonProperty("ref")
    private String ref;

    @JsonProperty("topic")
    private DbDto.Topic topic;

    @JsonProperty("fields")
    private List<Field> fields;

    @JsonProperty("version")
    private String version;

    @JsonProperty("categoryCount")
    private Integer categoryCount;

    @JsonTypeName("dbStructureField")
    @JsonSerialize
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Field implements Serializable {

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private FieldType fieldType;

        @JsonProperty("targetRef")
        private String targetRef;

        @JsonProperty("rank")
        private int rank;

        /**
         * @return true is current field is used to locate a local or remote resource, false otherwise.
         */
        @JsonIgnore
        public boolean isAResourceField() {
            return DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED == fieldType
                    || DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED == fieldType
                    || DbStructureDto.FieldType.RESOURCE_REMOTE == fieldType;
        }

        /**
         * @return builder, used to generate custom values.
         */
        public static FieldBuilder builder() {
            return new FieldBuilder();
        }

        public static class FieldBuilder {
            private Integer rank;
            private String name;
            private FieldType fieldType;
            private String targetRef;

            public FieldBuilder forName(String name) {
                this.name = name;
                return this;
            }

            public FieldBuilder fromType(FieldType fieldType) {
                this.fieldType = fieldType;
                return this;
            }

            public FieldBuilder toTargetReference(String targetRef) {
                this.targetRef = targetRef;
                return this;
            }

            public FieldBuilder ofRank(int rank) {
                this.rank = rank;
                return this;
            }

            public Field build() {
                requireNonNull(rank, "Field rank must be specified.");

                Field field = new Field();

                field.name = this.name;
                field.fieldType = this.fieldType;
                field.targetRef = this.targetRef;
                field.rank = this.rank;

                return field;
            }
        }

        public String getName() {
            return name;
        }

        public FieldType getFieldType() {
            return fieldType;
        }

        public String getTargetRef() {
            return targetRef;
        }

        public int getRank() {
            return rank;
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o, false);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }
    }

    /**
     * Enumerates all item types
     */
    @JsonTypeName("dbStructureFieldType")
    public enum FieldType {
        /**
         * Bitfield : set of binary values (0/1)
         */
        BITFIELD("b"),
        /**
         * Value as Float ( optional . decimal symbol)
         */
        FLOAT("f"),
        /**
         * Unique identifier of entry
         */
        UID("x"),
        /**
         * Value as Integer
         */
        INTEGER("i"),
        /**
         * Value as percentage (0.0 -> 1, optional . decimal symbol)
         */
        PERCENT("p"),
        /**
         * Identifier of entry in other Topic (aka. foreign key)
         */
        REFERENCE("r"),
        /**
         * Identifier of resource in other Topic (only applies to CarPacks, Hair and Interior)
         */
        RESOURCE_REMOTE("l"),
        /**
         * Identifier of resource in current Topic.
         * Associated resource value does not depend on particular locale (will not be displayed).
         */
        RESOURCE_CURRENT_GLOBALIZED("u"),
        /**
         * Identifier of resource in current Topic.
         * Associated resource value does depend on locale (will be displayed).
         */
        RESOURCE_CURRENT_LOCALIZED("h");

        private final String code;

        FieldType(String code) {
            this.code = code;
        }

        /**
         * @return type corresponding to provided code, or null if does not exist.
         */
        public static FieldType fromCode(String code) {
            for (FieldType value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }

        public String getCode() {
            return code;
        }
    }

    public String getRef() {
        return ref;
    }

    public List<Field> getFields() {
        return fields;
    }

    public DbDto.Topic getTopic() {
        return topic;
    }

    public String getVersion() {
        return version;
    }

    public Integer getCategoryCount() {
        return categoryCount;
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbStructureDtoBuilder builder() {
        return new DbStructureDtoBuilder();
    }

    public static class DbStructureDtoBuilder {
        private int categoryCount;
        private String version;
        private DbDto.Topic topic;
        private String ref;
        private final List<Field> fields = new ArrayList<>();

        public DbStructureDtoBuilder forReference(String reference) {
            this.ref = reference;
            return this;
        }

        public DbStructureDtoBuilder addItem(Field... fields) {
            return addItems(asList(fields));
        }

        public DbStructureDtoBuilder addItems(List<Field> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public DbStructureDtoBuilder forTopic(DbDto.Topic topic) {
            this.topic = topic;
            return this;
        }

        public DbStructureDtoBuilder atVersion(String version) {
            this.version = version;
            return this;
        }

        public DbStructureDtoBuilder withCategoryCount(int categoryCount) {
            this.categoryCount = categoryCount;
            return this;
        }

        public DbStructureDto build() {
            DbStructureDto dbStructureDto = new DbStructureDto();

            dbStructureDto.topic = this.topic;
            dbStructureDto.ref = this.ref;
            dbStructureDto.fields = this.fields;
            dbStructureDto.version = this.version;
            dbStructureDto.categoryCount = this.categoryCount;

            return dbStructureDto;
        }
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o, false);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}

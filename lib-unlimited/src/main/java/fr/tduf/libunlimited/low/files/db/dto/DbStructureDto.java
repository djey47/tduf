package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Represents structure of TDU database topic
 */
@JsonTypeName("dbStructure")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbStructureDto implements Serializable {

    @JsonProperty("ref")
    private String ref;

    @JsonProperty("topic")
    private DbDto.Topic topic;

    @JsonProperty("fields")
    private List<Field> fields;

    @JsonTypeName("dbStructureField")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Field implements Serializable {

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private FieldType fieldType;

        @JsonProperty("targetRef")
        private String targetRef;

        /**
         * @return builder, used to generate custom values.
         */
        public static FieldBuilder builder() {
            return new FieldBuilder() {
                private String name;
                private FieldType fieldType;
                private String targetRef;

                @Override
                public FieldBuilder forName(String name) {
                    this.name = name;
                    return this;
                }

                @Override
                public FieldBuilder fromType(FieldType fieldType) {
                    this.fieldType = fieldType;
                    return this;
                }

                @Override
                public FieldBuilder toTargetReference(String targetRef) {
                    this.targetRef = targetRef;
                    return this;
                }

                @Override
                public Field build() {
                    Field field = new Field();

                    field.name = this.name;
                    field.fieldType = this.fieldType;
                    field.targetRef = this.targetRef;

                    return field;
                }
            };
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

        public interface FieldBuilder {

            FieldBuilder forName(String name);

            FieldBuilder fromType(FieldType fieldType);

            FieldBuilder toTargetReference(String targetRef);

            Field build();
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
         * Identifier of resource in other Topic
         */
        RESOURCE_REMOTE("l"),
        /**
         * Identifier of resource in current Topic
         */
        RESOURCE_CURRENT("u"),
        /**
         * To be defined
         */
        RESOURCE_H("h");        // TODO huh...?! what's this?

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

    /**
     * @return builder, used to generate custom values.
     */
    public static DbStructureDtoBuilder builder() {
        return new DbStructureDtoBuilder() {
            private DbDto.Topic topic;
            private String ref;
            private final List<Field> fields = newArrayList();

            @Override
            public DbStructureDtoBuilder forReference(String reference) {
                this.ref = reference;
                return this;
            }

            @Override
            public DbStructureDtoBuilder addItem(Field... fields) {
                return addItems(asList(fields));
            }

            @Override
            public DbStructureDtoBuilder addItems(List<Field> fields) {
                this.fields.addAll(fields);
                return this;
            }

            @Override
            public DbStructureDtoBuilder forTopic(DbDto.Topic topic) {
                this.topic = topic;
                return this;
            }

            @Override
            public DbStructureDto build() {
                DbStructureDto dbStructureDto = new DbStructureDto();

                dbStructureDto.topic = this.topic;
                dbStructureDto.ref = this.ref;
                dbStructureDto.fields = this.fields;

                return dbStructureDto;
            }
        };
    }

    public interface DbStructureDtoBuilder {
        DbStructureDtoBuilder forReference(String reference);

        DbStructureDtoBuilder addItem(Field... fields);

        DbStructureDtoBuilder addItems(List<Field> fields);

        DbStructureDtoBuilder forTopic(DbDto.Topic topic);

        DbStructureDto build();
    }
}
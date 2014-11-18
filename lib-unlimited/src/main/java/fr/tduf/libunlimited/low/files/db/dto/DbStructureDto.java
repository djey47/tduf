package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonIgnore;
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

    @JsonIgnore
    private String ref;

    @JsonIgnore
    private String name;

    @JsonProperty("fields")
    private List<Field> fields;

    @JsonTypeName("dbStructureField")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Field implements Serializable {

        @JsonProperty("id")
        private long id;

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
                private long id;
                private String name;
                private FieldType fieldType;
                private String targetRef;

                @Override
                public FieldBuilder withId(long id) {
                    this.id = id;
                    return this;
                }

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

                    field.id = this.id;
                    field.name = this.name;
                    field.fieldType = this.fieldType;
                    field.targetRef = this.targetRef;

                    return field;
                }
            };
        }

        public interface FieldBuilder {
            FieldBuilder withId(long id);

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
        BITFIELD("b"),
        F("f"),                 // TODO huh...?! what's this?
        UID("x"),
        INTEGER("i"),
        PERCENT("p"),
        REFERENCE("r"),
        RESOURCE_REMOTE("l"),
        RESOURCE_CURRENT("u"),
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
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public List<Field> getFields() {
        return fields;
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbStructureDtoBuilder builder() {
        return new DbStructureDtoBuilder() {
            private String name;
            private String ref;
            private final List<Field> fields = newArrayList();

            @Override
            public DbStructureDtoBuilder forName(String name) {
                this.name = name;
                return this;
            }

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
            public DbStructureDto build() {
                DbStructureDto dbStructureDto = new DbStructureDto();

                dbStructureDto.name = this.name;
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

        DbStructureDtoBuilder forName(String name);

        DbStructureDto build();
    }
}
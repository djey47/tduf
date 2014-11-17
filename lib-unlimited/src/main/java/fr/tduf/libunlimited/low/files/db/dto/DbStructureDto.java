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

    @JsonProperty("fields")
    private List<Item> fields;

    @JsonTypeName("dbStructureItem")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Item implements Serializable {
        @JsonProperty("id")
        private long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private Type type;

        /**
         * @return builder, used to generate custom values.
         */
        public static FieldBuilder builder() {
            return new FieldBuilder() {
                private Type type;
                private String name;
                private long id;

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
                public FieldBuilder fromType(Type type) {
                    this.type = type;
                    return this;
                }

                @Override
                public Item build() {
                    Item item = new Item();

                    item.id = this.id;
                    item.name = this.name;
                    item.type = this.type;

                    return item;
                }
            };
        }

        public interface FieldBuilder {
            FieldBuilder withId(long id);

            FieldBuilder forName(String name);

            FieldBuilder fromType(Type type);

            Item build();
        }
    }

    /**
     * Enumerates all item types
     */
    @JsonTypeName("dbStructureFieldType")
    public enum Type {
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

        Type(String code) {
            this.code = code;
        }

        /**
         * @return type corresponding to provided code, or null if does not exist.
         */
        public static Type fromCode(String code) {
            for (Type value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    public String getRef() {
        return ref;
    }

    public List<Item> getFields() {
        return fields;
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbStructureDtoBuilder builder() {
        return new DbStructureDtoBuilder() {
            private String ref;
            private final List<Item> items = newArrayList();

            @Override
            public DbStructureDtoBuilder forReference(String reference) {
                this.ref = reference;
                return this;
            }

            @Override
            public DbStructureDtoBuilder addItem(Item... items) {
                return addItems(asList(items));
            }

            @Override
            public DbStructureDtoBuilder addItems(List<Item> items) {
                this.items.addAll(items);
                return this;
            }

            @Override
            public DbStructureDto build() {
                DbStructureDto dbStructureDto = new DbStructureDto();

                dbStructureDto.ref = this.ref;
                dbStructureDto.fields = this.items;

                return dbStructureDto;
            }
        };
    }

    public interface DbStructureDtoBuilder {
        DbStructureDtoBuilder forReference(String reference);

        DbStructureDtoBuilder addItem(Item... items);

        DbStructureDtoBuilder addItems(List<Item> items);

        DbStructureDto build();
    }
}
package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Represents structure of TDU database topic
 */
@JsonTypeName("dbStructure")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbStructureDto implements Serializable {
    @JsonProperty("items")
    private List<Item> items;

    @JsonTypeName("item")
    public static class Item implements Serializable {
        @JsonProperty("id")
        private long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private Type type;

        @JsonProperty("target")
        private String target;

        /**
         * @return builder, used to generate custom values.
         */
        public static ItemBuilder builder() {
            return new ItemBuilder() {
                private Type type;
                private String target;
                private String name;
                private long id;

                @Override
                public ItemBuilder withId(long id) {
                    this.id = id;
                    return this;
                }

                @Override
                public ItemBuilder withName(String name) {
                    this.name = name;
                    return this;
                }

                @Override
                public ItemBuilder fromType(Type type) {
                    this.type = type;
                    return this;
                }

                @Override
                public ItemBuilder forTarget(String target) {
                    this.target = target;
                    return this;
                }

                @Override
                public Item build() {
                    Item item = new Item();

                    item.id = this.id;
                    item.name = this.name;
                    item.target = this.target;
                    item.type = this.type;

                    return item;
                }
            };
        }

        public interface ItemBuilder {
            ItemBuilder withId(long id);

            ItemBuilder withName(String name);

            ItemBuilder fromType(Type type);

            ItemBuilder forTarget(String target);

            Item build();
        }
    }

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
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbStructureDtoBuilder builder() {
        return new DbStructureDtoBuilder() {
            private List<Item> items = newArrayList();

            @Override
            public DbStructureDtoBuilder addItem(Item item) {
                this.items.add(item);
                return this;
            }

            @Override
            public DbStructureDto build() {
                DbStructureDto dbStructureDto = new DbStructureDto();

                dbStructureDto.items = this.items;

                return dbStructureDto;
            }
        };
    }

    public interface DbStructureDtoBuilder {
        DbStructureDtoBuilder addItem(Item item);

        DbStructureDto build();
    }
}
package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Represents contents of TDU database topic
 */
@JsonTypeName("db")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbDataDto implements Serializable {
    @JsonProperty("entries")
    private List<Entry> entries;

    @JsonTypeName("dbEntry")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Entry {

        @JsonProperty("id")
        private long id;

        @JsonProperty("items")
        private List<Item> items;

        /**
         * @return builder, used to generate custom values.
         */
        public static EntryBuilder builder() {
            return new EntryBuilder() {
                private final List<Item> items = newArrayList();
                private long id;

                @Override
                public EntryBuilder forId(long id) {
                    this.id = id;
                    return this;
                }

                @Override
                public EntryBuilder addItem(Item... item) {
                    return addItems(asList(item));
                }

                @Override
                public EntryBuilder addItems(List<Item> items) {
                    this.items.addAll(items);
                    return this;
                }

                @Override
                public Entry build() {
                    Entry entry = new Entry();

                    entry.id = this.id;
                    entry.items = this.items;

                    return entry;
                }
            };
        }

        public long getId() {
            return id;
        }

        public List<Item> getItems() {
            return items;
        }

        public interface EntryBuilder {

            EntryBuilder forId(long id);

            EntryBuilder addItem(Item... item);

            EntryBuilder addItems(List<Item> items);

            Entry build();

        }
    }
    @JsonTypeName("dbEntryItem")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Item {

        @JsonProperty("name")
        private String name;

        @JsonProperty("rawValue")
        private String rawValue;

        public static ItemBuilder builder() {
            return new ItemBuilder() {
                private String name;
                private String raw;

                @Override
                public ItemBuilder forName(String name) {
                    this.name = name;
                    return this;
                }

                @Override
                public ItemBuilder withRawValue(String rawValue) {
                    this.raw = rawValue;
                    return this;
                }

                @Override
                public Item build() {
                    Item item = new Item();

                    item.rawValue = this.raw;
                    item.name = this.name;

                    return item;
                }
            };
        }

        public String getName() {
            return name;
        }

        public String getRawValue() {
            return rawValue;
        }

        public interface ItemBuilder {
            ItemBuilder forName(String name);

            ItemBuilder withRawValue(String singleValue);

            Item build();
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbDataDtoBuilder builder() {
        return new DbDataDtoBuilder() {
            private List<Entry> entries = newArrayList();

            @Override
            public DbDataDtoBuilder addEntry(Entry... entry) {
                return addEntries(asList(entry));
            }

            @Override
            public DbDataDtoBuilder addEntries(List<Entry> entries) {
                this.entries.addAll(entries);
                return this;
            }

            @Override
            public DbDataDto build() {
                DbDataDto dbDataDto = new DbDataDto();

                dbDataDto.entries = this.entries;

                return dbDataDto;
            }
        };
    }

    public interface DbDataDtoBuilder {
        DbDataDtoBuilder addEntry(Entry... entry);

        DbDataDtoBuilder addEntries(List<Entry> entries);

        DbDataDto build();
    }
}
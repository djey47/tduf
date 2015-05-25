package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

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
                private final List<Item> items = new ArrayList<>();
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

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public long getId() {
            return id;
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

        @JsonProperty("switchValues")
        private List<SwitchValue> switchValues;

        @JsonProperty("fieldRank")
        private int fieldRank;

        public static ItemBuilder builder() {
            return new ItemBuilder() {
                private List<SwitchValue> switchValues;
                private Integer fieldRank;
                private String name;
                private String raw;

                @Override
                public ItemBuilder fromStructureField(DbStructureDto.Field field) {
                    this.fieldRank = field.getRank();
                    this.name = field.getName();
                    return this;
                }

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
                public ItemBuilder withSwitchValues(List<SwitchValue> values) {
                    this.switchValues = values;
                    return this;
                }

                @Override
                public ItemBuilder ofFieldRank(int fieldRank) {
                    this.fieldRank = fieldRank;
                    return this;
                }

                @Override
                public Item build() {
                    requireNonNull(fieldRank, "Rank of associated field must be specified.");

                    Item item = new Item();

                    item.rawValue = this.raw;
                    item.name = this.name;
                    item.fieldRank = this.fieldRank;
                    item.switchValues = this.switchValues;

                    return item;
                }
            };
        }

        /**
         * Increases field rank by one unit.
         */
        public void shiftFieldRankRight() {
            this.fieldRank++;
        }

        public String getName() {
            return name;
        }

        public String getRawValue() {
            return rawValue;
        }

        public void setRawValue(String rawValue) {
            this.rawValue = rawValue;
        }

        public int getFieldRank() {
            return fieldRank;
        }

        public List<SwitchValue> getSwitchValues() {
            return switchValues;
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

        public interface ItemBuilder {
            ItemBuilder fromStructureField(DbStructureDto.Field field);

            ItemBuilder forName(String name);

            ItemBuilder withRawValue(String singleValue);

            ItemBuilder withSwitchValues(List<SwitchValue> values);

            ItemBuilder ofFieldRank(int fieldRank);

            Item build();
        }
    }

    @JsonTypeName("dbBitFieldSwitchValue")
    public static class SwitchValue {

        @JsonProperty("index")
        private final int index;

        @JsonProperty("name")
        private final String name;

        @JsonProperty("enabled")
        private final boolean enabled;

        public SwitchValue() {
            index = 0;
            name = null;
            enabled = false;
        }

        public SwitchValue(int index, String name, boolean enabled) {
            this.index = index;
            this.enabled = enabled;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
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
            private List<Entry> entries = new ArrayList<>();

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

    public interface DbDataDtoBuilder {
        DbDataDtoBuilder addEntry(Entry... entry);

        DbDataDtoBuilder addEntries(List<Entry> entries);

        DbDataDto build();
    }
}
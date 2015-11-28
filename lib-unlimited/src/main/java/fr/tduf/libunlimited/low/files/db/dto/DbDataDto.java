package fr.tduf.libunlimited.low.files.db.dto;

import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.BITFIELD;
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

        /**
         * Decreases current entry identifier from one unit.
         * To be used on all next entries when an entry gets deleted.
         */
        public void shiftIdUp() {
            if (id > 0) {
                id--;
            }
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

        @JsonIgnore
        private DbDto.Topic topicForBitfield;

        @JsonIgnore
        private DbDto.Topic getTopicForBitfield() {
            return topicForBitfield;
        }

        public static ItemBuilder builder() {
            return new ItemBuilder() {
                private Integer fieldRank;
                private String name;
                private String raw;

                private boolean isBitField = false;
                private DbDto.Topic topic;

                @Override
                public ItemBuilder bitFieldForTopic(boolean isBitField, DbDto.Topic topic) {
                    this.isBitField = isBitField;
                    this.topic = topic;
                    return this;
                }

                @Override
                public ItemBuilder fromStructureFieldAndTopic(DbStructureDto.Field field, DbDto.Topic topic) {
                    this.fieldRank = field.getRank();
                    this.name = field.getName();

                    boolean isBitfield = field.getFieldType() == BITFIELD;
                    this.isBitField = isBitfield;
                    this.topic = isBitfield ? topic : null;

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
                public ItemBuilder ofFieldRank(int fieldRank) {
                    this.fieldRank = fieldRank;
                    return this;
                }

                @Override
                public ItemBuilder fromExisting(Item contentItem) {
                    return DbDataDto.Item.builder()
                                .forName(contentItem.getName())
                                .ofFieldRank(contentItem.getFieldRank())
                                .withRawValue(contentItem.getRawValue())
                                .bitFieldForTopic(contentItem.isBitfield(),  contentItem.getTopicForBitfield());
                }

                @Override
                public Item build() {
                    requireNonNull(fieldRank, "Rank of associated field must be specified.");

                    Item item = new Item();

                    item.rawValue = this.raw;
                    item.name = this.name;
                    item.fieldRank = this.fieldRank;

                    if (isBitField) {
                        item.switchValues = buildSwitchValues();
                        item.topicForBitfield = this.topic;
                    }

                    return item;
                }

                private List<SwitchValue> buildSwitchValues() {
                    requireNonNull(raw, "A raw value is required");
                    requireNonNull(topic, "A database topic is required");

                    BitfieldHelper bitfieldHelper = new BitfieldHelper();
                    Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReference = bitfieldHelper.getBitfieldReferenceForTopic(topic);

                    List<DbDataDto.SwitchValue> switchValues = new ArrayList<>();
                    bitfieldReference.ifPresent((refs) -> {

                        List<Boolean> values = bitfieldHelper.resolve(topic, raw).get();
                        refs.stream()

                                .forEach((ref) -> {
                                    boolean switchState = values.get(ref.getIndex() - 1);
                                    switchValues.add(new DbDataDto.SwitchValue(ref.getIndex(), ref.getLabel(), switchState));
                                });
                    });

                    return switchValues;
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

        @JsonIgnore
        private boolean isBitfield() {
            return switchValues != null;
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
            ItemBuilder bitFieldForTopic(boolean isBitField, DbDto.Topic topic);

            ItemBuilder fromStructureFieldAndTopic(DbStructureDto.Field field, DbDto.Topic topic);

            ItemBuilder forName(String name);

            ItemBuilder withRawValue(String singleValue);

            ItemBuilder ofFieldRank(int fieldRank);

            ItemBuilder fromExisting(Item contentItem);

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

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
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
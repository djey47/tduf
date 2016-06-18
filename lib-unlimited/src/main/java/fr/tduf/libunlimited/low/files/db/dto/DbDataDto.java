package fr.tduf.libunlimited.low.files.db.dto;

import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.BITFIELD;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
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

    @JsonIgnore
    private Map<Long, Entry> entriesByInternalIdentifier;

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

        public void addItemAtRank(int fieldRank, Item item) {
            items.add(fieldRank - 1, item);

            // Rank update
            item.fieldRank = fieldRank;
            for (int i = fieldRank ; i < items.size() ; i++) {
                if (items.get(i).fieldRank != fieldRank + 1) {
                    items.get(i).shiftFieldRankRight();
                }
            }
        }

        public void appendItem(Item item) {
            items.add(item);
        }

        public void replaceItems(List<Item> newItems) {
            items.clear();
            items.addAll(newItems);
        }

        public List<Item> getItems() {
            return Collections.unmodifiableList(items);
        }

        public long getId() {
            return id;
        }

        public Optional<Item> getItemAtRank(int fieldRank) {
            return items.stream()
                    .filter((item) -> item.fieldRank == fieldRank)
                    .findAny();
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

        private void shiftIdUp() {
            if (id > 0) {
                id--;
            }
        }

        private void shiftIdDown() {
            id++;
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("value")
        private String rawValue;

        @JsonProperty("switches")
        private List<SwitchValue> switchValues;

        @JsonProperty("rank")
        private int fieldRank;

        public static ItemBuilder builder() {
            return new ItemBuilder() {
                private Integer fieldRank;
                private String raw;

                private boolean isBitField = false;
                private DbDto.Topic topicForBitField;

                @Override
                public ItemBuilder bitFieldForTopic(boolean isBitField, DbDto.Topic topic) {
                    this.isBitField = isBitField;
                    this.topicForBitField = topic;
                    return this;
                }

                @Override
                public ItemBuilder fromStructureFieldAndTopic(DbStructureDto.Field field, DbDto.Topic topic) {
                    this.fieldRank = field.getRank();

                    boolean isBitfield = field.getFieldType() == BITFIELD;
                    this.isBitField = isBitfield;
                    this.topicForBitField = isBitfield ? topic : null;

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
                public ItemBuilder fromExisting(Item contentItem, DbDto.Topic topic) {
                    return DbDataDto.Item.builder()
                            .ofFieldRank(contentItem.getFieldRank())
                            .withRawValue(contentItem.getRawValue())
                            .bitFieldForTopic(contentItem.isBitfield(), topic);
                }

                @Override
                public Item build() {
                    requireNonNull(fieldRank, "Rank of associated field must be specified.");

                    Item item = new Item();

                    item.rawValue = this.raw;
                    item.fieldRank = this.fieldRank;

                    if (isBitField) {
                        item.switchValues = buildSwitchValues();
                    }

                    return item;
                }

                private List<SwitchValue> buildSwitchValues() {
                    requireNonNull(raw, "A raw value is required");
                    requireNonNull(topicForBitField, "A database topic is required");

                    BitfieldHelper bitfieldHelper = new BitfieldHelper();
                    Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReference = bitfieldHelper.getBitfieldReferenceForTopic(topicForBitField);

                    List<DbDataDto.SwitchValue> switchValues = new ArrayList<>();
                    bitfieldReference.ifPresent((refs) -> {

                        List<Boolean> values = bitfieldHelper.resolve(topicForBitField, raw).get();
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

        private void shiftFieldRankRight() {
            this.fieldRank++;
        }

        public interface ItemBuilder {
            ItemBuilder bitFieldForTopic(boolean isBitField, DbDto.Topic topic);

            ItemBuilder fromStructureFieldAndTopic(DbStructureDto.Field field, DbDto.Topic topic);

            ItemBuilder withRawValue(String singleValue);

            ItemBuilder ofFieldRank(int fieldRank);

            ItemBuilder fromExisting(Item contentItem, DbDto.Topic topic);

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
        return Collections.unmodifiableList(entries);
    }

    public Optional<Entry> getEntryWithInternalIdentifier(long internalId) {
        return ofNullable(entriesByInternalIdentifier.get(internalId));
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
                dbDataDto.entriesByInternalIdentifier = createEntryIndex(this.entries);

                return dbDataDto;
            }
        };
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
        updateEntryIndexWithNewEntry(entry);
    }

    public void addEntryWithItems(List<Item> items) {
        addEntry(DbDataDto.Entry.builder()
                .forId(entries.size())
                .addItems(items)
                .build());
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);
        removeEntryFromIndex(entry);

        // Fix identifiers of next entries
        entries.stream()
                .filter(e -> e.getId() > entry.getId())
                .forEach(e -> {
                    // TODO method
                    removeEntryFromIndex(e);
                    e.shiftIdUp();
                    updateEntryIndexWithNewEntry(e);
                });

    }

    public void moveEntryUp(Entry entry) {
        // Moves down previous entry
        getEntryWithInternalIdentifier(entry.getId() - 1)
                .ifPresent((e) -> {
                    // TODO method
                    removeEntryFromIndex(e);
                    e.shiftIdDown();
                    updateEntryIndexWithNewEntry(e);
                });

        // TODO method
        removeEntryFromIndex(entry);
        entry.shiftIdUp();
        updateEntryIndexWithNewEntry(entry);

        sortEntriesByIdentifier();
    }

    public void moveEntryDown(Entry entry) {
        // Moves up next entry
        getEntryWithInternalIdentifier(entry.getId() + 1)
                .ifPresent(e -> {
                    // TODO method
                    removeEntryFromIndex(e);
                    e.shiftIdUp();
                    updateEntryIndexWithNewEntry(e);
                });

        // TODO method
        removeEntryFromIndex(entry);
        entry.shiftIdDown();
        updateEntryIndexWithNewEntry(entry);

        sortEntriesByIdentifier();
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

    @JsonSetter("entries")
    private void setEntries(Collection<Entry> entries) {
        this.entries = new ArrayList<>(entries);
        entriesByInternalIdentifier = createEntryIndex(entries);
    }

    private void sortEntriesByIdentifier() {
        entries.sort((e1, e2) -> Long.compare(e1.getId(), e2.getId()));
    }

    private void updateEntryIndexWithNewEntry(Entry entry) {
        entriesByInternalIdentifier.put(entry.getId(), entry);
    }

    private void removeEntryFromIndex(Entry entry) {
        entriesByInternalIdentifier.remove(entry.getId());
    }

    private static Map<Long, Entry> createEntryIndex(Collection<Entry> entries) {
        return new HashMap<>(entries.stream()
                .parallel()
                .collect(Collectors.toConcurrentMap(Entry::getId, identity())));
    }

    // TODO remove interface
    public interface DbDataDtoBuilder {
        DbDataDtoBuilder addEntry(Entry... entry);

        DbDataDtoBuilder addEntries(List<Entry> entries);

        DbDataDto build();
    }
}

package fr.tduf.libunlimited.low.files.db.dto.content;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbEntry")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ContentEntryDto {

    @JsonProperty("items")
    private List<ContentItemDto> items;

    @JsonIgnore
    private DbDataDto dataHost;

    @JsonIgnore
    private int valuesHash;

    /**
     * @return builder, used to generate custom values.
     */
    public static EntryBuilder builder() {
        return new EntryBuilder();
    }

    public void addItemAtRank(int fieldRank, ContentItemDto item) {
        items.add(fieldRank - 1, item);

        // Rank update
        item.setFieldRank(fieldRank);
        for (int i = fieldRank; i < items.size(); i++) {
            if (items.get(i).getFieldRank() != fieldRank + 1) {
                items.get(i).shiftFieldRankRight();
            }
        }

        computeValuesHash();
    }

    public void appendItem(ContentItemDto item) {
        items.add(item);

        computeValuesHash();
    }

    public void replaceItems(List<ContentItemDto> newItems) {
        items.clear();
        items.addAll(newItems);

        computeValuesHash();
    }

    public List<ContentItemDto> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Optional<ContentItemDto> getItemAtRank(int fieldRank) {
        return items.stream()
                .filter(item -> item.getFieldRank() == fieldRank)
                .findAny();
    }

    public Optional<ContentItemDto> updateItemValueAtRank(String newValue, int fieldRank) {
        ContentItemDto i = getItemAtRank(fieldRank)
                .<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException("No item at field rank: " + fieldRank));

        if (newValue.equals(i.getRawValue())) {
            return empty();
        }

        i.setRawValue(newValue);

        computeValuesHash();

        return of(i);
    }

    @JsonProperty("id")
    // TODO convert to int data type
    public long getId() {
        return dataHost == null ? -1L : dataHost.getEntryId(this);
    }

    @JsonIgnore
    public int getValuesHash() {
        if (valuesHash == 0) {
            computeValuesHash();
        }
        return valuesHash;
    }

    @Override
    public boolean equals(Object that) {
        return that != null
                && that.getClass() == getClass()
                && Objects.equals(items, ((ContentEntryDto) that).items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    void shiftIdUp() {
        // TODO
    }

    void shiftIdDown() {
        // TODO
    }

    void setId(int id)
    {
        // TODO
    }

    String getFirstItemValue() {
        return getItemAtRank(1)
                .<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException("Entry has no item at field rank 1"))
                .getRawValue();
    }

    void computeValuesHash() {
        valuesHash = Objects.hashCode(items.stream()
                .map(ContentItemDto::getRawValue)
                .collect(toList())
        );
    }

    void setDataHost(DbDataDto dataHost) {
        this.dataHost = dataHost;
    }

    public static class EntryBuilder {
        private final List<ContentItemDto> items = new ArrayList<>();

        public EntryBuilder forId(long id) {
            // TODO delete
            return this;
        }

        public EntryBuilder addItem(ContentItemDto... item) {
            return addItems(asList(item));
        }

        public EntryBuilder addItems(List<ContentItemDto> items) {
            this.items.addAll(items);
            return this;
        }

        public ContentEntryDto build() {
            ContentEntryDto entry = new ContentEntryDto();

            entry.items = this.items;

            entry.computeValuesHash();

            return entry;
        }
    }
}

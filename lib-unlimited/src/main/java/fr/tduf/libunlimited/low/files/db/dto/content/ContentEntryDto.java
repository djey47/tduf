package fr.tduf.libunlimited.low.files.db.dto.content;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbEntry")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "id", "items"})
public class ContentEntryDto {

    public static final String FORMAT_PSEUDO_REF = "%s|%s";

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
        addItemAtRank(items.size() + 1, item);
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
        ContentItemDto itemObject = getItemAtRank(fieldRank)
                .orElseThrow(() -> new IllegalArgumentException("No item at field rank: " + fieldRank));

        String oldValue = itemObject.getRawValue();
        if (newValue.equals(oldValue)) {
            return empty();
        }

        itemObject.setRawValue(newValue);

        computeValuesHash();

        if (dataHost != null) {
            dataHost.updateEntryIndexByReferenceWithChangedReference(this, oldValue, newValue, fieldRank);
        }

        return of(itemObject);
    }

    @JsonIgnore
    public String getEffectiveRef() {
        if (dataHost == null) {
            return getNativeRef();
        }

        DbDto.Topic topic = this.getDataHost().getTopic();
        return DatabaseStructureQueryHelper.isUidSupportForTopic(topic) ? getNativeRef() : getPseudoRef();
    }

    @JsonProperty("id")
    public int getId() {
        return dataHost == null ? -1 : dataHost.getEntryId(this);
    }

    // Kept for proper Jackson deserialization
    public void setId(long id) {}

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
                && getValuesHash() == ((ContentEntryDto) that).getValuesHash();
    }

    @Override
    public int hashCode() {
        return getValuesHash();
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    static String getPseudoRef(String refPart1, String refPart2) {
        return String.format(FORMAT_PSEUDO_REF, refPart1, refPart2);
    }

    String getNativeRef() {
        return getItemAtRank(1)
                .orElseThrow(() -> new IllegalArgumentException("Entry has no item at field rank 1"))
                .getRawValue();
    }

    String getPseudoRef() {
        String secondValue = getItemAtRank(2)
                .orElseThrow(() -> new IllegalArgumentException("Entry has no item at field rank 2"))
                .getRawValue();
        return getPseudoRef(getNativeRef(), secondValue);
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

    DbDataDto getDataHost() {
        return dataHost;
    }

    public static class EntryBuilder {
        private final List<ContentItemDto> items = new ArrayList<>();

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

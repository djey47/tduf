package fr.tduf.libunlimited.low.files.db.dto.content;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Optional.*;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents contents of TDU database topic
 */
@JsonTypeName("db")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbDataDto implements Serializable {

    @JsonProperty("topic")
    private DbDto.Topic topic;

    @JsonProperty("entries")
    private List<ContentEntryDto> entries;

    @JsonIgnore
    private Map<String, ContentEntryDto> entriesByReference;

    /**
     * @return builder, used to generate custom values.
     */
    public static DbDataDtoBuilder builder() {
        return new DbDataDtoBuilder();
    }

    public List<ContentEntryDto> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public Optional<ContentEntryDto> getEntryWithInternalIdentifier(int internalId) {
        try {
            return of(entries.get(internalId));
        } catch (IndexOutOfBoundsException iobe) {
            return empty();
        }
    }

    public Optional<ContentEntryDto> getEntryWithReference(String ref) {
        return ofNullable(entriesByReference.get(ref));
    }

    public void addEntry(ContentEntryDto entry) {
        entry.setDataHost(this);
        entries.add(entry);
        updateEntryIndexByReferenceWithNewEntry(entry);
    }

    public void addEntryWithItems(List<ContentItemDto> items) {
        addEntry(ContentEntryDto.builder()
                .addItems(items)
                .build());
    }

    public void removeEntry(ContentEntryDto entry) {
        final int entryId = getEntryId(entry);
        if (entryId == -1) {
            return;
        }

        entries.remove(entryId);
        removeEntryFromIndexByReference(entry);
    }

    public void removeEntries(List<ContentEntryDto> entriesToDelete) {
        entriesToDelete.forEach(this::removeEntry);
    }

    public void moveEntryUp(ContentEntryDto entry) {
        moveEntry(entry, true);
    }

    public void moveEntryDown(ContentEntryDto entry) {
        moveEntry(entry, false);
    }

    public DbDto.Topic getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object that) {
        return that != null
                && that.getClass() == getClass()
                && Objects.equals(entries, ((DbDataDto) that).entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    int getEntryId(ContentEntryDto contentEntry) {
        int index = 0;
        for (ContentEntryDto entry : entries) {
            if (entry == contentEntry) {
                return index;
            }
            index++;
        }
        return -1;
    }

    Map<String, ContentEntryDto> getEntriesByReference() {
        return entriesByReference;
    }

    @JsonSetter("entries")
    void setEntries(Collection<ContentEntryDto> entries) {
        this.entries = new ArrayList<>(entries);

        createEntryIndexByReference(entries);

        entries.forEach(entry -> {
            entry.computeValuesHash();
            entry.setDataHost(this);
        });
    }

    private void updateEntryIndexByReferenceWithNewEntry(ContentEntryDto entry) {
        if (entriesByReference != null) {
            entriesByReference.put(getEffectiveRef(entry), entry);
        }
    }

    private void removeEntryFromIndexByReference(ContentEntryDto entry) {
        if (entriesByReference != null) {
            entriesByReference.remove(getEffectiveRef(entry));
        }
    }

    private String getEffectiveRef(ContentEntryDto entry) {
        return DatabaseStructureQueryHelper.isUidSupportForTopic(topic) ? entry.getNativeRef() : entry.getPseudoRef();
    }

    private void createEntryIndexByReference(Collection<ContentEntryDto> entries) {
        entriesByReference = new HashMap<>(entries.stream()
                .parallel()
                .collect(Collectors.toConcurrentMap(
                        this::getEffectiveRef,
                        identity(),
                        (e1, e2) -> e2)
                ));
    }

    private void moveEntry(ContentEntryDto entry, boolean up) {
        int entryId = entry.getId();
        if (entryId == -1) {
            return;
        }

        int neighbourEntryId = up ? entryId - 1  : entryId + 1;
        if (neighbourEntryId < 0 || neighbourEntryId >= entries.size()) {
            return;
        }

        ContentEntryDto neighbourEntry = getEntryWithInternalIdentifier(neighbourEntryId)
                .orElseThrow(() -> new IllegalArgumentException("No neighbour entry with identifier: " + neighbourEntryId));

        entries.set(entryId, neighbourEntry);
        entries.set(neighbourEntryId, entry);
    }

    public static class DbDataDtoBuilder {
        private List<ContentEntryDto> entries = new ArrayList<>();
        private DbDto.Topic topic;

        public DbDataDtoBuilder addEntry(ContentEntryDto... entry) {
            return addEntries(asList(entry));
        }

        public DbDataDtoBuilder addEntries(List<ContentEntryDto> entries) {
            this.entries.addAll(entries);
            return this;
        }

        public DbDataDtoBuilder forTopic(DbDto.Topic topic) {
            this.topic = topic;
            return this;
        }

        public DbDataDto build() {
            DbDataDto dbDataDto = new DbDataDto();

            dbDataDto.entries = entries;
            dbDataDto.topic = topic;

            dbDataDto.createEntryIndexByReference(entries);

            dbDataDto.entries.forEach(entry -> entry.setDataHost(dbDataDto));

            return dbDataDto;
        }
    }
}

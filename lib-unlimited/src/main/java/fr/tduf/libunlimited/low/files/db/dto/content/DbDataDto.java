package fr.tduf.libunlimited.low.files.db.dto.content;

import com.esotericsoftware.minlog.Log;
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
// TODO add tests
public class DbDataDto implements Serializable {
    private static final String THIS_CLASS_NAME = DbDataDto.class.getSimpleName();

    @JsonProperty("topic")
    private DbDto.Topic topic;

    @JsonProperty("entries")
    private List<ContentEntryDto> entries;

    @JsonIgnore
    private Map<String, ContentEntryDto> entriesByReference;

    public List<ContentEntryDto> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    long getEntryId(ContentEntryDto contentEntry) {
        int index = 0;
        for (ContentEntryDto entry : entries) {
            if (entry == contentEntry) {
                return index;
            }
            index++;
        }
        return -1;
    }

    // TODO pass to int
    public Optional<ContentEntryDto> getEntryWithInternalIdentifier(long internalId) {
        try {
            return of(entries.get((int)internalId));
        } catch (IndexOutOfBoundsException iobe) {
            return empty();
        }
    }

    public Optional<ContentEntryDto> getEntryWithReference(String ref) {
        if (entriesByReference == null) {
            Log.warn(THIS_CLASS_NAME, "Will process entry search without index. Please fix contents for topic: " + topic);
            return entries.stream()
                    .filter(entry -> entry.getFirstItemValue().equals(ref))
                    .findFirst();
        }
        return ofNullable(entriesByReference.get(ref));
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbDataDtoBuilder builder() {
        return new DbDataDtoBuilder();
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
        entries.remove((int)getEntryId(entry));
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

    // TODO use swap algorithm when new id mechanism
    private void moveEntry(ContentEntryDto entry, boolean up) {
        // Moves previous entry down or next entry up
        getEntryWithInternalIdentifier(up ? entry.getId() - 1 : entry.getId() + 1)
                .ifPresent(e -> {
                    if (up) {
                        e.shiftIdDown();
                    } else {
                        e.shiftIdUp();
                    }
                });

        if (up) {
            entry.shiftIdUp();
        } else {
            entry.shiftIdDown();
        }
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

    @JsonSetter("entries")
    private void setEntries(Collection<ContentEntryDto> entries) {
        this.entries = new ArrayList<>(entries);

        if (topic == null || DatabaseStructureQueryHelper.isUidSupportForTopic(topic)) {
            entriesByReference = createEntryIndexByReference(entries);
        }

        entries.forEach(entry -> {
            entry.computeValuesHash();
            entry.setDataHost(this);
        });
    }

    private void updateEntryIndexByReferenceWithNewEntry(ContentEntryDto entry) {
        if (entriesByReference != null) {
            entriesByReference.put(entry.getFirstItemValue(), entry);
        }
    }

    private void removeEntryFromIndexByReference(ContentEntryDto entry) {
        if (entriesByReference != null) {
            entriesByReference.remove(entry.getFirstItemValue());
        }
    }

    private static Map<String, ContentEntryDto> createEntryIndexByReference(Collection<ContentEntryDto> entries) {
        return new HashMap<>(entries.stream()
                .parallel()
                .collect(Collectors.toConcurrentMap(
                        ContentEntryDto::getFirstItemValue,
                        identity(),
                        (e1, e2) -> e2)
                ));
    }

    public static class DbDataDtoBuilder {
        private List<ContentEntryDto> entries = new ArrayList<>();
        private boolean refIndexSupport = false;
        private DbDto.Topic topic;

        public DbDataDtoBuilder addEntry(ContentEntryDto... entry) {
            return addEntries(asList(entry));
        }

        public DbDataDtoBuilder addEntries(List<ContentEntryDto> entries) {
            this.entries.addAll(entries);
            return this;
        }

        public DbDataDtoBuilder supportingReferenceIndex(boolean refSupport) {
            this.refIndexSupport = refSupport;
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

            if (refIndexSupport) {
                dbDataDto.entriesByReference = createEntryIndexByReference(entries);
            }

            dbDataDto.entries.forEach(entry -> entry.setDataHost(dbDataDto));

            return dbDataDto;
        }
    }
}

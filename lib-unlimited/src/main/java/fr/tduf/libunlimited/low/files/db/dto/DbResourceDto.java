package fr.tduf.libunlimited.low.files.db.dto;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents contents of TDU database resources (multilingual)
 */
public class DbResourceDto {
    @JsonProperty("version")
    private String version;

    @JsonProperty("categoryCount")
    private Integer categoryCount;

    @JsonIgnore
    private Map<String, Entry> entriesByReference = new LinkedHashMap<>();

    private DbResourceDto() {}

    private static Map<String, Entry> createResourceIndex(Collection<Entry> entries) {
        return entries.stream()
                .collect(toMap(
                        Entry::getReference,
                        Function.identity(),
                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new)
                );
    }

    public static DbResourceDto.DbResourceEnhancedDtoBuilder builder() {
        return new DbResourceDto.DbResourceEnhancedDtoBuilder();
    }

    public Optional<DbResourceDto.Entry> getEntryByReference(String reference) {
        return ofNullable(entriesByReference.get(reference));
    }

    public Entry addEntryByReference(String reference) {
        getEntryByReference(reference)
                .ifPresent(entry -> {
                    throw new IllegalArgumentException("An entry with given reference already exists: " + reference);
                });

        DbResourceDto.Entry newEntry = DbResourceDto.Entry.builder()
                .forReference(reference)
                .build();

        entriesByReference.put(reference, newEntry);

        return newEntry;
    }

    public void removeEntryByReference(String reference) {
        entriesByReference.remove(reference);
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

    @JsonProperty("entries")
    public Collection<Entry> getEntries() {
        return unmodifiableCollection(entriesByReference.values());
    }

    @JsonSetter("entries")
    private void setEntries(Collection<Entry> entries) {
        entriesByReference = createResourceIndex(entries);
    }

    public String getVersion() {
        return version;
    }

    public Integer getCategoryCount() {
        return categoryCount;
    }

    public static class DbResourceEnhancedDtoBuilder {
        private String version;
        private int categoryCount;
        private final LinkedHashSet<Entry> entries = new LinkedHashSet<>();

        public DbResourceEnhancedDtoBuilder atVersion(String version) {
            this.version = version;
            return this;
        }

        public DbResourceEnhancedDtoBuilder withCategoryCount(int categoryCount) {
            this.categoryCount = categoryCount;
            return this;
        }

        public DbResourceEnhancedDtoBuilder containingEntries(Collection<Entry> entries) {
            this.entries.clear();
            this.entries.addAll(entries);
            return this;
        }

        public DbResourceDto build() {
            DbResourceDto dbResourceDto = new DbResourceDto();

            dbResourceDto.categoryCount = requireNonNull(categoryCount, "Category count is required.");
            dbResourceDto.version = requireNonNull(version, "Version is required.");
            dbResourceDto.entriesByReference = createResourceIndex(entries);

            return dbResourceDto;
        }
    }

    @JsonTypeName("dbResourceEnhancedEntry")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Entry implements Serializable {
        @JsonProperty("ref")
        private String reference;

        @JsonProperty("items")
        private LinkedHashSet<Item> items;

        private Entry() {
        }

        public static EntryBuilder builder() {
            return new EntryBuilder();
        }

        /**
         * @return available item for specified locale, empty otherwise.
         */
        public Optional<Item> getItemForLocale(Locale locale) {
            return items.stream()

                    .filter((item) -> item.locale == locale)

                    .findAny();
        }

        /**
         * @return available value for any locale, empty otherwise.
         */
        public Optional<String> pickValue() {
            return getPresentLocales().stream()
                    .findAny()
                    .flatMap(this::getValueForLocale);
        }

        /**
         * @return available value for specified locale, empty otherwise.
         */
        public Optional<String> getValueForLocale(fr.tduf.libunlimited.common.game.domain.Locale locale) {
            return getItemForLocale(locale)
                    .map((item) -> item.value);
        }

        /**
         * defines given value for every locale.
         * @return current entry
         */
        public Entry setValue(String value) {
            fr.tduf.libunlimited.common.game.domain.Locale.valuesAsStream()
                    .forEach((locale) -> setValueForLocale(value, locale));

            return this;
        }

        /**
         * defines given value for specified locale
         * @return current entry
         */
        public Entry setValueForLocale(String value, fr.tduf.libunlimited.common.game.domain.Locale locale) {
            Optional<Item> potentialItem = getItemForLocale(locale);

            if (potentialItem.isPresent()) {

                potentialItem.get().value = value;

            } else {

                items.add(Item.builder()
                        .withLocale(locale)
                        .withValue(value)
                        .build());

            }

            return this;
        }

        /**
         * does nothing if value does not exist for given locale
         * @return current entry
         */
        public Entry removeValueForLocale(fr.tduf.libunlimited.common.game.domain.Locale locale) {
            items.removeIf((item) -> item.locale == locale);

            return this;
        }

        @JsonIgnore
        public int getItemCount() {
            return items.size();
        }

        @JsonIgnore
        public Set<Locale> getPresentLocales() {
            return items.stream()

                    .map((item) -> item.locale)

                    .collect(toSet());
        }

        @JsonIgnore
        public Set<fr.tduf.libunlimited.common.game.domain.Locale> getMissingLocales() {
            Set<fr.tduf.libunlimited.common.game.domain.Locale> missingLocales = new HashSet<>(asList(Locale.values()));
            missingLocales.removeAll(getPresentLocales());
            return missingLocales;
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            // Reference is the only data required for hashing, other ones may mutate.
            return hash(reference);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        public String getReference() {
            return reference;
        }

        public static class EntryBuilder {
            private String reference;

            private final LinkedHashSet<Item> items = new LinkedHashSet<>();

            public EntryBuilder forReference(String ref) {
                this.reference = ref;
                return this;
            }

            public EntryBuilder withItems(Collection<Item> items) {
                this.items.clear();
                this.items.addAll(items);
                return this;
            }

            public Entry build() {
                Entry entry = new Entry();

                entry.reference = requireNonNull(reference, "Resource reference is required.");
                entry.items = items;

                return entry;
            }
        }
    }

    @JsonTypeName("dbResourceEnhancedEntryItem")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Item implements Serializable {
        private Locale locale;

        @JsonProperty("value")
        private String value;

        private Item() {}

        public static ItemBuilder builder() {
            return new ItemBuilder();
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            // Locale is the only data required for hashing, other ones may mutate.
            return hash(locale);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        public String getValue() {
            return value;
        }

        @JsonProperty("locale")
        public String getLocaleCode() {
            return locale.getCode();
        }

        @JsonSetter("locale")
        private void setLocaleFromCode(String localeCode) {
            locale = Locale.fromCode(localeCode);
        }

        public static class ItemBuilder {
            private fr.tduf.libunlimited.common.game.domain.Locale locale;
            private String value;

            public ItemBuilder withLocale(fr.tduf.libunlimited.common.game.domain.Locale locale) {
                this.locale = locale;
                return this;
            }

            public ItemBuilder withValue(String value) {
                this.value = value;
                return this;
            }

            public Item build() {
                Item item = new Item();

                item.locale = requireNonNull(locale, "Locale is required.");
                item.value = requireNonNull(value, "Value is required.");

                return item;
            }
        }
    }
}

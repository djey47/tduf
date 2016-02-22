package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents contents of TDU database resources (multilingual)
 */
public class DbResourceEnhancedDto {
    @JsonProperty("version")
    private String version;

    @JsonProperty("categoryCount")
    private Integer categoryCount;

    @JsonProperty("entries")
    private LinkedHashSet<Entry> entries;

    private DbResourceEnhancedDto() {
    }

    public static DbResourceEnhancedDto.DbResourceEnhancedDtoBuilder builder() {
        return new DbResourceEnhancedDto.DbResourceEnhancedDtoBuilder();
    }

    public Optional<DbResourceEnhancedDto.Entry> getEntryByReference(String reference) {
        return entries.stream()

                .filter((entry -> entry.getReference().equals(reference)))

                .findAny();
    }

    public Entry addEntryByReference(String reference) {
        getEntryByReference(reference)
                .ifPresent((entry) -> {
                    throw new IllegalArgumentException("An entry with given reference already exists: " + reference);
                });

        DbResourceEnhancedDto.Entry newEntry = DbResourceEnhancedDto.Entry.builder()
                .forReference(reference)
                .build();

        entries.add(newEntry);

        return newEntry;
    }

    public void removeEntryByReference(String reference) {
        entries.removeIf((entry) -> entry.getReference().equals(reference));
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

    public Set<Entry> getEntries() {
        return entries;
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

        public DbResourceEnhancedDto build() {
            DbResourceEnhancedDto dbResourceDto = new DbResourceEnhancedDto();

            dbResourceDto.categoryCount = requireNonNull(categoryCount, "Category count is required.");
            dbResourceDto.version = requireNonNull(version, "Version is required.");
            dbResourceDto.entries = this.entries;

            return dbResourceDto;
        }
    }

    /**
     * All culture variants for game files
     */
    public enum Locale {
        FRANCE("fr"),
        GERMANY("ge"),
        UNITED_STATES("us"),
        KOREA("ko"),
        CHINA("ch"),
        JAPAN("ja"),
        ITALY("it"),
        SPAIN("sp");

        private final String code;

        Locale(String code) {
            this.code = code;
        }

        /**
         * Retrieves a locale value from its code.
         */
        public static Locale fromCode(String code) {
            for (Locale locale : values()) {
                if (locale.code.equals(code)) {
                    return locale;
                }
            }
            throw new IllegalArgumentException("Unknown Locale code: " + code);
        }

        public String getCode() {
            return code;
        }

        public static Stream<Locale> valuesAsStream() {
            return Stream.of(values());
        }

        @Override
        public String toString() {
            return super.toString();
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
        public Optional<String> getValueForLocale(Locale locale) {
            return getItemForLocale(locale)
                    .map((item) -> item.value);
        }

        /**
         * defines given value for every locale.
         * @return current entry
         */
        public Entry setValue(String value) {
            Stream.of(Locale.values())
                    .forEach((locale) -> setValueForLocale(value, locale));

            return this;
        }

        /**
         * defines given value for specified locale
         * @return current entry
         */
        public Entry setValueForLocale(String value, Locale locale) {
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
        public Entry removeValueForLocale(Locale locale) {
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
        public Set<Locale> getMissingLocales() {
            Set<DbResourceEnhancedDto.Locale> missingLocales = new HashSet<>(asList(DbResourceEnhancedDto.Locale.values()));
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
        @JsonProperty("locale")
        private Locale locale;

        @JsonProperty("value")
        private String value;

        private Item() {
        }

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

        public static class ItemBuilder {
            private Locale locale;
            private String value;

            public ItemBuilder withLocale(Locale locale) {
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

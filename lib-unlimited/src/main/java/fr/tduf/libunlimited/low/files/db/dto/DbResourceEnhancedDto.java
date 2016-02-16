package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
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
    private Set<Entry> entries;

    private DbResourceEnhancedDto(){}

    public static DbResourceEnhancedDto.DbResourceEnhancedDtoBuilder builder() {
        return new DbResourceEnhancedDto.DbResourceEnhancedDtoBuilder();
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
        private Set<Entry> entries;

        public DbResourceEnhancedDtoBuilder atVersion(String version) {
            this.version = version;
            return this;
        }

        public DbResourceEnhancedDtoBuilder withCategoryCount(int categoryCount) {
            this.categoryCount = categoryCount;
            return this;
        }

        public DbResourceEnhancedDtoBuilder containingEntries(Collection<Entry> entries) {
            this.entries =  new LinkedHashSet<>(entries);
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

    @JsonTypeName("dbResourceEnhancedEntry")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Entry implements Serializable {
        @JsonProperty("ref")
        private String reference;

        @JsonProperty("items")
        private Set<Item> items;

        private Entry() {}

        public static EntryBuilder builder() {
            return new EntryBuilder();
        }

        public void addItem(Item item) {
            if (items == null) {
                items = new LinkedHashSet<>();
            }
            items.add(item);
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

        public String getReference() {
            return reference;
        }

        public static class EntryBuilder {
            private String reference;
            private Set<Item> items;

            public EntryBuilder forReference(String ref) {
                this.reference = ref;
                return this;
            }

            public EntryBuilder withItems(Collection<Item> items) {
                this.items = new LinkedHashSet<>(items);
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
        private DbResourceDto.Locale locale;

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
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        public static class ItemBuilder {
            private DbResourceDto.Locale locale;
            private String value;

            public ItemBuilder withLocale(DbResourceDto.Locale locale) {
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

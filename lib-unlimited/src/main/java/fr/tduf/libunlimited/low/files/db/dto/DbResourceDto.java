package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Represents contents of TDU database resources (multilingual)
 */
@JsonTypeName("dbResource")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbResourceDto implements Serializable {

    @JsonProperty("entries")
    private List<Entry> entries;

    @JsonProperty("version")
    private String version;

    @JsonProperty("categoryCount")
    private Integer categoryCount;

    @JsonTypeName("dbResourceEntry")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Entry implements Serializable {
        @JsonProperty("ref")
        private String reference;

        @JsonProperty("localizedValues")
        private List<LocalizedValue> localizedValues;

        /**
         * @return builder, used to generate custom values.
         */
        public static EntryBuilder builder() {
            return new EntryBuilder() {
                private String reference;
                private final List<LocalizedValue> localizedValues = newArrayList();

                @Override
                public EntryBuilder forReference(String reference) {
                    this.reference = reference;
                    return this;
                }

                @Override
                public EntryBuilder addLocalizedValue(LocalizedValue localizedValue) {
                    this.localizedValues.add(localizedValue);
                    return this;
                }

                @Override
                public Entry build() {
                    Entry entry = new Entry();

                    entry.localizedValues = this.localizedValues;
                    entry.reference = this.reference;

                    return entry;
                }
            };
        }

        public List<LocalizedValue> getLocalizedValues() {
            return localizedValues;
        }

        public interface EntryBuilder {
            EntryBuilder forReference(String reference);

            EntryBuilder addLocalizedValue(LocalizedValue localizedValue);

            Entry build();
        }
    }

    @JsonTypeName("dbResourceEntryLocalizedValue")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class LocalizedValue implements Serializable {
        @JsonProperty("locale")
        private Locale locale;

        @JsonProperty("value")
        private String value;

        /**
         * @return builder, used to generate custom values.
         */
        public static LocalizedValueBuilder builder() {
            return new LocalizedValueBuilder() {
                private String value;
                private Locale locale;

                @Override
                public LocalizedValueBuilder withLocale(Locale locale) {
                    this.locale = locale;
                    return this;
                }

                @Override
                public LocalizedValueBuilder withValue(String value) {
                    this.value = value;
                    return this;
                }

                @Override
                public LocalizedValue build() {
                    LocalizedValue localizedValue = new LocalizedValue();

                    localizedValue.locale = this.locale;
                    localizedValue.value = this.value;

                    return localizedValue;
                }
            };
        }

        public interface LocalizedValueBuilder {
            LocalizedValueBuilder withLocale(Locale locale);

            LocalizedValueBuilder withValue(String value);

            LocalizedValue build();
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

        private Locale(String code) {
            this.code = code;
        }

        /**
         * Retrieves a locale value from its code.
         */
        public static Locale fromCode(String code) {
            for(Locale locale : values()) {
                if (locale.code.equals(code)) {
                    return locale;
                }
            }
            throw new IllegalArgumentException("Unknown Locale code: " + code);
        }
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbResourceDtoBuilder builder() {
        return new DbResourceDtoBuilder() {
            private int categoryCount;
            private String version;
            private final List<Entry> entries = newArrayList();

            @Override
            public DbResourceDtoBuilder addEntry(Entry entry) {
                this.entries.add(entry);
                return this;
            }

            @Override
            public DbResourceDtoBuilder addEntries(List<Entry> entries) {
                this.entries.addAll(entries);
                return this;
            }

            @Override
            public DbResourceDtoBuilder atVersion(String version) {
                this.version = version;
                return this;
            }

            @Override
            public DbResourceDtoBuilder withCategoryCount(int categoryCount) {
                this.categoryCount = categoryCount;
                return this;
            }

            @Override
            public DbResourceDto build() {
                DbResourceDto dbResourceDto = new DbResourceDto();

                dbResourceDto.entries = this.entries;
                dbResourceDto.categoryCount = this.categoryCount;
                dbResourceDto.version = this.version;

                return dbResourceDto;
            }
        };
    }

    public String getVersion() {
        return version;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public interface DbResourceDtoBuilder {
        DbResourceDtoBuilder addEntry(Entry entry);

        DbResourceDtoBuilder addEntries(List<Entry> entries);

        DbResourceDtoBuilder atVersion(String version);

        DbResourceDtoBuilder withCategoryCount(int categoryCount);

        DbResourceDto build();
    }
}
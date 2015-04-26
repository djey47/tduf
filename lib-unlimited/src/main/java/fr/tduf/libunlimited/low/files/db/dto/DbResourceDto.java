package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

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

    @JsonProperty("locale")
    private Locale locale;

    @JsonTypeName("dbResourceEntry")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Entry implements Serializable {
        @JsonProperty("ref")
        private String reference;

        @JsonProperty("value")
        private String value;

        /**
         * @return builder, used to generate custom values.
         */
        public static EntryBuilder builder() {
            return new EntryBuilder() {
                private String value;
                private String reference;

                @Override
                public EntryBuilder fromExistingEntry(Entry entry) {
                    this.reference = entry.reference;
                    this.value = entry.value;
                    return this;
                }

                @Override
                public EntryBuilder forReference(String reference) {
                    this.reference = reference;
                    return this;
                }

                @Override
                public EntryBuilder withValue(String value) {
                    this.value = value;
                    return this;
                }

                @Override
                public Entry build() {
                    Entry entry = new Entry();

                    entry.reference = this.reference;
                    entry.value = this.value;

                    return entry;
                }
            };
        }

        public String getReference() {
            return reference;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
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
            return "(" + reference + " => '" + value + "')";
        }

        public interface EntryBuilder {
            EntryBuilder fromExistingEntry(Entry entry);

            EntryBuilder forReference(String reference);

            EntryBuilder withValue(String value);

            Entry build();
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
            for(Locale locale : values()) {
                if (locale.code.equals(code)) {
                    return locale;
                }
            }
            throw new IllegalArgumentException("Unknown Locale code: " + code);
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbResourceDtoBuilder builder() {
        return new DbResourceDtoBuilder() {
            private int categoryCount;
            private String version;
            private Locale locale;
            private final List<Entry> entries = new ArrayList<>();

            @Override
            public DbResourceDtoBuilder fromExistingResource(DbResourceDto dbResourceDto) {
                this.locale = dbResourceDto.locale;
                this.categoryCount = dbResourceDto.categoryCount;
                this.version = dbResourceDto.version;

                this.entries.clear();
                this.entries.addAll(dbResourceDto.entries.stream()

                        .map( (entry) -> Entry.builder().fromExistingEntry(entry).build())

                        .collect(toList()));

                return this;
            }

            @Override
            public DbResourceDtoBuilder withLocale(Locale locale) {
                this.locale = locale;
                return this;
            }

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
                dbResourceDto.locale = this.locale;

                return dbResourceDto;
            }
        };
    }

    public Locale getLocale() {
        return locale;
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
        return locale + ": " + entries;
    }

    public interface DbResourceDtoBuilder {
        DbResourceDtoBuilder fromExistingResource(DbResourceDto dbResourceDto);

        DbResourceDtoBuilder withLocale(Locale locale);

        DbResourceDtoBuilder addEntry(Entry entry);

        DbResourceDtoBuilder addEntries(List<Entry> entries);

        DbResourceDtoBuilder atVersion(String version);

        DbResourceDtoBuilder withCategoryCount(int categoryCount);

        DbResourceDto build();
    }
}
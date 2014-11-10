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

    @JsonTypeName("entry")
    public static class Entry implements Serializable {
        @JsonProperty("id")
        private long id;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("comment")
        private String comment;

        @JsonProperty("localizedValues")
        private List<LocalizedValue> localizedValues;

        /**
         * @return true if current entry is a comment, false otherwise (it has localized values)
         */
        public boolean isComment() {
            return comment != null;
        }

        /**
         * @return builder, used to generate custom values.
         */
        public static EntryBuilder builder() {
            return new EntryBuilder() {
                private long id;
                private String comment;
                private String reference;
                private List<LocalizedValue> localizedValues = newArrayList();

                @Override
                public EntryBuilder withId(long id) {
                    this.id = id;
                    return this;
                }

                @Override
                public EntryBuilder withReference(String reference) {
                    this.reference = reference;
                    return this;
                }

                @Override
                public EntryBuilder withComment(String comment) {
                    this.comment = comment;
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

                    entry.comment = this.comment;
                    entry.id = this.id;
                    entry.localizedValues = this.localizedValues;
                    entry.reference = this.reference;

                    return entry;
                }
            };
        }

        public interface EntryBuilder {
            EntryBuilder withId(long id);

            EntryBuilder withReference(String reference);

            EntryBuilder withComment(String comment);

            EntryBuilder addLocalizedValue(LocalizedValue localizedValue);

            Entry build();
        }
    }

    @JsonTypeName("localizedValue")
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
        FR("France"),
        GE("Germany"),
        US("United States"),
        KO("Korea"),
        CH("China"),
        JA("Japan"),
        IT("Italy"),
        SP("Spain");

        private final String label;

        private Locale(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbResourceDtoBuilder builder() {
        return new DbResourceDtoBuilder() {
            private List<Entry> entries = newArrayList();

            @Override
            public DbResourceDtoBuilder addEntry(Entry entry) {

                this.entries.add(entry);


                return this;
            }

            @Override
            public DbResourceDto build() {
                DbResourceDto dbResourceDto = new DbResourceDto();

                dbResourceDto.entries = this.entries;



                return dbResourceDto;
            }
        };
    }

    public interface DbResourceDtoBuilder {
        DbResourceDto build();

        DbResourceDtoBuilder addEntry(Entry entry);
    }
}
package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.*;

import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbResourceEnhancedEntry")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResourceEntryDto implements Serializable {
    @JsonProperty("ref")
    private String reference;

    @JsonProperty("items")
    private LinkedHashSet<ResourceItemDto> items;

    private ResourceEntryDto() {
    }

    public static EntryBuilder builder() {
        return new EntryBuilder();
    }

    /**
     * @return available item for specified locale, empty otherwise.
     */
    public Optional<ResourceItemDto> getItemForLocale(Locale locale) {
        return items.stream()
                .filter(item -> {
                    Locale currentLocale = item.getLocale();
                    return DEFAULT == currentLocale || locale == currentLocale;
                })
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
                .map(ResourceItemDto::getValue);
    }

    /**
     * defines given value for every locale.
     * @return current entry
     */
    // TODO rename ?
    public ResourceEntryDto setValue(String value) {
        Locale.valuesAsStream()
                .forEach(locale -> setValueForLocale(value, locale));

        return this;
    }

    /**
     * defines global value.
     * @return current entry
     */
    // TODO remove if unused
    public ResourceEntryDto setGlobalValue(String globalValue) {
        setValueForLocale(globalValue, DEFAULT);

        return this;
    }

    /**
     * defines given value for specified locale
     * @return current entry
     */
    public ResourceEntryDto setValueForLocale(String value, Locale locale) {
        Optional<ResourceItemDto> potentialItem = getItemForLocale(locale);

        if (potentialItem.isPresent()) {

            potentialItem.get().setValue(value);

        } else {

            items.add(ResourceItemDto.builder()
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
    public ResourceEntryDto removeValueForLocale(fr.tduf.libunlimited.common.game.domain.Locale locale) {
        items.removeIf(item -> item.getLocale() == locale);

        return this;
    }

    @JsonIgnore
    public int getItemCount() {
        return items.size();
    }

    @JsonIgnore
    // TODO Check callers properly handle DEFAULT locale
    public Set<Locale> getPresentLocales() {
        return items.stream()
                .map(ResourceItemDto::getLocale)
                .collect(toSet());
    }

    @JsonIgnore
    public Set<fr.tduf.libunlimited.common.game.domain.Locale> getMissingLocales() {
        Set<Locale> presentLocales = getPresentLocales();
        if (presentLocales.contains(DEFAULT)) {
            return new HashSet<>(0);
        }
        Set<fr.tduf.libunlimited.common.game.domain.Locale> missingLocales = Locale.valuesAsStream().collect(toSet());
        missingLocales.removeAll(presentLocales);
        return missingLocales;
    }

    /**
     * @return true if entry contains global item
     */
    @JsonIgnore
    public boolean isGlobalized() {
        return getValueForLocale(DEFAULT).isPresent();
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

        private final LinkedHashSet<ResourceItemDto> items = new LinkedHashSet<>();

        public EntryBuilder forReference(String ref) {
            this.reference = ref;
            return this;
        }

        public EntryBuilder withItems(Collection<ResourceItemDto> items) {
            this.items.clear();
            this.items.addAll(items);
            return this;
        }

        public EntryBuilder withGlobalItem(String value) {
            final ResourceItemDto globalItem = ResourceItemDto.builder()
                    .withGlobalValue(value)
                    .build();
            this.items.clear();
            this.items.add(globalItem);
            return this;
        }

        public ResourceEntryDto build() {
            ResourceEntryDto entry = new ResourceEntryDto();

            entry.reference = requireNonNull(reference, "Resource reference is required.");
            entry.items = items;

            return entry;
        }
    }
}

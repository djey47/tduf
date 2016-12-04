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
                .filter(item -> locale == item.getLocale())
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
     * @return available value for specified locale, default value as fallback, empty otherwise.
     */
    public Optional<String> getValueForLocale(Locale locale) {
        Optional<ResourceItemDto> itemForLocale = getItemForLocale(locale);
        if (itemForLocale.isPresent()) {
            return itemForLocale.map(ResourceItemDto::getValue);
        } else {
            return getItemForLocale(DEFAULT).map(ResourceItemDto::getValue);
        }
    }

    /**
     * defines given default value for every locale.
     * @return current entry
     */
    // TODO rename to setDefaultValue
    public ResourceEntryDto setValue(String defaultValue) {
        setValueForLocale(defaultValue, DEFAULT);

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
            ResourceItemDto resourceItemDto;
            if (DEFAULT == locale) {
                resourceItemDto = ResourceItemDto.builder()
                        .withGlobalValue(value)
                        .build();
            } else {
                resourceItemDto = ResourceItemDto.builder()
                        .withLocale(locale)
                        .withValue(value)
                        .build();
            }
            items.add(resourceItemDto);
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
    public Set<Locale> getPresentLocales() {
        return items.stream()
                .map(ResourceItemDto::getLocale)
                .filter(locale -> DEFAULT != locale)
                .collect(toSet());
    }

    @JsonIgnore
    public Set<Locale> getMissingLocales() {
        if (getValueForLocale(DEFAULT).isPresent()) {
            return new HashSet<>(0);
        }
        Set<Locale> missingLocales = Locale.valuesAsStream().collect(toSet());
        missingLocales.removeAll(getPresentLocales());
        return missingLocales;
    }

    /**
     * @return true if entry only default item
     */
    @JsonIgnore
    public boolean isGlobalized() {
        return 1 == getItemCount()
                && getValueForLocale(DEFAULT).isPresent();
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

        /**
         * Replace all existing items
         */
        public EntryBuilder withItems(Collection<ResourceItemDto> items) {
            this.items.clear();
            this.items.addAll(items);
            return this;
        }

        // TODO rename to withDefaultItem
        public EntryBuilder withGlobalItem(String value) {
            final ResourceItemDto globalItem = ResourceItemDto.builder()
                    .withGlobalValue(value)
                    .build();
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

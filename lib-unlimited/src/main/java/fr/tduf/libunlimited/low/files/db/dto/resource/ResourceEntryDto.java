package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbResourceEnhancedEntry")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResourceEntryDto {
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

                .filter(item -> item.getLocale() == locale)

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
                .map(ResourceItemDto::getValue);
    }

    /**
     * defines given value for every locale.
     * @return current entry
     */
    public ResourceEntryDto setValue(String value) {
        fr.tduf.libunlimited.common.game.domain.Locale.valuesAsStream()
                .forEach(locale -> setValueForLocale(value, locale));

        return this;
    }

    /**
     * defines given value for specified locale
     * @return current entry
     */
    public ResourceEntryDto setValueForLocale(String value, fr.tduf.libunlimited.common.game.domain.Locale locale) {
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
    public Set<Locale> getPresentLocales() {
        return items.stream()

                .map(item -> item.getLocale())

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

        public ResourceEntryDto build() {
            ResourceEntryDto entry = new ResourceEntryDto();

            entry.reference = requireNonNull(reference, "Resource reference is required.");
            entry.items = items;

            return entry;
        }
    }
}

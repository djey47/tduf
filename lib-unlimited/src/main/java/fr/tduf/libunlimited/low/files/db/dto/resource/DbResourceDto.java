package fr.tduf.libunlimited.low.files.db.dto.resource;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents contents of TDU database resources (multilingual)
 */
public class DbResourceDto implements Serializable {
    @JsonProperty("version")
    private String version;

    @JsonProperty("categoryCount")
    private Integer categoryCount;

    @JsonIgnore
    private Map<String, ResourceEntryDto> entriesByReference = new LinkedHashMap<>();

    private DbResourceDto() {}

    private static Map<String, ResourceEntryDto> createResourceIndex(Collection<ResourceEntryDto> entries) {
        return entries.stream()
                .collect(toMap(
                        ResourceEntryDto::getReference,
                        Function.identity(),
                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new)
                );
    }

    public static DbResourceDtoBuilder builder() {
        return new DbResourceDtoBuilder();
    }

    public Optional<ResourceEntryDto> getEntryByReference(String reference) {
        return ofNullable(entriesByReference.get(reference));
    }

    /**
     * @return localized resource entry which has been added
     */
    public ResourceEntryDto addEntryByReference(String reference) {
        checkEntryDoesNotExistWithReference(reference);

        ResourceEntryDto newResourceEntryDto = ResourceEntryDto.builder()
                .forReference(reference)
                .build();

        entriesByReference.put(reference, newResourceEntryDto);

        return newResourceEntryDto;
    }

    /**
     * @return default resource entry which has been added
     */
    public ResourceEntryDto addDefaultEntryByReference(String reference, String value) {
        checkEntryDoesNotExistWithReference(reference);

        ResourceEntryDto newResourceEntryDto = ResourceEntryDto.builder()
                .forReference(reference)
                .withGlobalItem(value)
                .build();

        entriesByReference.put(reference, newResourceEntryDto);

        return newResourceEntryDto;
    }

    public void removeEntryByReference(String reference) {
        entriesByReference.remove(reference);
    }

    private void checkEntryDoesNotExistWithReference(String reference) {
        getEntryByReference(reference)
                .ifPresent(resourceEntry -> {
                    throw new IllegalArgumentException("An ResourceEntryDto with given reference already exists: " + reference);
                });
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
    public Collection<ResourceEntryDto> getEntries() {
        return unmodifiableCollection(entriesByReference.values());
    }

    @JsonSetter("entries")
    private void setEntries(Collection<ResourceEntryDto> entries) {
        entriesByReference = createResourceIndex(entries);
    }

    public String getVersion() {
        return version;
    }

    public Integer getCategoryCount() {
        return categoryCount;
    }

    public static class DbResourceDtoBuilder {
        private String version;
        private int categoryCount;
        private final LinkedHashSet<ResourceEntryDto> entries = new LinkedHashSet<>();

        public DbResourceDtoBuilder atVersion(String version) {
            this.version = version;
            return this;
        }

        public DbResourceDtoBuilder withCategoryCount(int categoryCount) {
            this.categoryCount = categoryCount;
            return this;
        }

        public DbResourceDtoBuilder containingEntries(Collection<ResourceEntryDto> entries) {
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
}

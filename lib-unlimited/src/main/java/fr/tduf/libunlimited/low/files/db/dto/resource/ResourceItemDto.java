package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

import static fr.tduf.libunlimited.common.game.domain.Locale.ANY;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbResourceEnhancedResourceEntryDtoItem")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "value", "locale"})
public class ResourceItemDto implements Serializable {
    @JsonProperty("locale")
    private Locale locale;

    @JsonProperty("value")
    private String value;

    private ResourceItemDto() {}

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

    @JsonIgnore
    Locale getLocale() {
        return locale;
    }

    void setValue(String value) {
        this.value = value;
    }

    @JsonSetter("locale")
    private void setLocaleFromCode(String localeCode) {
        locale = Locale.fromCode(localeCode);
    }

    public static class ItemBuilder {
        private fr.tduf.libunlimited.common.game.domain.Locale locale;
        private String value;

        public ItemBuilder withLocale(fr.tduf.libunlimited.common.game.domain.Locale locale) {
            if (locale == ANY) {
                throw new IllegalArgumentException("'ANY' locale is not supported for localized item");
            }
            this.locale = locale;
            return this;
        }

        public ItemBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        ItemBuilder withGlobalValue(String value) {
            this.value = value;
            this.locale = ANY;
            return this;
        }

        public ResourceItemDto build() {
            ResourceItemDto item = new ResourceItemDto();

            item.locale = requireNonNull(locale, "Locale is required.");
            item.value = requireNonNull(value, "Value is required.");

            return item;
        }
    }

}

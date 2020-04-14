package fr.tduf.libunlimited.high.files.banks.interop.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Input object for .net cli, BANK-UX/BANK-RX commands.
 */
@JsonTypeName("genuineBatchInput")
public class GenuineBatchInputDto {
    @JsonProperty("items")
    private Set<Item> items;

    private GenuineBatchInputDto() {}

    public static GenuineBatchInputDtoBuilder builder() {
        return new GenuineBatchInputDtoBuilder();
    }

    public Set<Item> getItems() {
        return items;
    }

    public static class GenuineBatchInputDtoBuilder {
        private Set<Item> items = new HashSet<>();

        public GenuineBatchInputDtoBuilder addItems(Collection<Item> items) {
            this.items.addAll(items);
            return this;
        }

        public GenuineBatchInputDto build() {
            final GenuineBatchInputDto genuineBatchInputDto = new GenuineBatchInputDto();
            genuineBatchInputDto.items = items;
            return genuineBatchInputDto;
        }
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

    @JsonTypeName("genuineBatchItem")
    public static class Item {
        @JsonProperty("iPath")
        private String internalPath;

        @JsonProperty("eFile")
        private String externalFile;

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

        public String getExternalFile() {
            return externalFile;
        }

        public String getInternalPath() {
            return internalPath;
        }
    }

    public static class ItemBuilder {

        private String packedPath;
        private String externalFile;

        private ItemBuilder() {}

        public ItemBuilder forPackedPath(String packedPath) {
            this.packedPath = packedPath;
            return this;
        }

        public ItemBuilder withExternalFileName(String externalFile) {
            this.externalFile = externalFile;
            return this;
        }

        public Item build() {
            final Item item = new Item();
            item.internalPath = packedPath;
            item.externalFile = externalFile;
            return item;
        }
    }
}

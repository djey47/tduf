package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a brand (vehicles, tuning, clothes)
 */
public class Brand {
    private String ref;
    private Resource identifier;
    private Resource displayedName;

    private Brand() {}


    public static BrandBuilder builder() {
        return new BrandBuilder();
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

    public String getRef() {
        return ref;
    }

    public Resource getIdentifier() {
        return identifier;
    }

    public Resource getDisplayedName() {
        return displayedName;
    }

    public static class BrandBuilder {
        private String brandRef;
        private Resource brandId;
        private Resource brandName;

        public BrandBuilder withReference(String brandRef) {
            this.brandRef = brandRef;
            return this;
        }

        public BrandBuilder withIdentifier(Resource brandId) {
            this.brandId = brandId;
            return this;
        }

        public BrandBuilder withDisplayedName(Resource brandName) {
            this.brandName = brandName;
            return this;
        }

        public Brand build() {
            Brand brand = new Brand();

            brand.ref = requireNonNull(brandRef, "Brand reference is required");
            brand.identifier = brandId;
            brand.displayedName = brandName;

            return brand;
        }

    }


}

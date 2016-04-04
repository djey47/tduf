package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a (REF, VALUE) pair for a resource.
 */
public class Resource {
    private final String ref;
    private final String value;

    private Resource(String ref, String value) {
        this.ref = ref;
        this.value = value;
    }

    public static Resource from(String ref, String value) {
        return new Resource(requireNonNull(ref, "A ref is required."), requireNonNull(value, "A value is required."));
    }

    @Override
    public boolean equals(Object o) { return reflectionEquals(this, o); }

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

    public String getValue() {
        return value;
    }
}

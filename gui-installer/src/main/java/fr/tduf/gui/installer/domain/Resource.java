package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;

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

    public String getRef() {
        return ref;
    }

    public String getValue() {
        return value;
    }
}

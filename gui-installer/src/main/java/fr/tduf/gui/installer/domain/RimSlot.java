package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Domain object representing slot information for a particular rim set.
 */
public class RimSlot {
    private String ref;

    private RimSlot(String ref) {
        this.ref = requireNonNull(ref, "Slot reference is required.");
    }

    public static RimSlotBuilder builder() {
        return new RimSlotBuilder();
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

    /**
     * Creates custom RimSlot instances.
     */
    public static class RimSlotBuilder {
        private String ref;

        public RimSlotBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public RimSlot build() {
            return new RimSlot(this.ref);
        }
    }
}

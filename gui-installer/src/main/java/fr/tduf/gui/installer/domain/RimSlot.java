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

    private Resource parentDirectoryName;

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

    public Resource getParentDirectoryName() {
        return parentDirectoryName;
    }

    void setParentDirectoryName(Resource parentDirectoryName) {
        this.parentDirectoryName = parentDirectoryName;
    }

    /**
     * Creates custom RimSlot instances.
     */
    public static class RimSlotBuilder {
        private String ref;
        private Resource parentDirectoryName;

        public RimSlotBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public RimSlotBuilder withParentDirectoryName(Resource resource) {
            this.parentDirectoryName = resource;
            return this;
        }

        public RimSlot build() {
            final RimSlot rimSlot = new RimSlot(ref);

            rimSlot.setParentDirectoryName(parentDirectoryName);

            return rimSlot;
        }
    }
}

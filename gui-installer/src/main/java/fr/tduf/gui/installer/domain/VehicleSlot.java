package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Domain object representing slot information for a particular vehicle.
 */
public class VehicleSlot {
    private String ref;

    private VehicleSlot(String ref) {
        this.ref = requireNonNull(ref, "Slot reference is required.");
    }

    public static VehicleSlotBuilder builder(){
        return new VehicleSlotBuilder();
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

    /**
     * Creates custom VehicleSlot instances.
     */
    public static class VehicleSlotBuilder {
        private String ref;

        public VehicleSlotBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public VehicleSlot build() {
            final VehicleSlot vehicleSlot = new VehicleSlot(this.ref);
            return vehicleSlot;
        }
    }
}

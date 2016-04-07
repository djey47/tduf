package fr.tduf.gui.installer.domain;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Domain object representing dealer information.
 */
public class Dealer {
    private final String ref;

    private List<Slot> slots;
    private Resource displayedName;
    private String location;

    private Dealer(String ref) {
        this.ref = requireNonNull(ref, "A dealer reference is required.");
    }

    public static DealerBuilder builder() {
        return new DealerBuilder();
    }

    public String getRef() {
        return ref;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public Resource getDisplayedName() {
        return displayedName;
    }

    public int computeFreeSlotCount() {
        // TODO filter by vehicle slot REF
        return (int) slots.stream()
                .count();
    }

    @Override
    public String toString() { return reflectionToString(this); }

    public String getLocation() {
        return location;
    }

    public static class DealerBuilder {
        private String ref;
        private List<Slot> slots;
        private Resource displayedName;
        private String location;

        public DealerBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public DealerBuilder withSlots(List<Slot> slots) {
            this.slots = slots;
            return this;
        }

        public DealerBuilder withDisplayedName(Resource name) {
            this.displayedName = name;
            return this;
        }

        public DealerBuilder withLocation(String location) {
            this.location = location;
            return this;
        }

        public Dealer build() {
            final Dealer dealer = new Dealer(ref);

            dealer.slots = requireNonNull(slots, "A list of dealer slots is required.");
            dealer.displayedName = displayedName;
            dealer.location = location;

            return dealer;
        }
    }

    /**
     * Domain object representing a slot in vehicle dealer.
     */
    public static class Slot {
        private final int rank;
        private Optional<VehicleSlot> vehicleSlot;

        public Slot(int rank) {
            this.rank = rank;
        }

        public static SlotBuilder builder() {
            return new SlotBuilder();
        }

        public int getRank() {
            return rank;
        }

        public Optional<VehicleSlot> getVehicleSlot() {
            return vehicleSlot;
        }

        @Override
        public String toString() { return reflectionToString(this); }

        public static class SlotBuilder {
            private Integer rank;
            private VehicleSlot vehicleSlot;

            public SlotBuilder withRank(int rank) {
                this.rank = rank;
                return this;
            }

            public SlotBuilder havingVehicle(VehicleSlot vehicleSlot) {
                this.vehicleSlot = vehicleSlot;
                return this;
            }

            public Slot build() {
                final Slot slot = new Slot(rank);

                slot.vehicleSlot = ofNullable(vehicleSlot);

                return slot;
            }
        }
    }
}

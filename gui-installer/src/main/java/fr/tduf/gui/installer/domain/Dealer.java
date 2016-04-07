package fr.tduf.gui.installer.domain;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Domain object representing dealer information.
 */
public class Dealer {
    private final String ref;

    private List<Slot> slots;
    private Resource displayedName;

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

    public static class DealerBuilder {
        private String ref;
        private List<Slot> slots;
        private Resource displayedName;

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

        public Dealer build() {
            final Dealer dealer = new Dealer(ref);

            dealer.slots = requireNonNull(slots, "A list of dealer slots is required.");
            dealer.displayedName = displayedName;

            return dealer;
        }
    }

    /**
     * Domain object representing a slot in vehicle dealer.
     */
    public static class Slot {
        private final int rank;

        public Slot(int rank) {
            this.rank = rank;
        }

        public static SlotBuilder builder() {
            return new SlotBuilder();
        }

        public int getRank() {
            return rank;
        }

        @Override
        public String toString() { return reflectionToString(this); }

        public static class SlotBuilder {
            private Integer rank;

            public SlotBuilder withRank(int rank) {
                this.rank = rank;
                return this;
            }

            public Slot build() {
                return new Slot(rank);
            }
        }
    }
}

package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;

/**
 * Domain object representing dealer information.
 */
public class Dealer {
    private final String ref;

    private Dealer(String ref) {
        this.ref = requireNonNull(ref, "A dealer reference is required.");
    }

    public static DealerBuilder builder() {
        return new DealerBuilder();
    }

    public String getRef() {
        return ref;
    }

    public static class DealerBuilder {
        private String ref;

        public DealerBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public Dealer build() {
            return new Dealer(ref);
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

        public int getRank() {
            return rank;
        }

        public static SlotBuilder builder() {
            return new SlotBuilder();
        }

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

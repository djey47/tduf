package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;

/**
 * Domain object representing dealer information.
 */
public class Dealer {
    private String ref;

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
}

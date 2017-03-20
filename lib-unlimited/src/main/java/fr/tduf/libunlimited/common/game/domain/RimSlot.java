package fr.tduf.libunlimited.common.game.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Domain object representing slot information for a particular rim set.
 */
public class RimSlot {
    private String ref;
    private int rank;
    private Resource parentDirectoryName;
    private RimInfo frontRimInfo;
    private RimInfo rearRimInfo;
    private boolean defaultRims;

    private RimSlot(String ref, int rank) {
        this.ref = requireNonNull(ref, "Slot reference is required.");
        this.rank = requireNonNull(rank, "Rim rank is required.");
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

    public String getRef() {
        return ref;
    }

    public int getRank() {
        return rank;
    }

    public Resource getParentDirectoryName() {
        return parentDirectoryName;
    }

    public RimInfo getFrontRimInfo() {
        return frontRimInfo;
    }

    public RimInfo getRearRimInfo() {
        return rearRimInfo;
    }

    public boolean isDefault() {
        return defaultRims;
    }


    /**
     * Creates custom RimSlot instances.
     */
    public static class RimSlotBuilder {
        private String ref;
        private Integer rank;
        private Resource parentDirectoryName;
        private RimInfo frontRimInfo;
        private RimInfo rearRimInfo;
        private boolean defaultRims = false;

        public RimSlotBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public RimSlotBuilder atRank(int rank) {
            this.rank = rank;
            return this;
        }

        public RimSlotBuilder withParentDirectoryName(Resource resource) {
            this.parentDirectoryName = resource;
            return this;
        }

        public RimSlotBuilder withRimsInformation(RimInfo frontRimInfo, RimInfo rearRimInfo) {
            this.frontRimInfo = frontRimInfo;
            this.rearRimInfo = rearRimInfo;
            return this;
        }

        public RimSlotBuilder setDefaultRims(boolean defaultRims) {
            this.defaultRims = defaultRims;
            return this;
        }

        public RimSlot build() {
            final RimSlot rimSlot = new RimSlot(ref, rank);

            rimSlot.parentDirectoryName = parentDirectoryName;
            rimSlot.frontRimInfo = frontRimInfo;
            rimSlot.rearRimInfo = rearRimInfo;
            rimSlot.defaultRims = defaultRims;

            return rimSlot;
        }
    }

    /**
     * Technical information about a front/rear rim set
     */
    public static class RimInfo {
        private Resource fileName;

        public static RimInfoBuilder builder() {
            return new RimInfoBuilder();
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

        public Resource getFileName() {
            return fileName;
        }

        /**
         * Creates custom RimInfo instances.
         */
        public static class RimInfoBuilder {
            private Resource fileName;

            public RimInfoBuilder withFileName(Resource fileName) {
                this.fileName = fileName;
                return this;

            }

            public RimInfo build() {
                final RimInfo rimInfo = new RimInfo();

                rimInfo.fileName = fileName;

                return rimInfo;
            }
        }
    }
}

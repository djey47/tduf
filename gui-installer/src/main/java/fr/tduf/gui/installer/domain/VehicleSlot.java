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

    private Resource fileName;
    private RimSlot defaultRims;
    private int carIdentifier;
    private Resource brandName;
    private Resource realName;
    private Resource modelName;
    private Resource versionName;

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

    public Resource getFileName() {
        return fileName;
    }

    public RimSlot getDefaultRims() {
        return defaultRims;
    }

    public int getCarIdentifier() {
        return carIdentifier;
    }

    public Resource getRealName() {
        return realName;
    }

    public Resource getBrandName() {
        return brandName;
    }

    public Resource getModelName() {
        return modelName;
    }

    public Resource getVersionName() {
        return versionName;
    }

    /**
     * Creates custom VehicleSlot instances.
     */
    public static class VehicleSlotBuilder {
        private String ref;
        private Resource fileName;
        private RimSlot defaultRims;
        private int carIdentifier;
        private Resource realName;
        private Resource brandName;
        private Resource modelName;
        private Resource versionName;

        public VehicleSlotBuilder withRef(String ref) {
            this.ref = ref;
            return this;
        }

        public VehicleSlotBuilder withFileName(Resource fileName) {
            this.fileName = fileName;
            return this;
        }

        public VehicleSlotBuilder withRealName(Resource realName) {
            this.realName = realName;
            return this;
        }

        public VehicleSlotBuilder withBrandName(Resource brandName) {
            this.brandName = brandName;
            return this;
        }
        public VehicleSlotBuilder withModelName(Resource modelName) {
            this.modelName = modelName;
            return this;
        }
        public VehicleSlotBuilder withVersionName(Resource versionName) {
            this.versionName = versionName;
            return this;
        }

        public VehicleSlotBuilder withDefaultRims(RimSlot rims) {
            this.defaultRims = rims;
            return this;
        }

        public VehicleSlotBuilder withCarIdentifier(int id) {
            this.carIdentifier = id;
            return this;
        }

        public VehicleSlot build() {
            final VehicleSlot vehicleSlot = new VehicleSlot(this.ref);

            vehicleSlot.defaultRims = defaultRims;
            vehicleSlot.fileName = fileName;
            vehicleSlot.carIdentifier = carIdentifier;
            vehicleSlot.realName = realName;
            vehicleSlot.brandName = brandName;
            vehicleSlot.modelName = modelName;
            vehicleSlot.versionName = versionName;

            return vehicleSlot;
        }
    }
}

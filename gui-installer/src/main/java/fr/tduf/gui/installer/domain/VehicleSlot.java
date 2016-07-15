package fr.tduf.gui.installer.domain;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Domain object representing slot information for a particular vehicle.
 */
public class VehicleSlot {
    private String ref;

    private Brand brand;

    private Resource fileName;

    private Set<RimSlot> rimOptions;
    private Set<RimSlot> rimCandidates;

    private int carIdentifier;
    // TODO remove
    private Resource brandName;
    private Resource realName;
    private Resource modelName;
    private Resource versionName;
    private int cameraIdentifier;
    private SecurityOptions securityOptions;
    private List<PaintJob> paintJobs;

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

    public Optional<RimSlot> getDefaultRims() {
        return rimOptions.stream()
                .filter(RimSlot::isDefault)
                .findAny();
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

    public int getCameraIdentifier() {
        return cameraIdentifier;
    }

    public SecurityOptions getSecurityOptions() {
        return securityOptions;
    }

    public List<PaintJob> getPaintJobs() {
        return paintJobs;
    }

    public List<RimSlot> getAllRimOptionsSorted() {
        return rimOptions.stream()
                .sorted((rim1, rim2) -> Integer.compare(rim1.getRank(), rim2.getRank()))
                .collect(Collectors.toList());
    }

    public List<RimSlot> getAllRimCandidatesSorted() {
        return rimCandidates.stream()
                .sorted((rim1, rim2) -> Integer.compare(rim1.getRank(), rim2.getRank()))
                .collect(Collectors.toList());
    }

    public Optional<RimSlot> getRimAtRank(int rank) {
        Optional<RimSlot> rimFromOptions = rimOptions.stream()
                .filter(rim -> rim.getRank() == rank)
                .findAny();

        if (rimFromOptions.isPresent()) {
            return rimFromOptions;
        }

        return rimCandidates.stream()
                .filter(rim -> rim.getRank() == rank)
                .findAny();
    }

    public Brand getBrand() {
        return brand;
    }

    /**
     * Creates custom VehicleSlot instances.
     */
    public static class VehicleSlotBuilder {
        private String ref;
        private Resource fileName;
        private Set<RimSlot> rimOptions = new HashSet<>();
        private Set<RimSlot> rimCandidates = new HashSet<>();
        private int carIdentifier;
        private Resource realName;
        private Resource brandName;
        private Resource modelName;
        private Resource versionName;
        private int cameraIdentifier;
        private SecurityOptions securityOptions;
        private List<PaintJob> paintJobs = new ArrayList<>(20);
        private Brand brand;

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

        public VehicleSlotBuilder withCarIdentifier(int id) {
            this.carIdentifier = id;
            return this;
        }

        public VehicleSlotBuilder withCameraIdentifier(int id) {
            this.cameraIdentifier = id;
            return this;
        }

        public VehicleSlotBuilder withSecurityOptions(float one, int two) {
            this.securityOptions = SecurityOptions.fromValues(one, two);
            return this;
        }

        public VehicleSlotBuilder addPaintJob(PaintJob paintJob) {
            this.paintJobs.add(paintJob);
            return this;
        }

        public VehicleSlotBuilder addPaintJobs(List<PaintJob> paintJobs) {
            this.paintJobs.addAll(paintJobs);
            return this;
        }

        public VehicleSlotBuilder addRim(RimSlot rim) {
            rimOptions.add(rim);
            return this;
        }

        public VehicleSlotBuilder addRimOptions(Collection<RimSlot> rimSlots) {
            this.rimOptions.addAll(rimSlots);
            return this;
        }

        public VehicleSlotBuilder addRimCandidates(Collection<RimSlot> rimSlots) {
            this.rimCandidates.addAll(rimSlots);
            return this;
        }

        public VehicleSlotBuilder withBrand(Brand brand) {
            this.brand = brand;
            return this;
        }

        public VehicleSlot build() {
            final VehicleSlot vehicleSlot = new VehicleSlot(this.ref);

            vehicleSlot.rimOptions = rimOptions;
            vehicleSlot.rimCandidates = rimCandidates;
            vehicleSlot.fileName = fileName;
            vehicleSlot.carIdentifier = carIdentifier;
            vehicleSlot.realName = realName;
            vehicleSlot.brandName = brandName;
            vehicleSlot.modelName = modelName;
            vehicleSlot.versionName = versionName;
            vehicleSlot.cameraIdentifier = cameraIdentifier;
            vehicleSlot.securityOptions = securityOptions;
            vehicleSlot.paintJobs = paintJobs;
            vehicleSlot.brand = brand;

            return vehicleSlot;
        }
    }
}

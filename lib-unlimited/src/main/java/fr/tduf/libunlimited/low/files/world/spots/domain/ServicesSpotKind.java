package fr.tduf.libunlimited.low.files.world.spots.domain;

public enum ServicesSpotKind {
    UNKNOWN(-1),
    CAR_WASH(0),
    CAR_PAINT(1),
    REALTOR(4),
    CAR_PAINT_LUXE(9);

    private final int subCategoryIdentifier;

    ServicesSpotKind(int subCategoryIdentifier) {
        this.subCategoryIdentifier = subCategoryIdentifier;
    }

    public int getSubCategoryIdentifier() {
        return subCategoryIdentifier;
    }
}

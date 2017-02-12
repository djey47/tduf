package fr.tduf.libunlimited.low.files.world.spots.domain;

public enum ChallengesSpotKind {
    UNKNOWN(-1),
    TIME_SOLO(0),
    SPEED_SOLO(10),
    SPEED_MULTI(11),
    SPEED_CLUB(12),
    RACE_SOLO(30),
    RACE_MULTI(31),
    RACE_CLUB(32);

    private final int subCategoryIdentifier;

    ChallengesSpotKind(int subCategoryIdentifier) {
        this.subCategoryIdentifier = subCategoryIdentifier;
    }

    public int getSubCategoryIdentifier() {
        return subCategoryIdentifier;
    }
}

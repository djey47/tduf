package fr.tduf.libunlimited.low.files.world.spots.domain;

import static java.util.stream.Stream.of;

public enum SpotCategory {
    UNKNOWN(-1),
    PLAYER_HOUSE(0),    // EPH
    CAR_DEALER(1),      // ECD
    BIKE_DEALER(2),     // EBD
    AVATAR_SHOP(3),     // EAS
    AFTER_MARKET(4),    // EAM
    CLUB_HOUSE(5),      // ECH
    DINER(6),           // EDI
    CHALLENGE(7),       // RSFE, TSFE, SSFE, RMFE, SMFE, RCFE, SCFE
    SERVICE(8);         // ECW, ECP, ECR, ERE

    private int categoryIdentifier;

    SpotCategory(int categoryIdentifier) {
        this.categoryIdentifier = categoryIdentifier;
    }

    public int getCategoryIdentifier() {
        return categoryIdentifier;
    }

    public static SpotCategory fromCategoryId(int categoryIdentifier) {
        return of(SpotCategory.values())
                .filter(value -> categoryIdentifier == value.categoryIdentifier)
                .findAny()
                .orElse(UNKNOWN);
    }
}

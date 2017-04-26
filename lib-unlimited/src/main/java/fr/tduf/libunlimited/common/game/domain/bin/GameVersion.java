package fr.tduf.libunlimited.common.game.domain.bin;

/**
 * All TDU game versions
 */
public enum GameVersion {
    UNKNOWN("Unrecognized (cracked executable?)"),
    NO_GAME_BINARY("No game binary has been found. Check settings"),
    GENUINE_1_45A("Genuine, 1.45A"),
    GENUINE_1_66A("Genuine, patched to 1.66A"),
    GENUINE_1_66A_MP("Genuine, patched to 1.66A with Megapack");

    private final String label;

    GameVersion(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

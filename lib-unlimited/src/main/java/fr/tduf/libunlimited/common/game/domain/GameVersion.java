package fr.tduf.libunlimited.common.game.domain;

/**
 * All TDU game versions
 */
public enum GameVersion {
    UNKNOWN("Unrecognized version (cracked executable?)"),
    NO_GAME_BINARY("No game binary has been found. Check settings"),
    GENUINE_1_45A("Genuine version, 1.45A"),
    GENUINE_1_66A("Genuine version patched to 1.66A"),
    GENUINE_1_66A_MP("Genuine version patched to 1.66A with Megapack");

    private final String label;

    GameVersion(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

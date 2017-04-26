package fr.tduf.libunlimited.common.game.domain.bin;

/**
 * Describes all game executable statuses
 */
public enum GameStatus {
    UNKNOWN("UNKNOWN"),
    RUNNING("RUNNING"),
    OFF("CLOSED"),
    OFF_ABNORMALLY("CLOSED ABNORMALLY");

    private final String label;

    GameStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

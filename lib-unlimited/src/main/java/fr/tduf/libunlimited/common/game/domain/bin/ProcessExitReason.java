package fr.tduf.libunlimited.common.game.domain.bin;

import static java.util.stream.Stream.*;

/**
 * Registers all known exit codes
 */
public enum ProcessExitReason {
    // TODO add more codes and reasons
    SUCCESS(0, "ok"),
    KILLED_BY_USER(137, "killed by user"),
    CANCELED_SECUROM(21328, "canceled @ securom prompt"),
    UNKNOWN(-1, "unknown exit reason");

    private final int exitCode;
    private final String label;

    ProcessExitReason(int exitCode, String label) {
        this.exitCode = exitCode;
        this.label = label;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return true if it is normal termination, false otherwise
     */
    public boolean isAbnormalTermination() {
        return exitCode != 0;
    }

    /**
     * Resolves reason from given exit code
     */
    public static ProcessExitReason fromCode(int exitCode) {
        return of(values())
                .filter(v -> v.exitCode == exitCode)
                .findAny()
                .orElse(UNKNOWN);
    }
}

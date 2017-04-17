package fr.tduf.gui.common.steps;

/**
 * Describes all available GUI steps
 */
public enum StepKind {
    UNDEFINED,
    START_GAME(false);

    final GenericStep stepInstance;
    final boolean interactive;

    StepKind(GenericStep stepInstance, boolean interactive) {
        this.stepInstance = stepInstance;
        this.interactive = interactive;
    }

    StepKind(boolean interactive) {
        this(null, interactive);
    }

    StepKind() {
        this(null, false);
    }
}

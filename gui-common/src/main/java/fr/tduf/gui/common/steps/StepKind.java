package fr.tduf.gui.common.steps;

import fr.tduf.gui.common.steps.launch.StartGameStep;

/**
 * Describes all available GUI steps
 */
public enum StepKind {
    UNDEFINED,
    START_GAME(new StartGameStep(), false);

    final GenericStep stepInstance;
    final boolean interactive;

    StepKind(GenericStep stepInstance, boolean interactive) {
        this.stepInstance = stepInstance;
        this.interactive = interactive;
    }

    StepKind() {
        this(null, false);
    }
}

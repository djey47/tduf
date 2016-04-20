package fr.tduf.gui.installer.domain.exceptions;

import fr.tduf.gui.installer.steps.GenericStep;

/**
 * Represents an exception to be thrown while executing an install step.
 */
public class StepException extends Exception {
    private final GenericStep.StepType stepType;

    public StepException(GenericStep.StepType stepType, String message, Throwable cause) {
        super(message, cause);
        this.stepType = stepType;
    }

    public String getStepName() {
        return stepType.name();
    }
}

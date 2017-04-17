package fr.tduf.gui.common.domain.exceptions;


import fr.tduf.gui.common.steps.StepKind;

/**
 * Represents an exception to be thrown while executing an install step.
 */
public class StepException extends Exception {
    private final StepKind stepKind;

    public StepException(StepKind stepKind, String message, Throwable cause) {
        super(message, cause);
        this.stepKind = stepKind;
    }

    public String getStepName() {
        return stepKind.name();
    }

    public StepKind getStepKind() {
        return stepKind;
    }
}

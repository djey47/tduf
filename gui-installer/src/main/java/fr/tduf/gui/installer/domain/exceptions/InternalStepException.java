package fr.tduf.gui.installer.domain.exceptions;

import fr.tduf.gui.installer.steps.GenericStep;

/**
 * Runtime exception occuring inside of step.
 */
public class InternalStepException extends RuntimeException {
    private final GenericStep.StepType stepType;

    /**
     * Main constructore
     * @param stepType
     * @param message
     * @param cause
     */
    public InternalStepException(GenericStep.StepType stepType, String message, Throwable cause) {
        super(message, cause);
        this.stepType = stepType;
    }

    private InternalStepException() {
        stepType = null;
    }
}

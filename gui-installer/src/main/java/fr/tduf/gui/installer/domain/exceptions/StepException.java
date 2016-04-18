package fr.tduf.gui.installer.domain.exceptions;

/**
 * Represents an exception to be thrown while executing an install step.
 */
public class StepException extends Exception {
    private final String stepName;

    public StepException(String stepName, String message, Throwable cause) {
        super(message, cause);
        this.stepName = stepName;
    }

    public String getStepName() {
        return stepName;
    }
}

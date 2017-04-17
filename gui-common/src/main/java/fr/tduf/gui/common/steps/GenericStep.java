package fr.tduf.gui.common.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.gui.common.DisplayConstants.MESSAGE_STEP_KO;
import static fr.tduf.gui.common.steps.StepKind.UNDEFINED;
import static java.util.Objects.requireNonNull;

/**
 * Parent of all GUI steps
 */
public abstract class GenericStep {
    private StepKind kind;

    private ApplicationConfiguration applicationConfiguration;

    protected GenericStep() {
        kind = UNDEFINED;
    }

    private GenericStep(ApplicationConfiguration applicationConfiguration) {
        this();
        this.applicationConfiguration = applicationConfiguration;
    }

    /**
     * @param applicationConfiguration  : optional configuration
     * @return a reference of step to begin process
     */
    public static GenericStep starterStep(ApplicationConfiguration applicationConfiguration) throws StepException {
        return new GenericStep(applicationConfiguration) {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {
                // Nothing to do for now...
            }
        };
    }

    /**
     * How a particular step should initialize. Can be overriden if necessary.
     * Do not call it directly, use {@link GenericStep#nextStep(StepKind)} method instead
     */
    protected void onInit() {
        // Nothing to do for now...
    }

    /**
     * What a particular step should do.
     * Do not call it directly, use {@link GenericStep#start()} method instead
     */
    protected abstract void perform() throws IOException, ReflectiveOperationException, URISyntaxException;


    /**
     * @return a reference of step to continue process
     */
    public GenericStep nextStep(StepKind stepKind) {
        if (stepKind == null
                || stepKind.stepInstance == null) {
            throw new IllegalArgumentException("Step type not handled yet: " + stepKind);
        }

        if (stepKind.interactive) {
            throw new IllegalArgumentException("Step kind requires interactive processing and as such can't be dealt with orchestrator: " + stepKind);
        }

        stepKind.stepInstance.setKind(stepKind);

        shareContext(stepKind.stepInstance);

        stepKind.stepInstance.onInit();

        return stepKind.stepInstance;
    }

    /**
     * Triggers current step.
     */
    public GenericStep start() throws StepException {
        Log.trace(getClassName(), "->Entering step: " + kind);

        try {
            perform();
        } catch (Exception e) {
            Log.trace(getClassName(), "->Abnormally exiting step: " + kind);
            throw new StepException(kind, MESSAGE_STEP_KO, e);
        }

        Log.trace(getClassName(), "->Exiting step: " + kind);

        return this;
    }

    private void shareContext(GenericStep newStep) {
        requireNonNull(newStep, "New step is required.");

        newStep.applicationConfiguration = applicationConfiguration;
    }

    ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    private void setKind(StepKind kind) {
        this.kind = kind;
    }

    private String getClassName() {
        return this.getClass().getSimpleName();
    }
}

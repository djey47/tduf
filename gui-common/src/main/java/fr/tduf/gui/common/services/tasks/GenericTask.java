package fr.tduf.gui.common.services.tasks;

import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.steps.GenericStep;
import fr.tduf.gui.common.steps.StepKind;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.concurrent.Task;

import java.util.EnumMap;

import static fr.tduf.gui.common.steps.GenericStep.starterStep;
import static java.util.Arrays.asList;

/**
 * Parent of all coordinated tasks.
 */
public abstract class GenericTask extends Task<Void> {
    protected final ApplicationConfiguration configuration;
    protected final EnumMap<ContextKey, Object> context;

    public GenericTask(ApplicationConfiguration configuration, EnumMap<ContextKey, Object> context) {
        this.configuration = configuration;
        this.context = context;
    }

    public abstract void handleStepException(StepException se) throws StepException;

    protected void callStepChain(StepKind... steps) throws StepException {
        try {
            GenericStep currentStep = starterStep(configuration, context);
            for (StepKind stepKind : asList(steps)) {
                currentStep = currentStep.nextStep(stepKind).start();
            }
        } catch (StepException se) {
            handleStepException(se);
        }
    }
}

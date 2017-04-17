package fr.tduf.gui.common.services.tasks;

import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.steps.GenericStep;
import fr.tduf.gui.common.steps.StepKind;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;

import static fr.tduf.gui.common.steps.GenericStep.starterStep;
import static java.util.Arrays.asList;

/**
 * Parent of all coordinated tasks.
 */
public abstract class GenericTask extends Task<Void> {
    protected final ObjectProperty<ApplicationConfiguration> configuration;

    public GenericTask(ObjectProperty<ApplicationConfiguration> configuration) {
        this.configuration = configuration;
    }

    public abstract void handleStepException(StepException se) throws StepException;

    void callStepChain(StepKind... steps) throws StepException {
        try {
            GenericStep currentStep = starterStep(configuration.get());
            for (StepKind stepKind : asList(steps)) {
                currentStep = currentStep.nextStep(stepKind).start();
            }
        } catch (StepException se) {
            handleStepException(se);
        }
    }
}
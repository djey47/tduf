package fr.tduf.gui.installer.services.tasks;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;

import static java.util.Arrays.asList;

/**
 * Parent of all coordinated tasks.
 */
abstract class InstallerTask extends Task<Void> {
    protected final ObjectProperty<InstallerConfiguration> configuration;
    protected final ObjectProperty<DatabaseContext> context;

    InstallerTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
        this.configuration = configuration;
        this.context = context;
    }

    protected abstract void handleStepException(StepException se) throws StepException;

    void callStepChain(GenericStep.StepType... steps) throws StepException {
        try {
            GenericStep currentStep = GenericStep.starterStep(configuration.get(), context.get());
            for (GenericStep.StepType stepType : asList(steps)) {
                currentStep = currentStep.nextStep(stepType).start();
            }
        } catch (StepException se) {
            handleStepException(se);
        }
    }
}

package fr.tduf.gui.common.services.tasks.launch;

import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.services.tasks.GenericTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.beans.property.ObjectProperty;

/**
 * Orchestrates all steps for starting game and handles errors
 */
public class StartGameTask extends GenericTask {
    public StartGameTask(ObjectProperty<ApplicationConfiguration> configuration) {
        super(configuration);
    }

    @Override
    protected Void call() throws Exception {
        return null;
    }

    @Override
    public void handleStepException(StepException se) throws StepException {

    }
}

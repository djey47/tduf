package fr.tduf.gui.common.services.tasks.launch;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.services.tasks.GenericTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.beans.property.ObjectProperty;

/**
 * Orchestrates all steps for starting game and handles errors
 */
public class StartGameTask extends GenericTask {
    private static final String THIS_CLASS_NAME = StartGameTask.class.getSimpleName();

    public StartGameTask(ObjectProperty<ApplicationConfiguration> configuration) {
        super(configuration);
    }

    @Override
    protected Void call() throws Exception {
        Log.debug(THIS_CLASS_NAME, "TDU root dir: " + configuration.get().getGamePath());
        return null;
    }

    @Override
    public void handleStepException(StepException se) throws StepException {

    }
}

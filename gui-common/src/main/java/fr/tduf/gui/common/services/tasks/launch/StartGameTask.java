package fr.tduf.gui.common.services.tasks.launch;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.services.tasks.ContextKey;
import fr.tduf.gui.common.services.tasks.GenericTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;

import java.util.EnumMap;

import static fr.tduf.gui.common.steps.StepKind.START_GAME;

/**
 * Orchestrates all steps for starting game and handles errors
 */
public class StartGameTask extends GenericTask {
    private static final String THIS_CLASS_NAME = StartGameTask.class.getSimpleName();
    
    public StartGameTask(ApplicationConfiguration configuration, EnumMap<ContextKey, Object> context) {
        super(configuration, context);
    }

    @Override
    protected Void call() throws Exception {
        callStepChain(START_GAME);
         
        return null;
    }   

    @Override
    public void handleStepException(StepException se) throws StepException {
        Log.error(THIS_CLASS_NAME, se);
    }
}

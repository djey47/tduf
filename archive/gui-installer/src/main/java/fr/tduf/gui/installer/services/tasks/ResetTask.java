package fr.tduf.gui.installer.services.tasks;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import javafx.beans.property.ObjectProperty;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.RESTORE_SLOT;
import static fr.tduf.gui.installer.steps.GenericStep.StepType.SAVE_DATABASE;

/**
 * Orchestrates all steps for slot resetting and handles errors
 */
public class ResetTask extends InstallerTask {
    private static final String THIS_CLASS_NAME = ResetTask.class.getSimpleName();

    /**
     * Main constructor
     * @param configuration : configuration of installer application
     * @param context       : information about loaded database
     */
    public ResetTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
        super(configuration, context);
    }

    @Override
    protected Void call() throws StepException {
        Log.info(THIS_CLASS_NAME, "->Starting TDUCP slot reset");

        updateMessage(DisplayConstants.STATUS_RESET_IN_PROGRESS);

        callStepChain(
                RESTORE_SLOT,
                SAVE_DATABASE);

        updateMessage(DisplayConstants.STATUS_RESET_DONE);

        Log.info(THIS_CLASS_NAME, "->Done resetting");

        return null;
    }

    @Override
    protected void handleStepException(StepException se) throws StepException {
        Log.error(THIS_CLASS_NAME, "->Critical failure detected, no action will be intended yet.");

        updateMessage(DisplayConstants.STATUS_RESET_KO);

        Log.error(THIS_CLASS_NAME, "->Done resetting with error(s)");

        throw se;
    }
}

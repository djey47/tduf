package fr.tduf.gui.installer.services.tasks;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import javafx.beans.property.ObjectProperty;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.*;
import static fr.tduf.gui.installer.steps.GenericStep.StepType.REMOVE_BACKUP;
import static fr.tduf.gui.installer.steps.GenericStep.StepType.SAVE_DATABASE;

/**
 * Orchestrates all steps for uninstall and handles errors
 */
public class UninstallTask extends InstallerTask {
    private static final String THIS_CLASS_NAME = InstallTask.class.getSimpleName();

    public UninstallTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
        super(configuration, context);
    }

    @Override
    protected Void call() throws StepException {
        Log.info(THIS_CLASS_NAME, "->Starting uninstall");

        updateMessage(DisplayConstants.STATUS_UNINSTALL_IN_PROGRESS);

        callStepChain(
                RETRIEVE_BACKUP,
                REVERT_CAMERA,
                RESTORE_SNAPSHOT,
                RESTORE_FILES,
                SAVE_DATABASE,
                REMOVE_BACKUP);

        updateMessage(DisplayConstants.STATUS_UNINSTALL_DONE);

        Log.info(THIS_CLASS_NAME, "->Done uninstalling");

        succeeded();
        return null;
    }

    @Override
    protected void handleStepException(StepException se) throws StepException {
        Log.error(THIS_CLASS_NAME, "->Critical failure detected, no action will be intended yet.");

        updateMessage(DisplayConstants.STATUS_UNINSTALL_KO);

        Log.error(THIS_CLASS_NAME, "->Done uninstalling with error(s)");

        throw se;
    }
}

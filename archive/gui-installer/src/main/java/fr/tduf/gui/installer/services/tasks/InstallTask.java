package fr.tduf.gui.installer.services.tasks;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import javafx.beans.property.ObjectProperty;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.*;

/**
 * Orchestrates all steps for install and handles errors
 */
public class InstallTask extends InstallerTask {
    private static final String THIS_CLASS_NAME = InstallTask.class.getSimpleName();

    public InstallTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
        super(configuration, context);
    }

    @Override
    protected Void call() throws StepException {
        Log.info(THIS_CLASS_NAME, "->Starting full install");

        updateMessage(DisplayConstants.STATUS_INSTALL_IN_PROGRESS);

        callStepChain(
                INIT_BACKUP,
                BACKUP_DATABASE,
                UPDATE_DATABASE,
                SAVE_DATABASE,
                ADJUST_CAMERA,
                COPY_FILES,
                UPDATE_MAGIC_MAP);

        updateMessage(DisplayConstants.STATUS_INSTALL_DONE);

        Log.info(THIS_CLASS_NAME, "->Done installing");

        return null;
    }

    @Override
    protected void handleStepException(StepException se) throws StepException {
        switch (se.getStepType()) {
            case UPDATE_DATABASE:
            case SAVE_DATABASE:
                Log.error(THIS_CLASS_NAME, "->Critical failure detected, rollbacking database...");
                GenericStep.starterStep(configuration.get(), context.get())
                        .nextStep(RESTORE_DATABASE).start()
                        .nextStep(REMOVE_BACKUP).start();
                break;
            case ADJUST_CAMERA:
            case COPY_FILES:
            case UPDATE_MAGIC_MAP:
                Log.error(THIS_CLASS_NAME, "->Critical failure detected, rollbacking database and restoring backup files...");
                GenericStep.starterStep(configuration.get(), context.get())
                        .nextStep(RESTORE_DATABASE).start()
                        .nextStep(RESTORE_FILES).start()
                        .nextStep(REMOVE_BACKUP).start();
                break;
            default:
                Log.error(THIS_CLASS_NAME, "->Critical failure detected, no action will be intended yet.");
        }

        updateMessage(DisplayConstants.STATUS_INSTALL_KO);

        Log.error(THIS_CLASS_NAME, "->Done installing with error(s)");

        throw se;
    }
}

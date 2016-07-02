package fr.tduf.gui.installer.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.*;

/**
 * Background service to orchestrate all operations to install vehicle mod.
 */
public class StepsCoordinator extends Service<Void> {
    private static final String THIS_CLASS_NAME = StepsCoordinator.class.getSimpleName();

    private ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>();
    private ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>();
    private BooleanProperty uninstall = new SimpleBooleanProperty(false);

    @Override
    protected Task<Void> createTask() {
        return uninstall.get() ?
                new UninstallTask(configuration, context) : new InstallTask(configuration, context);
    }

    /**
     * Orchestrates all steps for install and handles errors
     */
    static class InstallTask extends InstallerTask {
        InstallTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
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

    /**
     * Orchestrates all steps for uninstall and handles errors
     */
    static class UninstallTask extends InstallerTask {
        UninstallTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
            super(configuration, context);
        }

        @Override
        protected Void call() throws StepException {
            Log.info(THIS_CLASS_NAME, "->Starting uninstall");

            updateMessage(DisplayConstants.STATUS_UNINSTALL_IN_PROGRESS);

            callStepChain(RETRIEVE_BACKUP/*, REMOVE_BACKUP*/);

            updateMessage(DisplayConstants.STATUS_UNINSTALL_DONE);

            Log.info(THIS_CLASS_NAME, "->Done uninstalling");

            succeeded();
            return null;
        }

        @Override
        protected void handleStepException(StepException se) throws StepException {
            updateMessage(DisplayConstants.STATUS_UNINSTALL_KO);

            Log.error(THIS_CLASS_NAME, "->Done uninstalling with error(s)");

            throw se;
        }
    }

    public ObjectProperty<InstallerConfiguration> configurationProperty() {
        return configuration;
    }

    public ObjectProperty<DatabaseContext> contextProperty() {
        return context;
    }

    public BooleanProperty uninstallProperty() { return uninstall; }
}

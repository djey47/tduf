package fr.tduf.gui.installer.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.*;

/**
 * Background service to orchestrate all operations to install vehicle mod.
 */
public class StepsCoordinator extends Service<String> {
    private static final String THIS_CLASS_NAME = StepsCoordinator.class.getSimpleName();

    private ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>();
    private ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>();

    @Override
    protected Task<String> createTask() {
        return new InstallTask(configuration, context);
    }

    /**
     * Orchestrates all steps for install and handles errors
     */
    static class InstallTask extends Task<String> {
        private final ObjectProperty<InstallerConfiguration> configuration;
        private final ObjectProperty<DatabaseContext> context;

        InstallTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
            this.configuration = configuration;
            this.context = context;
        }

        @Override
        protected String call() throws StepException {
            Log.info(THIS_CLASS_NAME, "->Starting full install");

            // FIXME messages not appearing...
            updateMessage(DisplayConstants.STATUS_INSTALL_IN_PROGRESS);

            try {
                GenericStep.starterStep(configuration.get(), context.get())
                        .nextStep(BACKUP_DATABASE).start()
                        .nextStep(UPDATE_DATABASE).start()
                        .nextStep(SAVE_DATABASE).start()
                        .nextStep(COPY_FILES).start()
                        .nextStep(UPDATE_MAGIC_MAP).start();
            } catch (StepException se) {
                handleStepException(se);
            }

            // FIXME messages not appearing...
            updateMessage(DisplayConstants.STATUS_INSTALL_DONE);

            Log.info(THIS_CLASS_NAME, "->Done installing");

            return "";
        }

        void handleStepException(StepException se) throws StepException {
            switch (se.getStepType()) {
                case UPDATE_DATABASE:
                case SAVE_DATABASE:
                case COPY_FILES:
                    Log.error(THIS_CLASS_NAME, "->Critical failure detected, rollbacking database...");
                    GenericStep.starterStep(configuration.get(), context.get())
                            .nextStep(RESTORE_DATABASE).start();
                    break;
                default:
            }

            // FIXME messages not appearing...
            updateMessage(DisplayConstants.STATUS_INSTALL_KO);

            Log.error(THIS_CLASS_NAME, "->Done installing with error(s)");

            throw se;
        }
    }

    public ObjectProperty<InstallerConfiguration> configurationProperty() { return configuration; }

    public ObjectProperty<DatabaseContext> contextProperty() { return context; }
}

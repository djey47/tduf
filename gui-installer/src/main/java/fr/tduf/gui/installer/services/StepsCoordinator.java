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

import java.util.List;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.*;
import static java.util.Arrays.asList;

/**
 * Background service to orchestrate all operations to install vehicle mod.
 */
public class StepsCoordinator extends Service<Void> {
    private static final String THIS_CLASS_NAME = StepsCoordinator.class.getSimpleName();

    private ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>();
    private ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>();

    @Override
    protected Task<Void> createTask() {
        return new InstallTask(configuration, context);
    }

    /**
     * Orchestrates all steps for install and handles errors
     */
    static class InstallTask extends Task<Void> {
        private final ObjectProperty<InstallerConfiguration> configuration;
        private final ObjectProperty<DatabaseContext> context;

        InstallTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
            this.configuration = configuration;
            this.context = context;
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
                    COPY_FILES,
                    UPDATE_MAGIC_MAP);

            updateMessage(DisplayConstants.STATUS_INSTALL_DONE);

            Log.info(THIS_CLASS_NAME, "->Done installing");

            return null;
        }

        void callStepChain(GenericStep.StepType... steps) throws StepException {
            updateProgress(0, steps.length );

            List<GenericStep.StepType> stepTypes = asList(steps);
            try {
                long stepCount = 0;
                GenericStep currentStep = GenericStep.starterStep(configuration.get(), context.get());
                for (GenericStep.StepType stepType : stepTypes) {
                    currentStep = currentStep.nextStep(stepType).start();

                    // FIXME progress does not update until last step performs
                    updateProgress(++stepCount, stepTypes.size());
                }
            } catch (StepException se) {
                handleStepException(se);
            }
        }

        void handleStepException(StepException se) throws StepException {
            switch (se.getStepType()) {
                case UPDATE_DATABASE:
                case SAVE_DATABASE:
                    Log.error(THIS_CLASS_NAME, "->Critical failure detected, rollbacking database...");
                    GenericStep.starterStep(configuration.get(), context.get())
                            .nextStep(RESTORE_DATABASE).start()
                            .nextStep(REMOVE_BACKUP).start();
                    break;
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

    public ObjectProperty<InstallerConfiguration> configurationProperty() {
        return configuration;
    }

    public ObjectProperty<DatabaseContext> contextProperty() {
        return context;
    }
}

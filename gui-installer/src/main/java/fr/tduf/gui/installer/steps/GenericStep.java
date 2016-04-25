package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Parent of all install steps
 */
public abstract class GenericStep {
    public enum StepType { UNDEFINED, LOAD_DATABASE, UPDATE_DATABASE, SAVE_DATABASE, BACKUP_DATABASE, RESTORE_DATABASE, UPDATE_MAGIC_MAP, LOAD_PATCH, SELECT_SLOTS, INIT_BACKUP, RESTORE_FILES, COPY_FILES }

    private StepType type;

    private InstallerConfiguration installerConfiguration;

    private DatabaseContext databaseContext;

    protected GenericStep() {
        type = StepType.UNDEFINED;
    }

    private GenericStep(InstallerConfiguration installerConfiguration, DatabaseContext databaseContext) {
        this();

        this.installerConfiguration = installerConfiguration;
        this.databaseContext = databaseContext;
    }

    /**
     * @param installerConfiguration    : optional configuration
     * @param databaseContext           : optional context
     * @return a reference of step to begin process
     */
    public static GenericStep starterStep(InstallerConfiguration installerConfiguration, DatabaseContext databaseContext) throws StepException {
        return new GenericStep(installerConfiguration, databaseContext) {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {}
        };
    }

    /**
     * What a particular step should do.
     * Do not call it directly, use {@link GenericStep#start()} method instead
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    protected abstract void perform() throws IOException, ReflectiveOperationException;

    /**
     * @return a reference of step to continue process
     */
    public GenericStep nextStep(StepType stepType) {
        final GenericStep currentStep;
        switch (stepType) {
            case INIT_BACKUP:
                currentStep = new InitBackupStep();
                break;
            case BACKUP_DATABASE:
                currentStep = new BackupDatabaseStep();
                break;
            case RESTORE_DATABASE:
                currentStep = new RestoreDatabaseStep();
                break;
            case UPDATE_DATABASE:
                currentStep = new UpdateDatabaseStep();
                break;
            case SAVE_DATABASE:
                currentStep = new SaveDatabaseStep();
                break;
            case COPY_FILES:
                currentStep = new CopyFilesStep();
                break;
            case RESTORE_FILES:
                currentStep = new GenericStep() {
                    @Override
                    protected void perform() throws IOException, ReflectiveOperationException {

                    }
                };
                break;
            case UPDATE_MAGIC_MAP:
                currentStep = new UpdateMagicMapStep();
                break;
            case LOAD_DATABASE:
            case LOAD_PATCH:
            case SELECT_SLOTS:
                throw new IllegalArgumentException("Step type requires interactive processing and as such can't be dealt with orchestrator: " + stepType);
            default:
                throw new IllegalArgumentException("Step type not handled yet: " + stepType);
        }

        currentStep.setType(stepType);

        shareContext(currentStep);

        return currentStep;
    }

    /**
     * Triggers current step.
     */
    public GenericStep start() throws StepException {
        Log.trace(getClassName(), "->Entering step: " + type);

        try {
            perform();
        } catch (Exception e) {
            Log.trace(getClassName(), "->Abnormally exiting step: " + type);
            throw new StepException(type, DisplayConstants.MESSAGE_STEP_KO, e);
        }

        Log.trace(getClassName(), "->Exiting step: " + type);

        return this;
    }

    private void shareContext(GenericStep newStep) {
        requireNonNull(newStep, "New step is required.");

        newStep.setDatabaseContext(databaseContext);
        newStep.setInstallerConfiguration(installerConfiguration);
    }

    protected DatabaseContext getDatabaseContext() {
        return databaseContext;
    }

    protected void setDatabaseContext(DatabaseContext databaseContext) {
        this.databaseContext = databaseContext;
    }

    protected InstallerConfiguration getInstallerConfiguration() {
        return installerConfiguration;
    }

    protected void setInstallerConfiguration(InstallerConfiguration installerConfiguration) {
        this.installerConfiguration = installerConfiguration;
    }

    protected void setType(StepType type) {
        this.type = type;
    }

    protected StepType getType() {
        return type;
    }

    private String getClassName() {
        return this.getClass().getSimpleName();
    }
}

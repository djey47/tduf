package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

/**
 * Parent of all install steps
 */
public abstract class GenericStep {
    /**
     * All available steps
     */
    public enum StepType {
        UNDEFINED,
        LOAD_DATABASE,
        LOAD_PATCH,
        SELECT_SLOTS,
        INIT_BACKUP(new InitBackupStep()),
        RETRIEVE_BACKUP(new RetrieveBackupStep()),
        BACKUP_DATABASE(new BackupDatabaseStep()),
        UPDATE_DATABASE(new UpdateDatabaseStep()),
        SAVE_DATABASE(new SaveDatabaseStep()),
        RESTORE_DATABASE(new RestoreDatabaseStep()),
        RESTORE_SNAPSHOT(new RestoreSnapshotStep()),
        RESTORE_SLOT( new RestoreSlotStep()),
        ADJUST_CAMERA(new AdjustCameraStep()),
        REVERT_CAMERA(new RevertCameraStep()),
        COPY_FILES(new CopyFilesStep()),
        RESTORE_FILES(new RestoreFilesStep()),
        UPDATE_MAGIC_MAP(new UpdateMagicMapStep()),
        REMOVE_BACKUP(new RemoveBackupStep());

        private final GenericStep stepInstance;

        StepType(GenericStep stepInstance) {
            this.stepInstance = stepInstance;
        }

        StepType() {
            this.stepInstance = null;
        }
    }

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
     * @param installerConfiguration : optional configuration
     * @param databaseContext        : optional context
     * @return a reference of step to begin process
     */
    public static GenericStep starterStep(InstallerConfiguration installerConfiguration, DatabaseContext databaseContext) throws StepException {
        return new GenericStep(installerConfiguration, databaseContext) {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {
                // Nothing to do for now...
            }
        };
    }

    /**
     * How a particular step should initialize. Can be overriden if necessary.
     * Do not call it directly, use {@link GenericStep#nextStep(StepType)} method instead
     */
    protected void onInit() {
        // Nothing to do for now...
    }

    /**
     * What a particular step should do.
     * Do not call it directly, use {@link GenericStep#start()} method instead
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    protected abstract void perform() throws IOException, ReflectiveOperationException, URISyntaxException;


    /**
     * @return a reference of step to continue process
     */
    public GenericStep nextStep(StepType stepType) {
        if (stepType == null
                || stepType.stepInstance == null) {
            throw new IllegalArgumentException("Step type not handled yet: " + stepType);
        }

        if (StepType.LOAD_DATABASE == stepType
                || StepType.LOAD_PATCH == stepType
                || StepType.SELECT_SLOTS == stepType) {
            throw new IllegalArgumentException("Step type requires interactive processing and as such can't be dealt with orchestrator: " + stepType);
        }

        stepType.stepInstance.setType(stepType);

        shareContext(stepType.stepInstance);

        stepType.stepInstance.onInit();

        return stepType.stepInstance;
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

        newStep.databaseContext = databaseContext;
        newStep.installerConfiguration = installerConfiguration;
    }

    DatabaseContext getDatabaseContext() {
        return databaseContext;
    }

    InstallerConfiguration getInstallerConfiguration() {
        return installerConfiguration;
    }

    private void setType(StepType type) {
        this.type = type;
    }

    protected StepType getType() {
        return type;
    }

    private String getClassName() {
        return this.getClass().getSimpleName();
    }
}

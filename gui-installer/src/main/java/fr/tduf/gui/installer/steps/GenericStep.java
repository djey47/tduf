package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Parent of all install steps
 */
public abstract class GenericStep {

    public enum StepType { LOAD_DATABASE, UPDATE_DATABASE, UPDATE_MAGIC_MAP, COPY_FILES}

    private InstallerConfiguration installerConfiguration;

    private DatabaseContext databaseContext;

    private PatchProperties patchProperties;

    protected GenericStep() { }

    private GenericStep(InstallerConfiguration installerConfiguration, DatabaseContext databaseContext, PatchProperties patchProperties) {
        this.installerConfiguration = installerConfiguration;
        this.databaseContext = databaseContext;
        this.patchProperties = patchProperties;
    }

    /**
     * @param installerConfiguration    : optional configuration
     * @param databaseContext           : optional context
     * @param patchProperties           : optional patch properties
     * @return a reference of step to begin process
     */
    public static GenericStep starterStep(InstallerConfiguration installerConfiguration, DatabaseContext databaseContext, PatchProperties patchProperties) {
        return new GenericStep(installerConfiguration, databaseContext, patchProperties ) {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {}
        };
    }

    // TODO change to instance method nextStep()
    /**
     * @return a reference of step to continue process
     */
    public static GenericStep loadStep(StepType stepType, GenericStep previousStep) {
        final GenericStep currentStep;
        switch(stepType) {
            case LOAD_DATABASE:
                currentStep = new LoadDatabaseStep();
                break;
            case UPDATE_DATABASE:
                currentStep = new UpdateDatabaseStep();
                break;
            case COPY_FILES:
                currentStep = new CopyFilesStep();
                break;
            case UPDATE_MAGIC_MAP:
                currentStep = new UpdateMagicMapStep();
                break;
            default:
                throw new IllegalArgumentException("Step type not handled yet: " + stepType.name());
        }

        shareContext(previousStep, currentStep);

        return currentStep;
    }

    // TODO return step instance to chain calls
    /**
     * What a particular step should do.
     * Do not call it directly, use {@link GenericStep#start()} method instead
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    protected abstract void perform() throws IOException, ReflectiveOperationException;

    /**
     * Triggers current step.
     */
    public void start() {
        Log.trace(getClassName(), "->Entering step");

        // TODO handle exceptions
        try {
            perform();
        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();
        }

        Log.trace(getClassName(), "->Exiting step");
    }

    private static void shareContext(GenericStep previousStep, GenericStep currentStep) {

        requireNonNull(currentStep, "Current step is required.");

        if(previousStep != null) {
            currentStep.setDatabaseContext(previousStep.databaseContext);
            currentStep.setInstallerConfiguration(previousStep.installerConfiguration);
            currentStep.setPatchProperties(previousStep.patchProperties);
        }
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

    protected PatchProperties getPatchProperties() {
        return patchProperties;
    }

    protected void setPatchProperties(PatchProperties patchProperties) {
        this.patchProperties = patchProperties;
    }

    private String getClassName() {
        return this.getClass().getSimpleName();
    }
}

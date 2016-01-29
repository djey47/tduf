package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Parent of all install steps
 */
public abstract class GenericStep {

    private InstallerConfiguration installerConfiguration;

    private DatabaseContext databaseContext;

    /**
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    // TODO integrate in common loader
    public void start() throws IOException, ReflectiveOperationException {

        Log.trace(getClassName(), "->Entering step");

        perform();

        Log.trace(getClassName(), "->Exiting step");
    }

    /**
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    protected abstract void perform() throws IOException, ReflectiveOperationException;

    public static GenericStep defaultStep(InstallerConfiguration installerConfiguration, DatabaseContext databaseContext) {
        final GenericStep genericStep = new GenericStep() {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {

            }
        };

        genericStep.setDatabaseContext(databaseContext);
        genericStep.setInstallerConfiguration(installerConfiguration);

        return genericStep;
    }

    // TODO create step enum and use only one method
    public static LoadDatabaseStep loadDatabaseStep(GenericStep previousStep) {
        final LoadDatabaseStep loadDatabaseStep = new LoadDatabaseStep();

        shareContext(previousStep, loadDatabaseStep);

        return loadDatabaseStep;
    }

    public static UpdateDatabaseStep updateDatabaseStep(GenericStep previousStep) {
        final UpdateDatabaseStep updateDatabaseStep = new UpdateDatabaseStep();

        shareContext(previousStep, updateDatabaseStep);

        return updateDatabaseStep;
    }

    public static CopyFilesStep copyFilesStep(GenericStep previousStep) {
        final CopyFilesStep copyFilesStep = new CopyFilesStep();

        shareContext(previousStep, copyFilesStep);

        return copyFilesStep;
    }

    public static UpdateMagicMapStep updateMagicMapStep(GenericStep previousStep) {
        final UpdateMagicMapStep updateMagicMapStep = new UpdateMagicMapStep();

        shareContext(previousStep, updateMagicMapStep);

        return updateMagicMapStep;
    }

    // TODO migrate to configuration
    protected static String getTduDatabaseDirectory(InstallerConfiguration configuration) {
        Path banksPath = Paths.get(getTduBanksDirectory(configuration));
        return banksPath.resolve("Database").toString();
    }

    // TODO use configuration instance
    protected static String getTduBanksDirectory(InstallerConfiguration configuration) {
        return Paths.get(configuration.getTestDriveUnlimitedDirectory(), "Euro", "Bnk").toString();
    }

    private static void shareContext(GenericStep previousStep, GenericStep currentStep) {

        requireNonNull(currentStep, "Current step is required.");

        if(previousStep != null) {
            currentStep.setDatabaseContext(previousStep.databaseContext);
            currentStep.setInstallerConfiguration(previousStep.installerConfiguration);
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

    private String getClassName() {
        return this.getClass().getSimpleName();
    }
}

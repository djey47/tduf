package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.InstallerConfiguration;

import java.io.IOException;

/**
 * Orchestrates all operations to install vehicle mod.
 */
public class StepsCoordinator {

    private static final String THIS_CLASS_NAME = StepsCoordinator.class.getSimpleName();

    /**
     * Entry point for full install
     * @param configuration : settings to install required mod.
     */
    public static void install(InstallerConfiguration configuration) throws IOException, ReflectiveOperationException {
        // TODO handle exceptions

        Log.trace(THIS_CLASS_NAME, "->Starting full install");

        // TODO create database backup to perform rollback is anything fails ?
        final LoadDatabaseStep loadDatabaseStep = GenericStep.loadDatabaseStep(null);
        try {
            loadDatabaseStep.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        final UpdateDatabaseStep updateDatabaseStep = GenericStep.updateDatabaseStep(loadDatabaseStep);
        try {
            updateDatabaseStep.start();
        } catch (IOException | ReflectiveOperationException ioe) {
            ioe.printStackTrace();
        }

        final CopyFilesStep copyFilesStep = GenericStep.copyFilesStep(updateDatabaseStep);
        try {
            copyFilesStep.start();
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        final UpdateMagicMapStep updateMagicMapStep = GenericStep.updateMagicMapStep(copyFilesStep);
        try {
            updateMagicMapStep.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

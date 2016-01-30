package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.InstallerConfiguration;

import java.io.IOException;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.LOAD_DATABASE;
import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_MAGIC_MAP;

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
        Log.trace(THIS_CLASS_NAME, "->Starting full install");

        final GenericStep starterStep = GenericStep.starterStep(configuration, null, null);

        // TODO create database backup to perform rollback is anything fails ?
        final GenericStep loadDatabaseStep = GenericStep.loadStep(LOAD_DATABASE, starterStep);
        loadDatabaseStep.start();

        final GenericStep updateDatabaseStep = GenericStep.loadStep(UPDATE_DATABASE, loadDatabaseStep);
        updateDatabaseStep.start();

        final GenericStep copyFilesStep = GenericStep.loadStep(GenericStep.StepType.COPY_FILES, updateDatabaseStep);
        copyFilesStep.start();

        final GenericStep updateMagicMapStep = GenericStep.loadStep(UPDATE_MAGIC_MAP, copyFilesStep);
        updateMagicMapStep.start();
    }
}

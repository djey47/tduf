package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.InstallerConfiguration;

import java.io.IOException;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.*;

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

        // TODO create database backup to perform rollback is anything fails ?
        GenericStep.starterStep(configuration, null, null)
                .nextStep(LOAD_DATABASE).start()
                .nextStep(UPDATE_DATABASE).start()
                .nextStep(COPY_FILES).start()
                .nextStep(UPDATE_MAGIC_MAP).start();
    }
}

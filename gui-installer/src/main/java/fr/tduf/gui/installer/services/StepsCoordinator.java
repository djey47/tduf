package fr.tduf.gui.installer.services;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
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

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                Log.trace(THIS_CLASS_NAME, "->Starting full install");

                // TODO create database backup to perform rollback is anything fails ?
                GenericStep.starterStep(configuration.get(), null, null)
                        .nextStep(LOAD_DATABASE).start()
                        .nextStep(UPDATE_DATABASE).start()
                        .nextStep(COPY_FILES).start()
                        .nextStep(UPDATE_MAGIC_MAP).start();

                return "";
            }
        };
    }

    public ObjectProperty<InstallerConfiguration> configurationProperty() { return configuration; }
}

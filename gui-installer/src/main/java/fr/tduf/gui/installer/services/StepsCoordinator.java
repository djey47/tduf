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
        return new Task<String>() {
            @Override
            protected String call() throws StepException {
                Log.trace(THIS_CLASS_NAME, "->Starting full install");

                // FIXME messages not appearing...
                updateMessage(DisplayConstants.STATUS_INSTALL_IN_PROGRESS);

                // TODO create database backup to perform rollback is anything fails ?
                GenericStep.starterStep(configuration.get(), context.get())
                        .nextStep(UPDATE_DATABASE).start()
                        .nextStep(SAVE_DATABASE).start()
                        .nextStep(COPY_FILES).start()
                        .nextStep(UPDATE_MAGIC_MAP).start();

                // FIXME messages not appearing...
                updateMessage(DisplayConstants.STATUS_INSTALL_DONE);

                return "";
            }
        };
    }

    public ObjectProperty<InstallerConfiguration> configurationProperty() { return configuration; }

    public ObjectProperty<DatabaseContext> contextProperty() { return context; }
}

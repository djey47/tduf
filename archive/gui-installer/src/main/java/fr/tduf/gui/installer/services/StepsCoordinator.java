package fr.tduf.gui.installer.services;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.services.tasks.InstallTask;
import fr.tduf.gui.installer.services.tasks.ResetTask;
import fr.tduf.gui.installer.services.tasks.TaskType;
import fr.tduf.gui.installer.services.tasks.UninstallTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Background service to orchestrate all non-interactive operations on vehicle mod.
 */
public class StepsCoordinator extends Service<Void> {
    private ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>();
    private ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>();
    private ObjectProperty<TaskType> taskType = new SimpleObjectProperty<>(TaskType.INSTALL);

    @Override
    protected Task<Void> createTask() {
        final TaskType task = taskType.getValue();
        switch (task) {
            case INSTALL:
                return new InstallTask(configuration, context);
            case UNINSTALL:
                return new UninstallTask(configuration, context);
            case RESET_SLOT:
                return new ResetTask(configuration, context);
            default:
                throw new IllegalArgumentException("Task type not handled yet: " + task);
        }
    }

    public ObjectProperty<InstallerConfiguration> configurationProperty() {
        return configuration;
    }

    public ObjectProperty<DatabaseContext> contextProperty() {
        return context;
    }

    public ObjectProperty<TaskType> taskTypeProperty() { return taskType; }
}

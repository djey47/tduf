package fr.tduf.gui.launcher.services;

import fr.tduf.gui.common.services.tasks.launch.StartGameTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Background service to orchestrate all non-interactive operations on vehicle mod.
 */
public class LauncherStepsCoordinator extends Service<Void> {
    private final ObjectProperty<ApplicationConfiguration> configuration = new SimpleObjectProperty<>();

    @Override
    protected Task<Void> createTask() {
        return new StartGameTask(configuration);
    }

    public ObjectProperty<ApplicationConfiguration> configurationProperty() {
        return configuration;
    }
}

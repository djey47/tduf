package fr.tduf.gui.launcher.services;

import fr.tduf.gui.common.services.tasks.ContextKey;
import fr.tduf.gui.common.services.tasks.launch.StartGameTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.EnumMap;

import static fr.tduf.gui.common.services.tasks.ContextKey.GAME_PROCESS;

/**
 * Background service to orchestrate all non-interactive operations on vehicle mod.
 */
public class LauncherStepsCoordinator extends Service<Void> {
    private final Property<ApplicationConfiguration> configuration = new SimpleObjectProperty<>();
    private final EnumMap<ContextKey, Object> context = new EnumMap<>(ContextKey.class);

    @Override
    protected Task<Void> createTask() {
        StartGameTask startGameTask = new StartGameTask(configuration.getValue(), context);
        return startGameTask;
    }

    public Property<ApplicationConfiguration> configurationProperty() {
        return configuration;
    }

    public Property<Process> gameProcessProperty() {
        return (Property<Process>) context.get(GAME_PROCESS);
    }
}

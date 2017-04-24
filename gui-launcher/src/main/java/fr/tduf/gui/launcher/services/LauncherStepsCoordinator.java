package fr.tduf.gui.launcher.services;

import fr.tduf.gui.common.services.tasks.ContextKey;
import fr.tduf.gui.common.services.tasks.launch.StartGameTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.GameStatus;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.EnumMap;

import static fr.tduf.gui.common.services.tasks.ContextKey.GAME_PROCESS;
import static fr.tduf.gui.common.services.tasks.ContextKey.PROCESS_STATUS;
import static fr.tduf.libunlimited.common.game.domain.GameStatus.OFF;

/**
 * Background service to orchestrate all non-interactive operations on vehicle mod.
 */
public class LauncherStepsCoordinator extends Service<Void> {
    private final Property<ApplicationConfiguration> configuration = new SimpleObjectProperty<>();
    private final Property<GameStatus> processStatus = new SimpleObjectProperty<>(OFF);
    private final Property<Process> gameProcess = new SimpleObjectProperty<>();

    private final EnumMap<ContextKey, Object> context = new EnumMap<>(ContextKey.class);

    @Override
    protected Task<Void> createTask() {
        fillContext();

        return new StartGameTask(configuration.getValue(), context);
    }

    private void fillContext() {
        context.put(GAME_PROCESS, gameProcess);
        context.put(PROCESS_STATUS, processStatus);
    }

    public Property<ApplicationConfiguration> configurationProperty() {
        return configuration;
    }

    public Property<GameStatus> processStatusProperty() { return processStatus; }
}

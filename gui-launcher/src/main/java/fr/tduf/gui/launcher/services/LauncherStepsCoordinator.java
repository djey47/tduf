package fr.tduf.gui.launcher.services;

import fr.tduf.gui.common.services.tasks.ContextKey;
import fr.tduf.gui.common.services.tasks.launch.StartGameTask;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.bin.GameStatus;
import fr.tduf.libunlimited.common.game.domain.bin.LaunchSwitch;
import fr.tduf.libunlimited.common.game.domain.bin.ProcessExitReason;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import static fr.tduf.gui.common.services.tasks.ContextKey.*;
import static fr.tduf.libunlimited.common.game.domain.bin.GameStatus.OFF;

/**
 * Background service to orchestrate all non-interactive operations on vehicle mod.
 */
public class LauncherStepsCoordinator extends Service<Void> {
    private final Property<ApplicationConfiguration> configuration = new SimpleObjectProperty<>();
    private final Property<GameStatus> processStatus = new SimpleObjectProperty<>(OFF);
    private final Property<Process> gameProcess = new SimpleObjectProperty<>();
    private final Property<ProcessExitReason> processExitReason = new SimpleObjectProperty<>(ProcessExitReason.UNKNOWN);
    private final Property<Set<LaunchSwitch>> gameSwitches = new SimpleObjectProperty<>();
    
    private final EnumMap<ContextKey, Object> context = new EnumMap<>(ContextKey.class);

    @Override
    protected Task<Void> createTask() {
        fillContext();

        return new StartGameTask(configuration.getValue(), context);
    }

    private void fillContext() {
        context.put(GAME_PROCESS, gameProcess);
        context.put(PROCESS_STATUS, processStatus);
        context.put(PROCESS_EXIT_REASON, processExitReason);
        context.put(GAME_SWITCHES, gameSwitches);
    }

    public Property<ApplicationConfiguration> configurationProperty() {
        return configuration;
    }

    public Property<GameStatus> processStatusProperty() { return processStatus; }

    public Property<Process> gameProcessProperty() { return gameProcess; }
    
    public Property<ProcessExitReason> processExitReasonProperty() { return processExitReason; }

    public Property<Set<LaunchSwitch>> gameSwitchesProperty() {
        return gameSwitches;
    }
}

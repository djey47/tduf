package fr.tduf.gui.common.steps.launch;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.steps.GenericStep;
import fr.tduf.libunlimited.common.game.domain.bin.GameStatus;
import javafx.application.Platform;
import javafx.beans.property.Property;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.gui.common.services.tasks.ContextKey.GAME_PROCESS;
import static fr.tduf.gui.common.services.tasks.ContextKey.PROCESS_STATUS;
import static fr.tduf.libunlimited.common.game.FileConstants.FILE_GAME_EXECUTABLE;
import static fr.tduf.libunlimited.common.game.domain.bin.GameStatus.*;

// TODO unit test
public class StartGameStep extends GenericStep {
    private static final String THIS_CLASS_NAME = StartGameStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {

        Log.debug(THIS_CLASS_NAME, "TDU root dir: " + getApplicationConfiguration().getGamePath());

        Property<Process> gameProcess = (Property<Process>) getContext().get(GAME_PROCESS);
        Property<GameStatus> processStatus = (Property<GameStatus>) getContext().get(PROCESS_STATUS);
        
        int exitCode = -1;
        try {
            File gamePath = resolveAndCheckGamePath();
            gameProcess.setValue(new ProcessBuilder()
                    .directory(gamePath)
                    .command(buildFullCommand(gamePath))
                    .start());

            Log.debug(THIS_CLASS_NAME, "Game process started: " + gameProcess.getValue());
            
            Platform.runLater(() -> processStatus.setValue(RUNNING));
            
            exitCode = gameProcess.getValue().waitFor();
            
            Log.debug(THIS_CLASS_NAME, "Game process ended with exit code: " + exitCode);
        } catch(InterruptedException ie) {
            throw new IOException("Game process was interrupted", ie);
        } catch (Exception e) {
            throw new IOException("Unable to start game process", e);
        } finally {
            GameStatus newStatus = exitCode == 0 ? OFF : OFF_ABNORMALLY;
            updateContext(newStatus, gameProcess, processStatus);
        }
    }

    private void updateContext(GameStatus status, Property<Process> gameProcess, Property<GameStatus> processStatus) {
        Platform.runLater(() -> {
            gameProcess.setValue(null);
            processStatus.setValue(status);  
        }); 
    }

    private File resolveAndCheckGamePath() throws StepException {
        File gamePath = getApplicationConfiguration().getGamePath()
                .map(Path::toFile)
                .orElseThrow(() -> new IllegalStateException("Game path has not been set"));

        if (!gamePath.exists()) {
            throw new IllegalArgumentException("Game path does not exist: " + gamePath);
        }

        return gamePath;
    }

    private String buildFullCommand(File gamePath) {
        Path executablePath = Paths.get(gamePath.getAbsolutePath(), FILE_GAME_EXECUTABLE);

        if (!executablePath.toFile().exists()) {
            throw new IllegalArgumentException("Game executable path does not exist: " + executablePath);
        }

        return executablePath.toString();
    }
}

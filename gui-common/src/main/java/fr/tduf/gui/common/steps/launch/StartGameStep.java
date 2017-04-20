package fr.tduf.gui.common.steps.launch;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.gui.common.steps.GenericStep;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.gui.common.services.tasks.ContextKey.GAME_PROCESS;
import static fr.tduf.libunlimited.common.game.FileConstants.FILE_GAME_EXECUTABLE;

public class StartGameStep extends GenericStep {
    private static final String THIS_CLASS_NAME = StartGameStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {

        Log.debug(THIS_CLASS_NAME, "TDU root dir: " + getApplicationConfiguration().getGamePath());

        Property<Process> gameProcess = new SimpleObjectProperty<>();
        getContext().put(GAME_PROCESS, gameProcess);

        try {
            File gamePath = resolveAndCheckGamePath();
            gameProcess.setValue(new ProcessBuilder()
                    .directory(gamePath)
                    .command(buildFullCommand(gamePath))
                    .start());

            Log.debug(THIS_CLASS_NAME, "Game process started: " + gameProcess.getValue());

            int exitCode = gameProcess.getValue().waitFor();
            Log.debug(THIS_CLASS_NAME, "Game process ended with exit code: " + exitCode);
        } catch(InterruptedException ie) {
            throw new IOException("Game process was interrupted", ie);
        } catch (Exception e) {
            throw new IOException("Unable to start game process", e);
        } finally {
            gameProcess.setValue(null);
        }
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

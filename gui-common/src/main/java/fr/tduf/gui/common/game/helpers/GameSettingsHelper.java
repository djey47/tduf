package fr.tduf.gui.common.game.helpers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static fr.tduf.gui.common.DisplayConstants.TITLE_BROWSE_GAME_DIRECTORY;
import static java.util.Optional.ofNullable;

/**
 * Provides misc. game settings
 */
public class GameSettingsHelper {
    private static final String THIS_CLASS_NAME = GameSettingsHelper.class.getSimpleName();

    /**
     * @param configuration : application configuration to be updated with selection
     * @param parent        : parent window, can be null
     * @return selected game root directory, or empty if no selection has been made
     */
    public static String askForGameLocationAndUpdateConfiguration(ApplicationConfiguration configuration, Window parent) {
        return browseForGameDirectory(parent)
                .map(gameLocation -> {
                    try {
                        configuration.setGamePath(gameLocation);
                        configuration.store();
                    } catch (IOException ioe) {
                        Log.warn(THIS_CLASS_NAME, "Unable to save application configuration", ioe);
                    }
                    return gameLocation;
                })
                .orElse("");
    }

    private static Optional<String> browseForGameDirectory(Window parent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(TITLE_BROWSE_GAME_DIRECTORY);

        return ofNullable(directoryChooser.showDialog(parent))
                .map(File::getPath);
    }
}

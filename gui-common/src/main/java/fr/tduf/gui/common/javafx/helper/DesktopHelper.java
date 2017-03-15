package fr.tduf.gui.common.javafx.helper;

import com.esotericsoftware.minlog.Log;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * Provides low level services to interop with desktop platform through AWT in JavaFX
 */
public class DesktopHelper {
    private static final String THIS_CLASS_NAME = DesktopHelper.class.getSimpleName();
    
    /**
     * @see Desktop#open(File)
     * @param path  : file or folder to be displayed in default file manager
     */
    public static void openInFiles(Path path) {
        requireNonNull(path, "Path to open is required");

        new Thread(() -> {
            try {
                Desktop.getDesktop().open(path.toFile());
            } catch (IOException | IllegalArgumentException e) {
                Log.error(THIS_CLASS_NAME, "Unable to display file system location: " + path, e);
            }
        }).start();        
    }    
    
    /**
     * @see Desktop#browse(URI)
     * @param address   : address to be displayed in default web browser
     */
    public static void openInBrowser(String address) {
        requireNonNull(address, "Address to open is required");

        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI(address));
            } catch (IOException | IllegalArgumentException | URISyntaxException e) {
                Log.error(THIS_CLASS_NAME, "Unable to browse address: " + address, e);
            }
        }).start();        
    }
}

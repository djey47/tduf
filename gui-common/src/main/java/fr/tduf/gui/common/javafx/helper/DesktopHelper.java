package fr.tduf.gui.common.javafx.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiApp;

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
                checkForLegacyDesktopSupport();
                Desktop.getDesktop().open(path.toFile());
            } catch (IOException | IllegalArgumentException | UnsupportedOperationException e) {
                Log.error(THIS_CLASS_NAME, "Unable to display file system location: " + path, e);
            }
        }).start();        
    }

    /**
     * @param address   : address to be displayed in default web browser
     */
    public static void openInBrowser(String address) {
        requireNonNull(address, "Address to open is required");
        
        try {
            AbstractGuiApp.getInstance().getHostServicesInstance().showDocument(address);
        } catch (NullPointerException npe) {
            // Workaround for OpenJDK, falling back to AWT's Desktop...
            Log.warn(THIS_CLASS_NAME, "Host services are not supported, falling back to AWT Desktop services...");
            DesktopHelper.openInBrowserLegacy(address);
        }        
    }

    /**
     * @see Desktop#browse(URI)
     */
    private static void openInBrowserLegacy(String address) {
        new Thread(() -> {
            try {
                checkForLegacyDesktopSupport();
                Desktop.getDesktop().browse(new URI(address));
            } catch (IOException | IllegalArgumentException | URISyntaxException | UnsupportedOperationException e) {
                Log.error(THIS_CLASS_NAME, "Unable to browse address: " + address, e);
            }
        }).start();        
    }

    private static void checkForLegacyDesktopSupport() {
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Java AWT Desktop services are not supported :(");
        }
    }
}

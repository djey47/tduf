package fr.tduf.gui.common.helper.javafx;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

/**
 * Helper class to factorize handling of common dialog boxes.
 */
public class CommonDialogsHelper {

    /**
     * Displays a system dialog to browse for file name or existing file
     * @param loadFile  : true to use as file chooser, else to use as target selector
     * @return chosen file, or empty if no selection has been made (dismissed).
     */
    public static Optional<File> browseForFilename(boolean loadFile, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();

        File selectedFile;
        if (loadFile) {
            selectedFile = fileChooser.showOpenDialog(ownerWindow);
        } else {
            selectedFile = fileChooser.showSaveDialog(ownerWindow);
        }

        return Optional.ofNullable(selectedFile);
    }

    /**
     * Displays a single error dialog box.
     * @param alertType         : type of dialog box to be created
     * @param title             : text in upper dialog bar
     * @param errorMessage      : short text
     * @param errorDescription  : details.
     */
    public static void showDialog(Alert.AlertType alertType, String title, String errorMessage, String errorDescription) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(errorMessage);
        alert.setContentText(errorDescription);

        alert.showAndWait();
    }
}
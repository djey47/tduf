package fr.tduf.gui.common.javafx.helper;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;

/**
 * Helper class to factorize handling of common dialog boxes.
 */
public class CommonDialogsHelper {

    private static final String LABEL_BUTTON_OK = "OK";
    private static final String LABEL_BUTTON_CANCEL = "Cancel";

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
     * Displays a single dialog box for different purposes.
     * @param alertType    : type of dialog box to be created
     * @param title        : text in upper dialog bar
     * @param message      : short text
     * @param description  : details.
     */
    public static void showDialog(Alert.AlertType alertType, String title, String message, String description) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(description);
        alert.setResizable(true);

        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node))
                .forEach(label -> label.setMinHeight(Region.USE_PREF_SIZE));

        alert.showAndWait();
    }

    /**
     * Display a dialog box requesting user for a value.
     * @param title : text in upper dialog bar
     * @param label : label for value text field
     * @return resulting value, or absent if dialog was dismissed.
     */
    public static Optional<String> showInputValueDialog(String title, String label) {
        Dialog<String> inputValueDialog = new Dialog<>();
        inputValueDialog.setTitle(title);
        inputValueDialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField valueTextField = new TextField();
        Platform.runLater(valueTextField::requestFocus);

        grid.add(new Label(label), 0, 0);
        grid.add(valueTextField, 1, 0);
        inputValueDialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType(LABEL_BUTTON_OK, OK_DONE);
        ButtonType cancelButtonType = new ButtonType(LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        inputValueDialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);

        inputValueDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return valueTextField.getText();
            }
            return null;
        });

        return inputValueDialog.showAndWait();
    }
}

package fr.tduf.gui.common.javafx.helper;

import fr.tduf.gui.common.javafx.helper.options.FileBrowsingOptions;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

import static fr.tduf.gui.common.DisplayConstants.LABEL_BUTTON_CANCEL;
import static fr.tduf.gui.common.DisplayConstants.LABEL_BUTTON_OK;
import static java.util.Optional.ofNullable;
import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;

/**
 * Helper class to factorize handling of common dialog boxes.
 */
public class CommonDialogsHelper {

    private static FileChooser fileChooser = new FileChooser();

    /**
     * Displays a system dialog to browse for file name or existing file
     *
     * @param fileBrowsingOptions   : specifies appearance and behaviour of component
     * @param parent                : owner dialog, or null to be a top-level one
     * @return chosen file, or empty if no selection has been made (dismissed).
     */
    public static Optional<File> browseForFilename(FileBrowsingOptions fileBrowsingOptions, Window parent) {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().addAll(fileBrowsingOptions.getExtensionFilters());

        fileChooser.setInitialFileName("");
        fileChooser.setInitialDirectory(new File(fileBrowsingOptions.getInitialDirectory()));
        fileChooser.setTitle(fileBrowsingOptions.getDialogTitle());

        final File selectedFile;
        if (FileBrowsingOptions.FileBrowsingContext.LOAD == fileBrowsingOptions.getContext()) {
            selectedFile = fileChooser.showOpenDialog(parent);
        } else {
            fileBrowsingOptions.getExtensionFilters().stream()
                    .findFirst()
                    .flatMap(extensionFilter -> extensionFilter.getExtensions().stream().findFirst())
                    .ifPresent(fileChooser::setInitialFileName);

             selectedFile = fileChooser.showSaveDialog(parent);
        }

        return ofNullable(selectedFile);
    }

    /**
     * Displays a single alert dialog box for different purposes.
     * @param dialogOptions : specifies appearance and behaviour of component
     * @param parent        : owner dialog, or null to be a top-level one
     */
    public static void showDialog(SimpleDialogOptions dialogOptions, Window parent) {
        Alert alert = new Alert(dialogOptions.getAlertContext());
        alert.initOwner(parent);
        alert.setTitle(dialogOptions.getTitle());
        alert.setHeaderText(dialogOptions.getMessage());
        alert.setContentText(dialogOptions.getDescription());
        alert.setResizable(true);

        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node))
                .forEach(label -> label.setMinHeight(Region.USE_PREF_SIZE));

        alert.showAndWait();
    }

    /**
     * Display a dialog box requesting user for a value.
     * @param title     : text in upper dialog bar
     * @param label     : label for value text field
     * @param parent    : owner dialog, or null to be a top-level one
     * @return resulting value, or absent if dialog was dismissed.
     */
    public static Optional<String> showInputValueDialog(String title, String label, Window parent) {
        // TODO use FX's TextInputDialog
        Dialog<String> inputValueDialog = new Dialog<>();
        inputValueDialog.initOwner(parent);
        inputValueDialog.setTitle(title);
        inputValueDialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        // TODO Normalize padding
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

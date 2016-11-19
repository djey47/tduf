package fr.tduf.gui.common.javafx.helper;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.common.DisplayConstants.LABEL_BUTTON_CANCEL;
import static fr.tduf.gui.common.DisplayConstants.LABEL_BUTTON_OK;
import static java.util.Optional.ofNullable;
import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;

/**
 * Helper class to factorize handling of common dialog boxes.
 */
// TODO add options for alert dialog
public class CommonDialogsHelper {

    private static FileChooser fileChooser = new FileChooser();

    /**
     * Displays a system dialog to browse for file name or existing file
     *
     * @param fileBrowsingOptions   : specifies appearance and behaviour of component
     * @param parent       : owner dialog, or null to be a top-level one
     * @return chosen file, or empty if no selection has been made (dismissed).
     */
    public static Optional<File> browseForFilename(FileBrowsingOptions fileBrowsingOptions, Window parent) {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().addAll(fileBrowsingOptions.extensionFilters);

        fileChooser.setInitialFileName("");
        fileChooser.setInitialDirectory(new File(fileBrowsingOptions.initialDirectory));
        fileChooser.setTitle(fileBrowsingOptions.dialogTitle);

        final File selectedFile;
        if (FileBrowsingOptions.FileBrowsingContext.LOAD == fileBrowsingOptions.context) {
            selectedFile = fileChooser.showOpenDialog(parent);
        } else {
            fileBrowsingOptions.extensionFilters.stream()
                    .findFirst()
                    .flatMap(extensionFilter -> extensionFilter.getExtensions().stream().findFirst())
                    .ifPresent(fileChooser::setInitialFileName);

             selectedFile = fileChooser.showSaveDialog(parent);
        }

        return ofNullable(selectedFile);
    }

    /**
     * Displays a single alert dialog box for different purposes.
     * @param alertType    : type of dialog box to be created
     * @param title        : text in upper dialog bar
     * @param message      : short text
     * @param description  : details
     * @param parent       : owner dialog, or null to be a top-level one
     */
    public static void showDialog(Alert.AlertType alertType, String title, String message, String description, Window parent) {
        Alert alert = new Alert(alertType);
        alert.initOwner(parent);
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
     * @param title     : text in upper dialog bar
     * @param label     : label for value text field
     * @param parent    : owner dialog, or null to be a top-level one
     * @return resulting value, or absent if dialog was dismissed.
     */
    public static Optional<String> showInputValueDialog(String title, String label, Window parent) {
        Dialog<String> inputValueDialog = new Dialog<>();
        inputValueDialog.initOwner(parent);
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

    /**
     * Specifies file selector appearance and behaviour.
     */
    public static class FileBrowsingOptions {

        public static final FileBrowsingOptions defaultSettings = new FileBrowsingOptions();

        private FileBrowsingOptions() {}

        enum FileBrowsingContext { LOAD, SAVE }

        final List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
        String dialogTitle = "";
        FileBrowsingContext context = FileBrowsingContext.LOAD;
        String initialDirectory = ".";

        /**
         * @return instance creator
         */
        public static FileBrowsingOptionsBuilder builder() {
            return new FileBrowsingOptionsBuilder();
        }


        public static class FileBrowsingOptionsBuilder extends FileBrowsingOptions {
            public FileBrowsingOptionsBuilder forLoading() {
                context = FileBrowsingContext.LOAD;
                return this;
            }

            public FileBrowsingOptionsBuilder forSaving() {
                context = FileBrowsingContext.SAVE;
                return this;
            }

            public FileBrowsingOptionsBuilder withDialogTitle(String title) {
                dialogTitle = title;
                return this;
            }

            public FileBrowsingOptionsBuilder withExtensionFilters(List<FileChooser.ExtensionFilter> extensionFilters) {
                this.extensionFilters.clear();
                this.extensionFilters.addAll(extensionFilters);
                return this;
            }

            public FileBrowsingOptionsBuilder withInitialDirectory(String initialDirectory) {
                this.initialDirectory = initialDirectory;
                return this;
            }

            public FileBrowsingOptions build() {
                FileBrowsingOptions fileBrowsingOptions = new FileBrowsingOptions();

                fileBrowsingOptions.context = context;
                fileBrowsingOptions.extensionFilters.addAll(extensionFilters);
                fileBrowsingOptions.dialogTitle = dialogTitle;
                fileBrowsingOptions.initialDirectory = initialDirectory;

                return fileBrowsingOptions;
            }
        }
    }
}

package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;

/**
 * Helper class to build and display dialog boxes.
 */
public class DialogsHelper {

    /**
     * @return true if all locales should be affected, false otherwise - or absent if dialog was dismissed.
     */
    public Optional<Boolean> showResourceDeletionDialog(RemoteResource resource, String localeCode) {
        Alert alert = new Alert(CONFIRMATION);
        alert.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        alert.setHeaderText(resource.toDisplayableValue());
        alert.setContentText(DisplayConstants.MESSAGE_DELETED_RESOURCE + "\n" + DisplayConstants.QUESTION_AFFECTED_LOCALES);

        ButtonType currentLocaleButtonType = new ButtonType(String.format(DisplayConstants.LABEL_BUTTON_CURRENT_LOCALE, localeCode));
        ButtonType allLocalesButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_ALL);
        ButtonType cancelButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        alert.getButtonTypes().setAll(currentLocaleButtonType, allLocalesButtonType, cancelButtonType);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() == cancelButtonType) {
            return Optional.empty();
        }

        return Optional.of(result.get() == allLocalesButtonType);
    }

    public Optional<Pair<String, String>> showEditResourceDialog(RemoteResource selectedResource, DbDto.Topic topic) {
        Dialog<Pair<String, String>> editResourceDialog = new Dialog<>();
        editResourceDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        editResourceDialog.setHeaderText(String.format(DisplayConstants.MESSAGE_EDITED_RESOURCE,
                topic.getLabel(),
                selectedResource.toDisplayableValue()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField referenceTextField = new TextField(selectedResource.referenceProperty().get());
        Platform.runLater(referenceTextField::requestFocus);
        TextField valueTextField = new TextField(selectedResource.valueProperty().get());
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_REFERENCE), 0, 0);
        grid.add(referenceTextField, 1, 0);
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_VALUE), 0, 1);
        grid.add(valueTextField, 1, 1);
        editResourceDialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_OK, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        editResourceDialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);

        editResourceDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(referenceTextField.getText(), valueTextField.getText());
            }
            return null;
        });

        return editResourceDialog.showAndWait();
    }

    // TODO factorize add and edit dialogs
    public Optional<Pair<String, String>> showAddResourceDialog(DbDto.Topic topic) {
        Dialog<Pair<String, String>> addResourceDialog = new Dialog<>();
        addResourceDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        addResourceDialog.setHeaderText(DisplayConstants.MESSAGE_ADDED_RESOURCE + topic.getLabel());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        // TODO generate default reference?
        TextField referenceTextField = new TextField();
        Platform.runLater(referenceTextField::requestFocus);
        TextField valueTextField = new TextField();
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_REFERENCE), 0, 0);
        grid.add(referenceTextField, 1, 0);
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_VALUE), 0, 1);
        grid.add(valueTextField, 1, 1);
        addResourceDialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_OK, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        addResourceDialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);

        addResourceDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(referenceTextField.getText(), valueTextField.getText());
            }
            return null;
        });

        return addResourceDialog.showAndWait();
    }
}
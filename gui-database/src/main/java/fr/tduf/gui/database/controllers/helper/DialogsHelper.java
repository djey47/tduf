package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.DatabaseEntry;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseHelper;
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

    public void showErrorDialog(String errorMessage, String errorDescription) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        alert.setHeaderText(errorMessage);
        alert.setContentText(errorDescription);

        alert.showAndWait();
    }

    /**
     * @return true if all locales should be affected, false otherwise - or absent if dialog was dismissed.
     */
    public Optional<Boolean> showResourceDeletionDialog(DbDto.Topic topic, DatabaseEntry resource, String localeCode) {
        Alert alert = new Alert(CONFIRMATION);
        alert.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        alert.setHeaderText(String.format(DisplayConstants.MESSAGE_DELETED_RESOURCE,
                topic.getLabel(),
                resource.toDisplayableValue()));
        alert.setContentText(DisplayConstants.WARNING_DELETED_RESOURCE + "\n" + DisplayConstants.QUESTION_AFFECTED_LOCALES);

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

    /**
     * @param topicObject       : topic contents to be affected
     * @param updatedResource   : resource to apply, or absent to create a new one
     * @return resulting resource, or absent if dialog was dismissed.
     */
    public Optional<Pair<String, String>> showEditResourceDialog(DbDto topicObject, Optional<DatabaseEntry> updatedResource) {
        Dialog<Pair<String, String>> editResourceDialog = new Dialog<>();
        editResourceDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);

        boolean updateResourceMode = updatedResource.isPresent();
        String defaultReference;
        String defaultValue = "";
        DbDto.Topic topic = topicObject.getTopic();
        if (updateResourceMode) {
            DatabaseEntry resource = updatedResource.get();
            editResourceDialog.setHeaderText(String.format(DisplayConstants.MESSAGE_EDITED_RESOURCE,
                    topic.getLabel(),
                    resource.toDisplayableValue()));
            defaultReference = resource.referenceProperty().get();
            defaultValue = resource.valueProperty().get();
        } else {
            editResourceDialog.setHeaderText(DisplayConstants.MESSAGE_ADDED_RESOURCE + topic.getLabel());
            defaultReference = DatabaseHelper.generateUniqueResourceEntryIdentifier(topicObject);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField referenceTextField = new TextField(defaultReference);
        Platform.runLater(referenceTextField::requestFocus);
        TextField valueTextField = new TextField(defaultValue);
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
}
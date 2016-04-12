package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.gui.database.domain.javafx.ResourceEntryDataItem;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.OTHER;

/**
 * Helper class to build and display dialog boxes.
 */
public class DialogsHelper {

    /**
     * @return true if all locales should be affected, false otherwise - or absent if dialog was dismissed.
     */
    public Optional<Boolean> showResourceDeletionDialog(DbDto.Topic topic, ResourceEntryDataItem resource, String localeCode) {
        Alert alert = new Alert(CONFIRMATION);
        alert.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        alert.setHeaderText(String.format(DisplayConstants.MESSAGE_DELETED_RESOURCE,
                topic.getLabel(),
                resource.toDisplayableValueForLocale(DbResourceDto.Locale.UNITED_STATES))); // Use current locale
        alert.setContentText(String.format("%s\n%s", DisplayConstants.WARNING_DELETED_RESOURCE, DisplayConstants.QUESTION_AFFECTED_LOCALES));

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
     * Display a dialog box to add or remove a resource.
     * It enables locale selection.
     * @param topicObject       : topic contents to be affected
     * @param updatedResource   : resource to apply, or absent to create a new one
     * @return resulting resource, or absent if dialog was dismissed.
     */
    public Optional<LocalizedResource> showEditResourceDialog(DbDto topicObject, Optional<ResourceEntryDataItem> updatedResource, DbResourceDto.Locale currentLocale) {
        Dialog<LocalizedResource> editResourceDialog = new Dialog<>();
        editResourceDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);

        boolean updateResourceMode = updatedResource.isPresent();
        String defaultReference;
        String defaultValue = DisplayConstants.VALUE_RESOURCE_DEFAULT;
        DbDto.Topic topic = topicObject.getTopic();
        if (updateResourceMode) {
            ResourceEntryDataItem resource = updatedResource.get();
            editResourceDialog.setHeaderText(String.format(DisplayConstants.MESSAGE_EDITED_RESOURCE,
                    topic.getLabel(),
                    resource.toDisplayableValueForLocale(currentLocale)));
            defaultReference = resource.referenceProperty().get();
            defaultValue = resource.valuePropertyForLocale(currentLocale).get();
        } else {
            editResourceDialog.setHeaderText(DisplayConstants.MESSAGE_ADDED_RESOURCE + topic.getLabel());
            defaultReference = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ChoiceBox<String> localeChoiceBox = createLocaleChoiceBox(currentLocale);
        TextField referenceTextField = new TextField(defaultReference);
        TextField valueTextField = new TextField(defaultValue);
        Platform.runLater(referenceTextField::requestFocus);

        grid.add(new Label(DisplayConstants.LABEL_CHOICEBOX_LOCALE), 0, 0);
        grid.add(localeChoiceBox, 1, 0);
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_REFERENCE), 0, 1);
        grid.add(referenceTextField, 1, 1);
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_VALUE), 0, 2);
        grid.add(valueTextField, 1, 2);
        editResourceDialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_OK, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        editResourceDialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);

        editResourceDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Optional<DbResourceDto.Locale> affectedLocale = Optional.empty();
                int selectedLocaleIndex = localeChoiceBox.getSelectionModel().getSelectedIndex();
                if (selectedLocaleIndex != 0) {
                    affectedLocale = Optional.of(DbResourceDto.Locale.values()[selectedLocaleIndex - 1]);
                }
                return new LocalizedResource( new Pair<>(referenceTextField.getText(), valueTextField.getText()), affectedLocale);
            }
            return null;
        });

        return editResourceDialog.showAndWait();
    }

    /**
     * Displays a dialog box with results of export, allowing to copy them to clipboard.
     * @param result    : exported data to be displayed
     */
    public void showExportResultDialog(String result) {
        Dialog<Boolean> resultDialog = new Dialog<>();
        resultDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT);

        TextArea textArea = new TextArea(result);
        textArea.setPrefWidth(650);
        textArea.setPrefHeight(150);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        AnchorPane.setLeftAnchor(textArea, 0.0);
        AnchorPane.setRightAnchor(textArea, 0.0);
        AnchorPane.setTopAnchor(textArea, 0.0);
        AnchorPane.setBottomAnchor(textArea, 0.0);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(textArea);
        resultDialog.getDialogPane().setContent(anchorPane);

        ButtonType fileExportButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_SAVE, OTHER);
        ButtonType closeButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CLOSE, CANCEL_CLOSE);
        resultDialog.getDialogPane().getButtonTypes().setAll(fileExportButtonType, closeButtonType);

        resultDialog.resultConverterProperty().setValue(fileExportButtonType::equals);

        final Optional<Boolean> potentialResult = resultDialog.showAndWait();

        if (potentialResult.orElse(false)) {
            askForLocationThenExportToFile(result);
        }
    }

    private static void askForLocationThenExportToFile(String contents) {
        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(false, null);
        if (!potentialFile.isPresent()) {
            return;
        }

        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT_FILE;
        String fileLocation = potentialFile.get().getPath();
        try (FileWriter fileWriter = new FileWriter(fileLocation)) {
            fileWriter.write(contents);
            CommonDialogsHelper.showDialog(INFORMATION, dialogTitle, DisplayConstants.MESSAGE_FILE_EXPORT_OK, fileLocation);
        } catch (IOException ioe) {
            CommonDialogsHelper.showDialog(ERROR, dialogTitle, DisplayConstants.MESSAGE_FILE_EXPORT_KO, DisplayConstants.MESSAGE_SEE_LOGS);
        }
    }

    private static ChoiceBox<String> createLocaleChoiceBox(DbResourceDto.Locale currentLocale) {
        ObservableList<String> localeItems = FXCollections.observableArrayList();

        int localeIndex = 0 ;
        int currentLocaleIndex = 0;
        localeItems.add(DisplayConstants.LABEL_ITEM_LOCALE_ALL);
        for (DbResourceDto.Locale locale : asList(DbResourceDto.Locale.values())) {
            if (locale == currentLocale) {
                localeItems.add(String.format(DisplayConstants.LABEL_ITEM_LOCALE_CURRENT, locale.getCode()));
                currentLocaleIndex = localeIndex;
            } else {
                localeItems.add(locale.getCode());
                localeIndex++;
            }
        }

        ChoiceBox<String> localeChoiceBox = new ChoiceBox<>(localeItems);
        localeChoiceBox.getSelectionModel().select(currentLocaleIndex + 1);
        return localeChoiceBox;
    }
}
package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.gui.database.domain.javafx.ResourceEntryDataItem;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
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
import java.util.stream.Collectors;

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
                resource.toDisplayableValueForLocale(Locale.UNITED_STATES))); // Use current locale
        alert.setContentText(String.format("%s%n%s", DisplayConstants.WARNING_DELETED_RESOURCE, DisplayConstants.QUESTION_AFFECTED_LOCALES));

        ButtonType allLocalesButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_ALL);
        ButtonType currentLocaleButtonType = new ButtonType(String.format(DisplayConstants.LABEL_BUTTON_CURRENT_LOCALE, localeCode));
        ButtonType cancelButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        alert.getButtonTypes().setAll(allLocalesButtonType, currentLocaleButtonType, cancelButtonType);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() == cancelButtonType) {
            return Optional.empty();
        }

        return Optional.of(result.get() == allLocalesButtonType);
    }

    /**
     * Display a dialog box to modify a resource.
     * It enables locale selection.
     *
     * @param topicObject     : topic contents to be affected
     * @param updatedResource : resource to apply, or absent to create a new one
     * @param currentLocale   : selected locale in GUI settings
     * @return resulting resource, or absent if dialog was dismissed.
     */
    public Optional<LocalizedResource> showEditResourceDialog(DbDto topicObject, ResourceEntryDataItem updatedResource, Locale currentLocale) {
        String defaultReference = updatedResource.referenceProperty().get();
        String defaultValue = updatedResource.valuePropertyForLocale(currentLocale).get();

        Dialog<LocalizedResource> editResourceDialog = createLocalizedResourceDialog(currentLocale, defaultReference, defaultValue);
        editResourceDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        editResourceDialog.setHeaderText(String.format(DisplayConstants.MESSAGE_EDITED_RESOURCE,
                topicObject.getTopic().getLabel(),
                updatedResource.toDisplayableValueForLocale(currentLocale)));

        return editResourceDialog.showAndWait();
    }

    /**
     * Display a dialog box to add a resource.
     * It enables locale selection.
     *
     * @param topicObject   : topic contents to be affected
     * @param currentLocale : selected locale in GUI settings
     * @return resulting resource, or absent if dialog was dismissed.
     */
    public Optional<LocalizedResource> showAddResourceDialog(DbDto topicObject, Locale currentLocale) {
        String defaultReference = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject);

        final Dialog<LocalizedResource> addResourceDialog = createLocalizedResourceDialog(currentLocale, defaultReference, DisplayConstants.VALUE_RESOURCE_DEFAULT);
        addResourceDialog.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        addResourceDialog.setHeaderText(DisplayConstants.MESSAGE_ADDED_RESOURCE + topicObject.getTopic().getLabel());

        return addResourceDialog.showAndWait();
    }

    /**
     * Displays a dialog box with results of export, allowing to copy them to clipboard.
     *
     * @param result : exported data to be displayed
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

    private static Dialog<LocalizedResource> createLocalizedResourceDialog(Locale currentLocale, String defaultReference, String defaultValue) {
        Dialog<LocalizedResource> editResourceDialog = new Dialog<>();
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
                Optional<Locale> affectedLocale = Optional.empty();
                int selectedLocaleIndex = localeChoiceBox.getSelectionModel().getSelectedIndex();
                if (selectedLocaleIndex != 0) {
                    affectedLocale = Optional.of(Locale.values()[selectedLocaleIndex - 1]);
                }
                return new LocalizedResource(new Pair<>(referenceTextField.getText(), valueTextField.getText()), affectedLocale);
            }
            return null;
        });
        return editResourceDialog;
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

    private static ChoiceBox<String> createLocaleChoiceBox(Locale currentLocale) {
        ObservableList<String> localeItems = FXCollections.observableArrayList();

        localeItems.add(DisplayConstants.LABEL_ITEM_LOCALE_ALL);
        localeItems.addAll(
                Locale.valuesAsStream()
                        .map(locale -> {
                            if (locale == currentLocale) {
                                return String.format(DisplayConstants.LABEL_ITEM_LOCALE_CURRENT, locale.getCode());
                            } else {
                                return locale.getCode();
                            }
                        })
                        .collect(Collectors.toList())
        );

        ChoiceBox<String> localeChoiceBox = new ChoiceBox<>(localeItems);
        localeChoiceBox.getSelectionModel().select(0);
        return localeChoiceBox;
    }
}

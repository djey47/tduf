package fr.tduf.gui.database.controllers.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.options.FileBrowsingOptions;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.common.game.domain.Locale.fromOrder;
import static java.util.Arrays.asList;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.OTHER;

/**
 * Helper class to build and display dialog boxes.
 */
public class DialogsHelper {
    private static final String THIS_CLASS_NAME = DialogsHelper.class.getSimpleName();

    private static final String DEFAULT_PATH = ".";

    /**
     * All file location kind to be stored
     */
    protected enum FileLocation { TDUF, TDUPK, PCH, TXT }

    private Map<FileLocation, String> fileLocations = new HashMap<>();

    /**
     * Display a dialog box to modify a resource.
     * It enables locale selection.
     *
     * @param topicObject     : topic contents to be affected
     * @param updatedResource : resource to apply
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
    public void showExportResultDialog(String result, Window parent) {
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

        if (resultDialog.showAndWait().orElse(false)) {
            askForLocationThenExportToFile(result, parent);
        }
    }

    /**
     * Displays file load dialog for TDUPE Performance Pack
     * @return empty if no selection was made (dismissed)
     */
    public Optional<String> askForPerformancePackLocation(Window parent) {
        List<FileChooser.ExtensionFilter> extensionFilters = asList(FxConstants.EXTENSION_FILTER_TDUPE_PP, FxConstants.EXTENSION_FILTER_ALL);

        return askForLoadLocation(FileLocation.TDUPK, extensionFilters, parent);
    }

    /**
     * Displays file load dialog for TDUMT patch
     * @return empty if no selection was made (dismissed)
     */
    public Optional<String> askForGenuinePatchLocation(Window parent) {
        List<FileChooser.ExtensionFilter> extensionFilters = asList(FxConstants.EXTENSION_FILTER_TDUMT_PATCH, FxConstants.EXTENSION_FILTER_ALL);

        return askForLoadLocation(FileLocation.PCH, extensionFilters, parent);
    }

    /**
     * Displays file load dialog for TDUF patch
     * @return empty if no selection was made (dismissed)
     */
    public Optional<String> askForPatchLocation(Window parent) {
        List<FileChooser.ExtensionFilter> extensionFilters = asList(FxConstants.EXTENSION_FILTER_TDUF_PATCH, FxConstants.EXTENSION_FILTER_ALL);

        return askForLoadLocation(FileLocation.TDUF, extensionFilters, parent);
    }

    /**
     * Displays file save dialog for TDUF patch
     * @return empty if no selection was made (dismissed)
     */
    public Optional<String> askForPatchSaveLocation(Window parent) throws IOException {
        List<FileChooser.ExtensionFilter> extensionFilters = asList(FxConstants.EXTENSION_FILTER_TDUF_PATCH, FxConstants.EXTENSION_FILTER_ALL);

        return askForSaveLocation(FileLocation.TDUF, extensionFilters, parent);
    }

    /**
     * To be used by plugins (by extending this class)
     */
    protected Optional<String> askForLoadLocation(FileLocation fileLocation, List<FileChooser.ExtensionFilter> extensionFilters, Window parent) {
        FileBrowsingOptions options = FileBrowsingOptions.builder()
                .forLoading()
                .withDialogTitle(String.format(DisplayConstants.TITLE_FORMAT_LOAD, fileLocation))
                .withExtensionFilters(extensionFilters)
                .withInitialDirectory(fileLocations.getOrDefault(fileLocation, DEFAULT_PATH))
                .build();
        return CommonDialogsHelper.browseForFilename(options, parent)
                .map((file) -> {
                    fileLocations.put(fileLocation, file.getParent());
                    return file.getPath();
                });
    }

    /**
     * To be used by plugins (by extending this class)
     */
    protected Optional<String> askForSaveLocation(FileLocation fileLocation, List<FileChooser.ExtensionFilter> extensionFilters, Window parent) {
        FileBrowsingOptions options = FileBrowsingOptions.builder()
                .forSaving()
                .withDialogTitle(String.format(DisplayConstants.TITLE_FORMAT_SAVE, fileLocation))
                .withExtensionFilters(extensionFilters)
                .withInitialDirectory(fileLocations.getOrDefault(fileLocation, DEFAULT_PATH))
                .build();
        return CommonDialogsHelper.browseForFilename(options, parent)
                .map((file) -> {
                    fileLocations.put(fileLocation, file.getParent());
                    return file.getPath();
                });
    }

    private void askForLocationThenExportToFile(String contents, Window parent) {
        List<FileChooser.ExtensionFilter> extensionFilters = asList(FxConstants.EXTENSION_FILTER_TDUPE_PP, FxConstants.EXTENSION_FILTER_TEXT, FxConstants.EXTENSION_FILTER_ALL);

        askForSaveLocation(FileLocation.TXT, extensionFilters, parent)
                .ifPresent(location -> {
            String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT_FILE;
            try (FileWriter fileWriter = new FileWriter(location)) {
                fileWriter.write(contents);

                final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(INFORMATION)
                        .withTitle(dialogTitle)
                        .withMessage(DisplayConstants.MESSAGE_FILE_EXPORT_OK)
                        .withDescription(location)
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, parent);
            } catch (IOException ioe) {
                Log.error(THIS_CLASS_NAME, ExceptionUtils.getStackTrace(ioe));

                final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(ERROR)
                        .withTitle(dialogTitle)
                        .withMessage(DisplayConstants.MESSAGE_FILE_EXPORT_KO)
                        .withDescription(DisplayConstants.MESSAGE_SEE_LOGS)
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, parent);
            }
        });
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

        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_REFERENCE), 0, 0);
        grid.add(referenceTextField, 1, 0);
        grid.add(new Label(DisplayConstants.LABEL_TEXTFIELD_VALUE), 0, 1);
        grid.add(valueTextField, 1, 1);
        grid.add(localeChoiceBox, 2, 1);
        editResourceDialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_OK, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_CANCEL, CANCEL_CLOSE);
        editResourceDialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);

        editResourceDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Locale affectedLocale = null;
                int selectedLocaleIndex = localeChoiceBox.getSelectionModel().getSelectedIndex();
                if (selectedLocaleIndex != 0) {
                    affectedLocale = fromOrder(selectedLocaleIndex);
                }
                return new LocalizedResource(new Pair<>(referenceTextField.getText(), valueTextField.getText()), affectedLocale);
            }
            return null;
        });
        return editResourceDialog;
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

package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.gui.common.helper.javafx.CommonDialogsHelper;
import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.controllers.helper.DynamicFieldControlsHelper;
import fr.tduf.gui.database.controllers.helper.DynamicLinkControlsHelper;
import fr.tduf.gui.database.converter.CurrentEntryIndexToStringConverter;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.converter.EntryItemsCountToStringConverter;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.factory.EntryCellFactory;
import fr.tduf.gui.database.stages.EntriesDesigner;
import fr.tduf.gui.database.stages.ResourcesDesigner;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private DynamicFieldControlsHelper dynamicFieldControlsHelper;
    private DynamicLinkControlsHelper dynamicLinkControlsHelper;
    private DialogsHelper dialogsHelper;

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;
    private ResourcesStageController resourcesStageController;
    private EntriesStageController entriesStageController;

    Property<DbDto.Topic> currentTopicProperty;
    Property<DbResourceDto.Locale> currentLocaleProperty;
    Property<ContentEntryDataItem> currentEntryProperty;
    Property<Long> currentEntryIndexProperty;
    SimpleStringProperty currentEntryLabelProperty;
    Property<Integer> entryItemsCountProperty;
    Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();
    Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourceListByTopicLink = new HashMap<>();
    ObservableList<ContentEntryDataItem> browsableEntryList;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private Label currentTopicLabel;

    @FXML
    private Label currentEntryLabel;

    @FXML
    ChoiceBox<DbResourceDto.Locale> localesChoiceBox;

    @FXML
    ChoiceBox<String> profilesChoiceBox;

    @FXML
    TabPane tabPane;

    @FXML
    private VBox defaultTab;

    @FXML
    private TextField databaseLocationTextField;

    @FXML
    private TextField entryNumberTextField;

    @FXML
    private ComboBox<ContentEntryDataItem> entryNumberComboBox;

    @FXML
    private Label entryItemsCountLabel;

    @FXML
    private Label statusLabel;

    private Map<String, VBox> tabContentByName = new HashMap<>();

    private List<DbDto> databaseObjects = new ArrayList<>();
    private DbDto currentTopicObject;

    private EditorLayoutDto layoutObject;
    private EditorLayoutDto.EditorProfileDto profileObject;
    private BulkDatabaseMiner databaseMiner;

    private Stack<EditorLocation> navigationHistory = new Stack<>();

    @Override
    protected void init() throws IOException {
        viewDataController = new MainStageViewDataController(this);
        changeDataController = new MainStageChangeDataController(this);

        dynamicFieldControlsHelper = new DynamicFieldControlsHelper(this);
        dynamicLinkControlsHelper = new DynamicLinkControlsHelper(this);
        dialogsHelper = new DialogsHelper();

        initSettingsPane();

        initResourcesStageController();

        initEntriesStageController();

        initTopicEntryHeaderPane();

        initStatusBar();
    }

    @FXML
    public void handleBrowseDirectoryButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleBrowseDirectoryButtonMouseClick");

        browseForDatabaseDirectory();
    }

    @FXML
    public void handleLoadButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLoadButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        loadDatabaseFromDirectory(databaseLocation);
    }

    @FXML
    public void handleSaveButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleSaveButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (databaseObjects == null
                || databaseObjects.isEmpty()
                || StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        saveDatabaseToDirectory(databaseLocation);
    }

    @FXML
    public void handleSearchEntryButtonAction(ActionEvent actionEvent) {
        System.out.println("handleSearchEntryButtonAction");

        if (currentTopicObject == null) {
            return;
        }

        askForReferenceAndSwitchToEntry();
    }

    @FXML
    public void handleNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleNextButtonMouseClick");

        if (currentTopicObject == null) {
            return;
        }

        viewDataController.switchToNextEntry();
    }

    @FXML
    public void handleFastNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastNextButtonMouseClick");

        if (currentTopicObject == null) {
            return;
        }

        viewDataController.switchToNext10Entry();
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        if (currentTopicObject == null) {
            return;
        }

        viewDataController.switchToPreviousEntry();
    }

    @FXML
    public void handleFastPreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastPreviousButtonMouseClick");

        if (currentTopicObject == null) {
            return;
        }

        viewDataController.switchToPrevious10Entry();
    }

    @FXML
    public void handleFirstButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFirstButtonMouseClick");

        if (currentTopicObject == null) {
            return;
        }

        viewDataController.switchToFirstEntry();
    }

    @FXML
    public void handleLastButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        if (currentTopicObject == null) {
            return;
        }

        viewDataController.switchToLastEntry();
    }

    @FXML
    public void handleEntryNumberTextFieldKeyPressed(KeyEvent keyEvent) {
        System.out.println("handleEntryNumberTextFieldKeyPressed");

        if (currentTopicObject == null) {
            return;
        }

        if (KeyCode.ENTER == keyEvent.getCode()
                || KeyCode.TAB == keyEvent.getCode()) {
            viewDataController.switchToContentEntry(currentEntryIndexProperty.getValue());
        }
    }

    @FXML
    public void handleBackButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        viewDataController.switchToPreviousLocation();
    }

    @FXML
    public void handleAddEntryButtonAction(ActionEvent actionEvent) {
        System.out.println("handleAddEntryButtonAction");

        if (currentTopicObject == null) {
            return;
        }

        addEntryAndUpdateStage();
    }

    @FXML
    public void handleDuplicateEntryButtonAction(ActionEvent actionEvent) {
        System.out.println("handleDuplicateEntryButtonAction");

        if (currentTopicObject == null) {
            return;
        }

        duplicateEntryAndUpdateStage();
    }

    @FXML
    public void handleRemoveEntryButtonAction(ActionEvent actionEvent) {
        System.out.println("handleRemoveEntryButtonAction");

        if (currentTopicObject == null) {
            return;
        }

        removeCurrentEntryAndUpdateStage();
    }

    @FXML
    public void handleExportEntryLineMenuAction(ActionEvent actionEvent) {
        System.out.println("handleExportEntryLineMenuAction");

        if (currentTopicObject == null || currentEntryIndexProperty.getValue() == null) {
            return;
        }

        exportCurrentEntryAsLineAndShowResult();
    }

    @FXML
    public void handleExportEntryPchMenuAction(ActionEvent actionEvent) {
        System.out.println("handleExportEntryPchMenuAction");

        if (currentTopicObject == null || currentEntryIndexProperty.getValue() == null) {
            return;
        }

        exportCurrentEntryAsPchValueAndShowResult();
    }

    @FXML
    public void handleExportEntryTdufPatchMenuAction(ActionEvent actionEvent) throws IOException {
        System.out.println("handleExportEntryTdufPatchMenuAction");

        if (currentTopicObject == null || currentEntryIndexProperty.getValue() == null) {
            return;
        }

        askForPatchLocationAndExportCurrentEntryToFile();
    }

    @FXML
    public void handleImportEntryTdufPatchMenuAction(ActionEvent actionEvent) throws IOException {
        System.out.println("handleImportEntryTdufPatchMenuAction");

        if (currentTopicObject == null) {
            return;
        }

        askForPatchLocationAndImportData();
    }

    @FXML
    public void handleImportPerformancePackMenuAction(ActionEvent actionEvent) {
        System.out.println("handleImportPerformancePackMenuAction");

        if (currentTopicObject == null
                || DbDto.Topic.CAR_PHYSICS_DATA != currentTopicProperty.getValue()) {
            return;
        }

        askForPerformancePackLocationAndImportData();
    }

    public EventHandler<ActionEvent> handleBrowseResourcesButtonMouseClick(DbDto.Topic targetTopic, SimpleStringProperty targetReferenceProperty, int fieldRank) {
        return (actionEvent) -> {
            System.out.println("browseResourcesButton clicked");

            resourcesStageController.initAndShowDialog(targetReferenceProperty, fieldRank, localesChoiceBox.getValue(), targetTopic);
        };
    }

    public EventHandler<ActionEvent> handleBrowseEntriesButtonMouseClick(DbDto.Topic targetTopic, List<Integer> labelFieldRanks, SimpleStringProperty targetEntryReferenceProperty, int fieldRank) {
        return (actionEvent) -> {
            System.out.println("browseEntriesButton clicked");

            entriesStageController.initAndShowDialog(targetEntryReferenceProperty.get(), fieldRank, targetTopic, labelFieldRanks);
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(DbDto.Topic targetTopic, int fieldRank, String targetProfileName) {
        return (actionEvent) -> {
            System.out.println("gotoReferenceButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            databaseMiner.getRemoteContentEntryWithInternalIdentifier(currentTopicObject.getTopic(), fieldRank, currentEntryIndexProperty.getValue(), targetTopic)

                    .ifPresent((remoteContentEntry) -> viewDataController.switchToProfileAndEntry(targetProfileName, remoteContentEntry.getId(), true));
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName) {
        return (actionEvent) -> {
            System.out.println("gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            this.viewDataController.switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
        };
    }

    public EventHandler<ActionEvent> handleAddLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            System.out.println("handleAddLinkedEntryButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            Optional<ContentEntryDataItem> potentialSelectedEntry = Optional.empty();
            DbDto.Topic finalTopic = targetTopic;
            List<DbStructureDto.Field> structureFields = databaseMiner.getDatabaseTopic(targetTopic).get().getStructure().getFields();
            if (DatabaseStructureQueryHelper.getUidFieldRank(structureFields).isPresent()) {
                // Association topic -> browse remote entries
                finalTopic = topicLinkObject.getTopic();
                potentialSelectedEntry = entriesStageController.initAndShowModalDialog(targetTopic, targetProfileName);
            }
            addLinkedEntryAndUpdateStage(tableViewSelectionModel, finalTopic, potentialSelectedEntry, topicLinkObject);
        };
    }

    public EventHandler<ActionEvent> handleRemoveLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            System.out.println("handleRemoveLinkedEntryButton clicked");

            ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();
            if (selectedItem == null) {
                return;
            }

            removeLinkedEntryAndUpdateStage(tableViewSelectionModel, topicLinkObject);
        };
    }

    public EventHandler<MouseEvent> handleLinkTableMouseClick(String targetProfileName, DbDto.Topic targetTopic) {
        return event -> {
            System.out.println("handleLinkTableMouseClick, targetProfileName:" + targetProfileName + ", targetTopic:" + targetTopic);

            if (MouseButton.PRIMARY == event.getButton() && event.getClickCount() == 2) {
                TableViewHelper.getMouseSelectedItem(event)
                        .ifPresent((selectedResource) -> viewDataController.switchToSelectedResourceForLinkedTopic((ContentEntryDataItem) selectedResource, targetTopic, targetProfileName));
            }
        };
    }

    public ChangeListener<Boolean> handleTextFieldFocusChange(int fieldRank, SimpleStringProperty textFieldValueProperty) {
        return (observable, oldFocusState, newFocusState) -> {
            System.out.println("handleTextFieldFocusChange, focused=" + newFocusState + ", fieldRank=" + fieldRank + ", fieldValue=" + textFieldValueProperty.get());

            if (oldFocusState && !newFocusState) {
                changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, textFieldValueProperty.get());
            }
        };
    }

    public ChangeListener<Boolean> handleBitfieldCheckboxSelectionChange(int fieldRank, SimpleStringProperty textFieldValueProperty) {
        return ((observable, oldCheckedState, newCheckedState) -> {
            System.out.println("handleBitfieldCheckboxSelectionChange, checked=" + newCheckedState + ", fieldRank=" + fieldRank);

            if (newCheckedState != oldCheckedState) {
                changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, textFieldValueProperty.get());
            }
        });
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        if (databaseObjects.isEmpty()) {
            return;
        }

        applyProfile(newProfileName);
    }

    private void handleLocaleChoiceChanged(DbResourceDto.Locale newLocale) {
        System.out.println("handleLocaleChoiceChanged: " + newLocale.name());

        if (databaseObjects.isEmpty()) {
            return;
        }

        viewDataController.updateAllPropertiesWithItemValues();
    }

    private void handleEntryChoiceChanged(ContentEntryDataItem newEntry) {
        System.out.println("handleEntryChoiceChanged: " + newEntry);

        if (newEntry != null) {
            viewDataController.switchToContentEntry(newEntry.getInternalEntryId());
        }
    }

    private void initResourcesStageController() throws IOException {
        Stage resourcesStage = new Stage();
        Platform.runLater(() -> resourcesStage.initOwner(getWindow())); // runLater() ensures main stage will be initialized first.

        resourcesStageController = ResourcesDesigner.init(resourcesStage);
        resourcesStageController.setMainStageController(this);
    }

    private void initEntriesStageController() throws IOException {
        Stage entriesStage = new Stage();
        Platform.runLater(() -> entriesStage.initOwner(getWindow())); // runLater() ensures main stage will be initialized first.

        entriesStageController = EntriesDesigner.init(entriesStage);
        entriesStageController.setMainStageController(this);
    }

    private void initSettingsPane() throws IOException {
        settingsPane.setExpanded(false);

        viewDataController.fillLocales();
        localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue)));

        viewDataController.loadAndFillProfiles();
        profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

        String databaseDirectory = SettingsConstants.DATABASE_DIRECTORY_DEFAULT;
        List<String> appParameters = DatabaseEditor.getParameterList();
        if (!appParameters.isEmpty()) {
            databaseDirectory = appParameters.get(0);
        }
        databaseLocationTextField.setText(databaseDirectory);
    }

    private void initStatusBar() {
        entryItemsCountProperty = new SimpleObjectProperty<>(-1);
        currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);

        entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new CurrentEntryIndexToStringConverter());
        entryItemsCountLabel.textProperty().bindBidirectional(entryItemsCountProperty, new EntryItemsCountToStringConverter());

        statusLabel.setText(DisplayConstants.LABEL_STATUS_VERSION);
    }

    private void initTopicEntryHeaderPane() {
        currentEntryProperty = new SimpleObjectProperty<>();
        currentTopicProperty = new SimpleObjectProperty<>();
        currentEntryLabelProperty = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_ENTRY_DEFAULT);
        browsableEntryList = FXCollections.observableArrayList();

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
        currentEntryLabel.textProperty().bindBidirectional(currentEntryLabelProperty);

        entryNumberComboBox.setItems(browsableEntryList);
        entryNumberComboBox.setCellFactory(new EntryCellFactory());
        entryNumberComboBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleEntryChoiceChanged((ContentEntryDataItem) newValue));
    }

    private void initTabPane() {
        initGroupTabs();

        addDynamicControls();

        viewDataController.updateAllPropertiesWithItemValues();
    }

    private void initGroupTabs() {
        defaultTab.getChildren().clear();

        tabPane.getTabs().remove(1, tabPane.getTabs().size());
        tabContentByName.clear();

        if (profileObject.getGroups() != null) {
            profileObject.getGroups().forEach((groupName) -> {
                VBox vbox = new VBox();
                Tab groupTab = new Tab(groupName, new ScrollPane(vbox));

                tabPane.getTabs().add(tabPane.getTabs().size(), groupTab);

                tabContentByName.put(groupName, vbox);
            });
        }
    }

    private void addDynamicControls() {
        if (profileObject.getFieldSettings() != null) {
            dynamicFieldControlsHelper.addAllFieldsControls(
                    layoutObject,
                    profilesChoiceBox.getValue(),
                    currentTopicObject.getTopic());
        }

        if (profileObject.getTopicLinks() != null) {
            dynamicLinkControlsHelper.addAllLinksControls(
                    profileObject);
        }
    }

    private void applyProfile(String profileName) {
        profileObject = EditorLayoutHelper.getAvailableProfileByName(profileName, layoutObject);
        currentTopicObject = databaseMiner.getDatabaseTopic(profileObject.getTopic()).get();

        currentTopicProperty.setValue(currentTopicObject.getTopic());
        currentEntryIndexProperty.setValue(0L);
        entryItemsCountProperty.setValue(currentTopicObject.getData().getEntries().size());
        rawValuePropertyByFieldRank.clear();
        resolvedValuePropertyByFieldRank.clear();
        resourceListByTopicLink.clear();

        viewDataController.fillBrowsableEntries(currentTopicObject.getTopic());

        initTabPane();
    }

    private void browseForDatabaseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File directory = new File(this.databaseLocationTextField.getText());
        if (directory.exists()) {
            directoryChooser.setInitialDirectory(directory);
        }

        File selectedDirectory = directoryChooser.showDialog(getWindow());
        if (selectedDirectory != null) {
            this.databaseLocationTextField.setText(selectedDirectory.getPath());
        }
    }

    private void loadDatabaseFromDirectory(String databaseLocation) {
        databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
        if (!databaseObjects.isEmpty()) {
            databaseMiner = BulkDatabaseMiner.load(this.databaseObjects);

            profilesChoiceBox.getSelectionModel().selectFirst();

            navigationHistory.clear();
        }
    }

    private void saveDatabaseToDirectory(String databaseLocation) {
        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects, databaseLocation);

        CommonDialogsHelper.showDialog(Alert.AlertType.INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES, DisplayConstants.MESSAGE_DATABASE_SAVED, databaseLocation);
    }

    private void addEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.addEntryForCurrentTopic();

        viewDataController.updateEntryCountAndSwitchToEntry(newEntryIndex);
    }

    private void duplicateEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.duplicateCurrentEntry();

        viewDataController.updateEntryCountAndSwitchToEntry(newEntryIndex);
    }

    private void addLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, Optional<ContentEntryDataItem> potentialLinkedEntry, TopicLinkDto topicLinkObject) {
        String sourceEntryRef = databaseMiner.getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty.getValue(), currentTopicProperty.getValue()).get();
        Optional<String> targetEntryRef = Optional.empty();
        if (potentialLinkedEntry.isPresent()) {
            targetEntryRef = Optional.of(potentialLinkedEntry.get().referenceProperty().get());
        }
        changeDataController.addLinkedEntry(sourceEntryRef, targetEntryRef, targetTopic);

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectLastRowAndScroll(tableViewSelectionModel.getTableView());
    }

    private void removeCurrentEntryAndUpdateStage() {
        long currentEntryIndex = currentEntryIndexProperty.getValue();
        changeDataController.removeEntryWithIdentifier(currentEntryIndex, currentTopicProperty.getValue());

        if (currentEntryIndex == 0) {
            currentEntryIndex = 1;
        }
        viewDataController.updateEntryCountAndSwitchToEntry(currentEntryIndex - 1);
    }

    private void removeLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();

        changeDataController.removeEntryWithIdentifier(selectedItem.getInternalEntryId(), topicLinkObject.getTopic());

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectRowAndScroll(initialRowIndex, tableViewSelectionModel.getTableView());
    }

    private void exportCurrentEntryAsLineAndShowResult() {
        dialogsHelper.showExportResultDialog(changeDataController.exportCurrentEntryAsLine());
    }

    private void exportCurrentEntryAsPchValueAndShowResult() {
        dialogsHelper.showExportResultDialog(changeDataController.exportCurrentEntryToPchValue());
    }

    private void askForPatchLocationAndExportCurrentEntryToFile() throws IOException {

        DbDto.Topic currentTopic = currentTopicObject.getTopic();
        Optional<String> potentialEntryRef = databaseMiner.getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty.getValue(), currentTopic);
        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT;
        if (!potentialEntryRef.isPresent()) {
            CommonDialogsHelper.showDialog(Alert.AlertType.ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_EXPORT_ENTRY, DisplayConstants.MESSAGE_ENTRY_WITHOUT_REF);
            return;
        }

        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(false, getWindow());
        if (!potentialFile.isPresent()) {
            return;
        }

        String fileLocation = potentialFile.get().getPath();
        if (changeDataController.exportEntryToPatchFile(currentTopic, potentialEntryRef, fileLocation)) {
            CommonDialogsHelper.showDialog(Alert.AlertType.INFORMATION, dialogTitle, DisplayConstants.MESSAGE_ENTRY_EXPORTED, fileLocation);
        } else {
            CommonDialogsHelper.showDialog(Alert.AlertType.ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_EXPORT_ENTRY, DisplayConstants.MESSAGE_SEE_LOGS);
        }
    }

    private void askForPatchLocationAndImportData() {
        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(true, getWindow());
        if (!potentialFile.isPresent()) {
            return;
        }

        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_IMPORT;
        try {
            File patchFile = potentialFile.get();
            changeDataController.importPatch(patchFile);

            viewDataController.updateEntryCount();
            viewDataController.updateAllPropertiesWithItemValues();

            CommonDialogsHelper.showDialog(Alert.AlertType.INFORMATION, dialogTitle, DisplayConstants.MESSAGE_DATA_IMPORTED, patchFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();

            CommonDialogsHelper.showDialog(Alert.AlertType.ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_IMPORT_PATCH, DisplayConstants.MESSAGE_SEE_LOGS);
        }
    }

    private void askForPerformancePackLocationAndImportData() {
        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(true, getWindow());
        if (!potentialFile.isPresent()) {
            return;
        }

        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_IMPORT_PERFORMANCE_PACK;
        try {
            String packFilePath = potentialFile.get().getPath();
            changeDataController.importPerformancePack(packFilePath);

            viewDataController.updateAllPropertiesWithItemValues();

            CommonDialogsHelper.showDialog(Alert.AlertType.INFORMATION, dialogTitle, DisplayConstants.MESSAGE_DATA_IMPORTED_PERFORMANCE_PACK, packFilePath);
        } catch (Exception e) {
            e.printStackTrace();

            CommonDialogsHelper.showDialog(Alert.AlertType.ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK, DisplayConstants.MESSAGE_SEE_LOGS);
        }
    }

    private void askForReferenceAndSwitchToEntry() {
        CommonDialogsHelper.showInputValueDialog(
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SEARCH_ENTRY,
                DisplayConstants.LABEL_SEARCH_ENTRY)

                .ifPresent((entryReference) -> viewDataController.switchToEntryWithReference(entryReference, currentTopicProperty.getValue()));
    }

    public DbDto getCurrentTopicObject() {
        return this.currentTopicObject;
    }

    public EditorLayoutDto.EditorProfileDto getCurrentProfileObject() {
        return profileObject;
    }

    public EditorLayoutDto getLayoutObject() {
        return layoutObject;
    }

    public Map<String, VBox> getTabContentByName() {
        return tabContentByName;
    }

    public VBox getDefaultTab() {
        return defaultTab;
    }

    public Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> getResourceListByTopicLink() {
        return resourceListByTopicLink;
    }

    public BulkDatabaseMiner getMiner() {
        return databaseMiner;
    }

    public Map<Integer, SimpleStringProperty> getRawValuePropertyByFieldRank() {
        return rawValuePropertyByFieldRank;
    }

    public Map<Integer, SimpleStringProperty> getResolvedValuePropertyByFieldRank() {
        return resolvedValuePropertyByFieldRank;
    }

    void setLayoutObject(EditorLayoutDto layoutObject) {
        this.layoutObject = layoutObject;
    }

    Stack<EditorLocation> getNavigationHistory() {
        return navigationHistory;
    }

    MainStageViewDataController getViewDataController() {
        return viewDataController;
    }

    MainStageChangeDataController getChangeDataController() {
        return changeDataController;
    }

    List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }
}
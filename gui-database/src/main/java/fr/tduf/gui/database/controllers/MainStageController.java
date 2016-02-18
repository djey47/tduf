package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
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
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.factory.EntryCellFactory;
import fr.tduf.gui.database.stages.EntriesDesigner;
import fr.tduf.gui.database.stages.FieldsBrowserDesigner;
import fr.tduf.gui.database.stages.ResourcesDesigner;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto;
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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static javafx.beans.binding.Bindings.size;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private DynamicFieldControlsHelper dynamicFieldControlsHelper;
    private DynamicLinkControlsHelper dynamicLinkControlsHelper;
    private DialogsHelper dialogsHelper;

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;
    private ResourcesStageController resourcesStageController;

    private EntriesStageController entriesStageController;

    private FieldsBrowserStageController fieldsBrowserStageController;

    Property<DbDto.Topic> currentTopicProperty;
    Property<DbResourceEnhancedDto.Locale> currentLocaleProperty;
    Property<Long> currentEntryIndexProperty;
    SimpleStringProperty currentEntryLabelProperty;
    Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();
    Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourceListByTopicLink = new HashMap<>();
    ObservableList<ContentEntryDataItem> browsableEntries;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private Label currentTopicLabel;

    @FXML
    private Label currentEntryLabel;

    @FXML
    ChoiceBox<DbResourceEnhancedDto.Locale> localesChoiceBox;

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

        String initialDatabaseDirectory = SettingsConstants.DATABASE_DIRECTORY_DEFAULT;
        boolean databaseAutoLoad = false;
        List<String> appParameters = DatabaseEditor.getCommandLineParameters();
        if (!appParameters.isEmpty()) {
            initialDatabaseDirectory = appParameters.get(0);
            databaseAutoLoad = true;
        }
        initSettingsPane(initialDatabaseDirectory);

        initResourcesStageController();

        initEntriesStageController();

        initFieldsBrowserStageController();

        initTopicEntryHeaderPane();

        initStatusBar();

        if (databaseAutoLoad) {
            Log.trace(THIS_CLASS_NAME, "->init: database auto load");
            loadDatabaseFromDirectory(initialDatabaseDirectory);
        }
    }

    @FXML
    public void handleBrowseDirectoryButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleBrowseDirectoryButtonMouseClick");

        browseForDatabaseDirectory();
    }

    @FXML
    public void handleLoadButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleLoadButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        loadDatabaseFromDirectory(databaseLocation);
    }

    @FXML
    public void handleSaveButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleSaveButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (databaseObjects == null
                || databaseObjects.isEmpty()
                || StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        saveDatabaseToDirectory(databaseLocation);
    }

    @FXML
    public void handleSearchEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> askForReferenceAndSwitchToEntry());
    }

    @FXML
    public void handleNextButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleNextButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToNextEntry());
    }

    @FXML
    public void handleFastNextButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleFastNextButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToNext10Entry());
    }

    @FXML
    public void handlePreviousButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handlePreviousButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToPreviousEntry());
    }

    @FXML
    public void handleFastPreviousButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleFastPreviousButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToPrevious10Entry());
    }

    @FXML
    public void handleFirstButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleFirstButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToFirstEntry());
    }

    @FXML
    public void handleLastButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleLastButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToLastEntry());
    }

    @FXML
    public void handleEntryNumberTextFieldKeyPressed(KeyEvent keyEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleEntryNumberTextFieldKeyPressed");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> {
                    if (KeyCode.ENTER == keyEvent.getCode()
                            || KeyCode.TAB == keyEvent.getCode()) {
                        viewDataController.switchToContentEntry(currentEntryIndexProperty.getValue());
                    }
                });
    }

    @FXML
    public void handleBackButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleLastButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> viewDataController.switchToPreviousLocation());
    }

    @FXML
    public void handleAddEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleAddEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> addEntryAndUpdateStage());
    }

    @FXML
    public void handleDuplicateEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleDuplicateEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> duplicateEntryAndUpdateStage());
    }

    @FXML
    public void handleRemoveEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleRemoveEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent((topicObject) -> removeCurrentEntryAndUpdateStage());
    }

    @FXML
    public void handleExportEntryLineMenuAction() {
        Log.trace(THIS_CLASS_NAME, "->handleExportEntryLineMenuAction");

        if (currentTopicObject == null || currentEntryIndexProperty.getValue() == null) {
            return;
        }

        exportCurrentEntryAsLineAndShowResult();
    }

    @FXML
    public void handleExportEntryPchMenuAction() {
        Log.trace(THIS_CLASS_NAME, "->handleExportEntryPchMenuAction");

        if (currentTopicObject == null || currentEntryIndexProperty.getValue() == null) {
            return;
        }

        exportCurrentEntryAsPchValueAndShowResult();
    }

    @FXML
    public void handleExportEntryTdufPatchMenuAction() throws IOException {
        Log.trace(THIS_CLASS_NAME, "->handleExportEntryTdufPatchMenuAction");

        if (currentTopicObject == null || currentEntryIndexProperty.getValue() == null) {
            return;
        }

        askForExportOptionsThenExportToFile();
    }

    @FXML
    public void handleImportEntryTdufPatchMenuAction() throws IOException {
        Log.trace(THIS_CLASS_NAME, "->handleImportEntryTdufPatchMenuAction");

        ofNullable(currentTopicObject)

                .ifPresent((topicObject) -> askForPatchLocationAndImportDataFromFile());
    }

    @FXML
    public void handleImportPerformancePackMenuAction() {
        Log.trace(THIS_CLASS_NAME, "->handleImportPerformancePackMenuAction");

        if (currentTopicObject == null
                || DbDto.Topic.CAR_PHYSICS_DATA != currentTopicProperty.getValue()) {
            return;
        }

        askForPerformancePackLocationAndImportData();
    }

    public EventHandler<ActionEvent> handleBrowseResourcesButtonMouseClick(DbDto.Topic targetTopic, SimpleStringProperty targetReferenceProperty, int fieldRank) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->browseResourcesButton clicked");

            resourcesStageController.initAndShowDialog(targetReferenceProperty, fieldRank, localesChoiceBox.getValue(), targetTopic);
        };
    }

    public EventHandler<ActionEvent> handleBrowseEntriesButtonMouseClick(DbDto.Topic targetTopic, List<Integer> labelFieldRanks, SimpleStringProperty targetEntryReferenceProperty, int fieldRank) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->browseEntriesButton clicked");

            entriesStageController.initAndShowDialog(targetEntryReferenceProperty.get(), fieldRank, targetTopic, labelFieldRanks);
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(DbDto.Topic targetTopic, int fieldRank, String targetProfileName) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->gotoReferenceButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            databaseMiner.getRemoteContentEntryWithInternalIdentifier(currentTopicObject.getTopic(), fieldRank, currentEntryIndexProperty.getValue(), targetTopic)
                    .ifPresent((remoteContentEntry) -> viewDataController.switchToProfileAndEntry(targetProfileName, remoteContentEntry.getId(), true));
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            viewDataController.switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
        };
    }

    public EventHandler<ActionEvent> handleAddLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->handleAddLinkedEntryButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            List<DbStructureDto.Field> structureFields = databaseMiner.getDatabaseTopic(targetTopic).get().getStructure().getFields();
            if (DatabaseStructureQueryHelper.getUidFieldRank(structureFields).isPresent()) {
                // Association topic -> browse remote entries in target topic
                entriesStageController.initAndShowModalDialog(empty(), targetTopic, targetProfileName)
                        .ifPresent((selectedEntry) -> addLinkedEntryAndUpdateStage(tableViewSelectionModel, topicLinkObject.getTopic(), of(selectedEntry), topicLinkObject));
            } else {
                // Direct topic link -> add default entry in target topic
                addLinkedEntryAndUpdateStage(tableViewSelectionModel, targetTopic, Optional.empty(), topicLinkObject);
            }
        };
    }

    public EventHandler<ActionEvent> handleRemoveLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->handleRemoveLinkedEntryButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent((selectedItem) -> removeLinkedEntryAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<ActionEvent> handleMoveLinkedEntryUpButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->handleMoveLinkedEntryUpButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent((selectedItem) -> moveLinkedEntryUpAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<ActionEvent> handleMoveLinkedEntryDownButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->handleMoveLinkedEntryDownButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent((selectedItem) -> moveLinkedEntryDownAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<MouseEvent> handleLinkTableMouseClick(String targetProfileName, DbDto.Topic targetTopic) {
        return (mouseEvent) -> {
            Log.trace(THIS_CLASS_NAME, "->handleLinkTableMouseClick, targetProfileName:" + targetProfileName + ", targetTopic:" + targetTopic);

            if (MouseButton.PRIMARY == mouseEvent.getButton() && mouseEvent.getClickCount() == 2) {
                TableViewHelper.getMouseSelectedItem(mouseEvent, ContentEntryDataItem.class)
                        .ifPresent((selectedResource) -> viewDataController.switchToSelectedResourceForLinkedTopic(selectedResource, targetTopic, targetProfileName));
            }
        };
    }

    public ChangeListener<Boolean> handleTextFieldFocusChange(int fieldRank, SimpleStringProperty textFieldValueProperty) {
        return (observable, oldFocusState, newFocusState) -> {
            Log.trace(THIS_CLASS_NAME, "->handleTextFieldFocusChange, focused=" + newFocusState + ", fieldRank=" + fieldRank + ", fieldValue=" + textFieldValueProperty.get());

            if (oldFocusState && !newFocusState) {
                changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, textFieldValueProperty.get());
            }
        };
    }

    public ChangeListener<Boolean> handleBitfieldCheckboxSelectionChange(int fieldRank, SimpleStringProperty textFieldValueProperty) {
        return ((observable, oldCheckedState, newCheckedState) -> {
            Log.trace(THIS_CLASS_NAME, "->handleBitfieldCheckboxSelectionChange, checked=" + newCheckedState + ", fieldRank=" + fieldRank);

            if (newCheckedState != oldCheckedState) {
                changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, textFieldValueProperty.get());
            }
        });
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        Log.trace(THIS_CLASS_NAME, "->handleProfileChoiceChanged: " + newProfileName);

        if (newProfileName == null || databaseObjects.isEmpty()) {
            return;
        }

        applyProfile(newProfileName);
    }

    private void handleLocaleChoiceChanged(DbResourceEnhancedDto.Locale newLocale) {
        Log.trace(THIS_CLASS_NAME, "->handleLocaleChoiceChanged: " + newLocale.name());

        if (databaseObjects.isEmpty()) {
            return;
        }

        viewDataController.updateAllPropertiesWithItemValues();
    }

    private void handleEntryChoiceChanged(ContentEntryDataItem newEntry) {
        Log.trace(THIS_CLASS_NAME, "->handleEntryChoiceChanged: " + newEntry);

        ofNullable(newEntry)
                .map(ContentEntryDataItem::getInternalEntryId)
                .ifPresent(viewDataController::switchToContentEntry);
    }

    private void initResourcesStageController() throws IOException {
        Stage resourcesStage = new Stage();
        Platform.runLater(() -> resourcesStage.initOwner(getWindow())); // runLater() ensures main stage will be initialized first.

        resourcesStageController = ResourcesDesigner.init(resourcesStage);
        resourcesStageController.setMainStageController(this);
    }

    private void initEntriesStageController() throws IOException {
        Stage entriesStage = new Stage();
        Platform.runLater(() -> entriesStage.initOwner(getWindow()));

        entriesStageController = EntriesDesigner.init(entriesStage);
        entriesStageController.setMainStageController(this);
    }

    private void initFieldsBrowserStageController() throws IOException {
        Stage entriesStage = new Stage();
        Platform.runLater(() -> entriesStage.initOwner(getWindow()));

        fieldsBrowserStageController = FieldsBrowserDesigner.init(entriesStage);
        fieldsBrowserStageController.setMainStageController(this);
    }

    private void initSettingsPane(String databaseDirectory) throws IOException {
        settingsPane.setExpanded(false);

        viewDataController.fillLocales();
        localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue)));

        viewDataController.loadAndFillProfiles();
        profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

        databaseLocationTextField.setText(databaseDirectory);
    }

    private void initStatusBar() {
        currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);

        entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new CurrentEntryIndexToStringConverter());
        entryItemsCountLabel.textProperty().bind(size(browsableEntries).asString(DisplayConstants.LABEL_ITEM_ENTRY_COUNT));

        statusLabel.setText(DisplayConstants.LABEL_STATUS_VERSION);
    }

    private void initTopicEntryHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();
        currentEntryLabelProperty = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_ENTRY_DEFAULT);
        browsableEntries = FXCollections.observableArrayList();

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
        currentEntryLabel.textProperty().bindBidirectional(currentEntryLabelProperty);

        entryNumberComboBox.setItems(browsableEntries);
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
        rawValuePropertyByFieldRank.clear();
        resolvedValuePropertyByFieldRank.clear();
        resourceListByTopicLink.clear();

        viewDataController.fillBrowsableEntries();

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
            databaseMiner = BulkDatabaseMiner.load(databaseObjects);

            profilesChoiceBox.getSelectionModel().clearSelection(); // ensures event will be fired even though 1st item is selected
            profilesChoiceBox.getSelectionModel().selectFirst();

            navigationHistory.clear();
        }
    }

    private void saveDatabaseToDirectory(String databaseLocation) {
        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseObjects, databaseLocation);

        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES, DisplayConstants.MESSAGE_DATABASE_SAVED, databaseLocation);
    }

    private void addEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.addEntryForCurrentTopic();

        viewDataController.updateEntriesAndSwitchTo(newEntryIndex);
        viewDataController.fillBrowsableEntries();
    }

    private void duplicateEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.duplicateCurrentEntry();

        viewDataController.updateEntriesAndSwitchTo(newEntryIndex);
    }

    private void addLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, Optional<ContentEntryDataItem> potentialLinkedEntry, TopicLinkDto topicLinkObject) {
        String sourceEntryRef = databaseMiner.getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty.getValue(), currentTopicProperty.getValue()).get();
        Optional<String> targetEntryRef = Optional.empty();
        if (potentialLinkedEntry.isPresent()) {
            targetEntryRef = of(potentialLinkedEntry.get().referenceProperty().get());
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
        viewDataController.updateEntriesAndSwitchTo(currentEntryIndex - 1);
    }

    private void removeLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();

        changeDataController.removeEntryWithIdentifier(selectedItem.getInternalEntryId(), topicLinkObject.getTopic());

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectRowAndScroll(initialRowIndex, tableViewSelectionModel.getTableView());
    }

    private void moveLinkedEntryUpAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        final TableView<ContentEntryDataItem> tableView = tableViewSelectionModel.getTableView();
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        if (initialRowIndex == 0) {
            return;
        }

        ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();

        changeDataController.moveEntryWithIdentifier(-1, selectedItem.getInternalEntryId(), topicLinkObject.getTopic());

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectRowAndScroll(initialRowIndex - 1, tableView);
    }

    private void moveLinkedEntryDownAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        final TableView<ContentEntryDataItem> tableView = tableViewSelectionModel.getTableView();
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        if (initialRowIndex == tableView.getItems().size() - 1) {
            return;
        }

        ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();

        changeDataController.moveEntryWithIdentifier(1, selectedItem.getInternalEntryId(), topicLinkObject.getTopic());

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectRowAndScroll(initialRowIndex + 1, tableView);
    }

    private void exportCurrentEntryAsLineAndShowResult() {
        dialogsHelper.showExportResultDialog(changeDataController.exportCurrentEntryAsLine());
    }

    private void exportCurrentEntryAsPchValueAndShowResult() {
        dialogsHelper.showExportResultDialog(changeDataController.exportCurrentEntryToPchValue());
    }

    private void askForExportOptionsThenExportToFile() throws IOException {

        final DbDto.Topic currentTopic = currentTopicObject.getTopic();
        final String name = getCurrentProfileObject().getName();

        final List<String> selectedEntryRefs = viewDataController.selectEntriesFromTopic(currentTopic, name);
        final List<String> selectedEntryFields = viewDataController.selectFieldsFromTopic(currentTopic);

        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(false, getWindow());
        if (!potentialFile.isPresent()) {
            return;
        }

        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT;
        String fileLocation = potentialFile.get().getPath();
        if (changeDataController.exportEntriesToPatchFile(currentTopic, selectedEntryRefs, selectedEntryFields, fileLocation)) {
            String message = selectedEntryRefs.isEmpty() ?
                    DisplayConstants.MESSAGE_ALL_ENTRIES_EXPORTED :
                    DisplayConstants.MESSAGE_ENTRIES_EXPORTED;
            CommonDialogsHelper.showDialog(INFORMATION, dialogTitle, message, fileLocation);
        } else {
            String message = selectedEntryRefs.isEmpty() ?
                    DisplayConstants.MESSAGE_UNABLE_EXPORT_ALL_ENTRIES :
                    DisplayConstants.MESSAGE_UNABLE_EXPORT_ENTRIES;
            CommonDialogsHelper.showDialog(ERROR, dialogTitle, message, DisplayConstants.MESSAGE_SEE_LOGS);
        }
    }

    private void askForPatchLocationAndImportDataFromFile() {
        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(true, getWindow());
        if (!potentialFile.isPresent()) {
            return;
        }

        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_IMPORT;
        try {
            File patchFile = potentialFile.get();
            final Optional<String> potentialPropertiesFile = changeDataController.importPatch(patchFile);

            viewDataController.updateAllPropertiesWithItemValues();

            String writtenPropertiesPath = "";
            if (potentialPropertiesFile.isPresent()) {
                writtenPropertiesPath += ("Written properties file: " +  System.lineSeparator() + potentialPropertiesFile.get());
            }

            CommonDialogsHelper.showDialog(INFORMATION, dialogTitle, DisplayConstants.MESSAGE_DATA_IMPORTED, writtenPropertiesPath);
        } catch (Exception e) {
            e.printStackTrace();

            CommonDialogsHelper.showDialog(ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_IMPORT_PATCH, DisplayConstants.MESSAGE_SEE_LOGS);
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

            CommonDialogsHelper.showDialog(INFORMATION, dialogTitle, DisplayConstants.MESSAGE_DATA_IMPORTED_PERFORMANCE_PACK, packFilePath);
        } catch (Exception e) {
            e.printStackTrace();

            CommonDialogsHelper.showDialog(ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK, DisplayConstants.MESSAGE_SEE_LOGS);
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

    EntriesStageController getEntriesStageController() {
        return entriesStageController;
    }

    FieldsBrowserStageController getFieldsBrowserStageController() {
        return fieldsBrowserStageController;
    }

    List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }
}

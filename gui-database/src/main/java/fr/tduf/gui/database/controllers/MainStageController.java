package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.controllers.helper.DatabaseOpsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.common.services.DatabaseChecker;
import fr.tduf.gui.common.services.DatabaseFixer;
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
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.gui.database.services.DatabaseSaver;
import fr.tduf.gui.database.stages.EntriesDesigner;
import fr.tduf.gui.database.stages.FieldsBrowserDesigner;
import fr.tduf.gui.database.stages.ResourcesDesigner;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static javafx.beans.binding.Bindings.size;
import static javafx.beans.binding.Bindings.when;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private final BankSupport bankSupport = new GenuineBnkGateway(new CommandLineHelper());

    private DynamicFieldControlsHelper dynamicFieldControlsHelper;
    private DynamicLinkControlsHelper dynamicLinkControlsHelper;
    private DialogsHelper dialogsHelper;

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;

    private ResourcesStageController resourcesStageController;
    private EntriesStageController entriesStageController;
    private FieldsBrowserStageController fieldsBrowserStageController;

    Property<DbDto.Topic> currentTopicProperty;
    Property<fr.tduf.libunlimited.common.game.domain.Locale> currentLocaleProperty;
    Property<Long> currentEntryIndexProperty;
    SimpleStringProperty currentEntryLabelProperty;
    Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();
    Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourceListByTopicLink = new HashMap<>();
    ObservableList<ContentEntryDataItem> browsableEntries;

    private BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private DatabaseLoader databaseLoader = new DatabaseLoader();
    private DatabaseSaver databaseSaver = new DatabaseSaver();
    private DatabaseChecker databaseChecker = new DatabaseChecker();
    private DatabaseFixer databaseFixer = new DatabaseFixer();

    @FXML
    private Button loadDatabaseButton;

    @FXML
    private Label creditsLabel;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private Label currentTopicLabel;

    @FXML
    private Label currentEntryLabel;

    @FXML
    ChoiceBox<Locale> localesChoiceBox;

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

    private List<DbDto> databaseObjects = new ArrayList<>(18);
    private DbDto currentTopicObject;

    private EditorLayoutDto layoutObject;
    private EditorLayoutDto.EditorProfileDto profileObject;
    private BulkDatabaseMiner databaseMiner;

    private Deque<EditorLocation> navigationHistory = new ArrayDeque<>();

    private ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    @Override
    protected void init() throws IOException {
        applicationConfiguration.load();

        viewDataController = new MainStageViewDataController(this);
        changeDataController = new MainStageChangeDataController(this);

        dynamicFieldControlsHelper = new DynamicFieldControlsHelper(this);
        dynamicLinkControlsHelper = new DynamicLinkControlsHelper(this);
        dialogsHelper = new DialogsHelper();

        runningServiceProperty.bind(databaseLoader.runningProperty()
                        .or(databaseSaver.runningProperty()
                        .or(databaseChecker.runningProperty())));
        mouseCursorProperty().bind(
                when(runningServiceProperty)
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );

        Optional<String> initialDatabaseDirectory = viewDataController.resolveInitialDatabaseDirectory();

        initTopToolbar();

        initSettingsPane(initialDatabaseDirectory.orElse(SettingsConstants.DATABASE_DIRECTORY_DEFAULT));

        initResourcesStageController();

        initEntriesStageController();

        initFieldsBrowserStageController();

        initTopicEntryHeaderPane();

        initStatusBar();

        initServiceListeners();

        initialDatabaseDirectory.ifPresent(databaseLocation -> {
            Log.trace(THIS_CLASS_NAME, "->init: database auto load");
            try {
                loadDatabaseFromDirectory(databaseLocation);
            } catch (IOException ioe) {
                Log.error(THIS_CLASS_NAME, "Unable to load database at location: " + databaseLocation, ioe);
            }
        });
    }

    @FXML
    public void handleBrowseDirectoryButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleBrowseDirectoryButtonMouseClick");

        browseForDatabaseDirectory();
    }

    @FXML
    public void handleLoadButtonMouseClick() throws IOException {
        Log.trace(THIS_CLASS_NAME, "->handleLoadButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        loadDatabaseFromDirectory(databaseLocation);
    }

    @FXML
    public void handleSaveButtonMouseClick() throws IOException {
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
    public void handleResetDatabaseCacheMenuItemAction() throws IOException {
        Log.trace(THIS_CLASS_NAME, "->handleResetDatabaseCacheMenuItemAction");

        String databaseLocation = databaseLocationTextField.getText();
        if (StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        resetDatabaseCache(databaseLocation);
    }

    @FXML
    public void handleCheckButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleCheckButtonAction");

        String databaseLocation = databaseLocationTextField.getText();
        if (StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        checkDatabase(databaseLocation);
    }


    @FXML
    public void handleSearchEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> askForReferenceAndSwitchToEntry());
    }

    @FXML
    public void handleNextButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleNextButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> viewDataController.switchToNextEntry());
    }

    @FXML
    public void handleFastNextButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleFastNextButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> viewDataController.switchToNext10Entry());
    }

    @FXML
    public void handlePreviousButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handlePreviousButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> viewDataController.switchToPreviousEntry());
    }

    @FXML
    public void handleFastPreviousButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleFastPreviousButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> viewDataController.switchToPrevious10Entry());
    }

    @FXML
    public void handleFirstButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleFirstButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> viewDataController.switchToFirstEntry());
    }

    @FXML
    public void handleLastButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleLastButtonMouseClick");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> viewDataController.switchToLastEntry());
    }

    @FXML
    public void handleEntryNumberTextFieldKeyPressed(KeyEvent keyEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleEntryNumberTextFieldKeyPressed");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> {
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
                .ifPresent(topicObject -> viewDataController.switchToPreviousLocation());
    }

    @FXML
    public void handleAddEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleAddEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> addEntryAndUpdateStage());
    }

    @FXML
    public void handleDuplicateEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleDuplicateEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> duplicateEntryAndUpdateStage());
    }

    @FXML
    public void handleRemoveEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleRemoveEntryButtonAction");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> removeCurrentEntryAndUpdateStage());
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

                .ifPresent(topicObject -> askForPatchLocationAndImportDataFromFile());
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
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->browseResourcesButton clicked");

            resourcesStageController.initAndShowDialog(targetReferenceProperty, fieldRank, localesChoiceBox.getValue(), targetTopic);
        };
    }

    public EventHandler<ActionEvent> handleBrowseEntriesButtonMouseClick(DbDto.Topic targetTopic, List<Integer> labelFieldRanks, SimpleStringProperty targetEntryReferenceProperty, int fieldRank) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->browseEntriesButton clicked");

            entriesStageController.initAndShowDialog(targetEntryReferenceProperty.get(), fieldRank, targetTopic, labelFieldRanks);
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(DbDto.Topic targetTopic, int fieldRank, String targetProfileName) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->gotoReferenceButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            databaseMiner.getRemoteContentEntryWithInternalIdentifier(currentTopicObject.getTopic(), fieldRank, currentEntryIndexProperty.getValue(), targetTopic)
                    .ifPresent(remoteContentEntry -> viewDataController.switchToProfileAndEntry(targetProfileName, remoteContentEntry.getId(), true));
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            viewDataController.switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
        };
    }

    public EventHandler<ActionEvent> handleAddLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleAddLinkedEntryButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            List<DbStructureDto.Field> structureFields = databaseMiner.getDatabaseTopic(targetTopic).get().getStructure().getFields();
            if (DatabaseStructureQueryHelper.getUidFieldRank(structureFields).isPresent()) {
                // Association topic -> browse remote entries in target topic
                entriesStageController.initAndShowModalDialog(empty(), targetTopic, targetProfileName)
                        .ifPresent(selectedEntry -> addLinkedEntryWithTargetRef(tableViewSelectionModel, topicLinkObject.getTopic(), selectedEntry, topicLinkObject));
            } else {
                // Direct topic link -> add default entry in target topic
                addLinkedEntryWithoutTargetRef(tableViewSelectionModel, targetTopic, topicLinkObject);
            }
        };
    }

    public EventHandler<ActionEvent> handleRemoveLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleRemoveLinkedEntryButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent(selectedItem -> removeLinkedEntryAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<ActionEvent> handleMoveLinkedEntryUpButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleMoveLinkedEntryUpButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent(selectedItem -> moveLinkedEntryUpAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<ActionEvent> handleMoveLinkedEntryDownButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleMoveLinkedEntryDownButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent(selectedItem -> moveLinkedEntryDownAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<MouseEvent> handleLinkTableMouseClick(String targetProfileName, DbDto.Topic targetTopic) {
        return mouseEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleLinkTableMouseClick, targetProfileName:" + targetProfileName + ", targetTopic:" + targetTopic);

            if (MouseButton.PRIMARY == mouseEvent.getButton()
                    && mouseEvent.getClickCount() == 2) {
                TableViewHelper.getMouseSelectedItem(mouseEvent, ContentEntryDataItem.class)
                        .ifPresent(selectedResource -> viewDataController.switchToSelectedResourceForLinkedTopic(selectedResource, targetTopic, targetProfileName));
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

    public ChangeListener<Boolean> handleSliderValueChange(int fieldRank, SimpleStringProperty rawValueProperty) {
        return (observable, oldState, newState) -> {
            Log.trace(THIS_CLASS_NAME, "->handleSliderValueChange, fieldRank=" + fieldRank + ", rawValue=" + rawValueProperty.get());

            changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, rawValueProperty.get());
        };
    }

    public ChangeListener<Boolean> handleBitfieldCheckboxSelectionChange(int fieldRank, SimpleStringProperty textFieldValueProperty) {
        return (observable, oldCheckedState, newCheckedState) -> {
            Log.trace(THIS_CLASS_NAME, "->handleBitfieldCheckboxSelectionChange, checked=" + newCheckedState + ", fieldRank=" + fieldRank);

            if (newCheckedState != oldCheckedState) {
                changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, textFieldValueProperty.get());
            }
        };
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        Log.trace(THIS_CLASS_NAME, "->handleProfileChoiceChanged: " + newProfileName);

        if (newProfileName == null || databaseObjects.isEmpty()) {
            return;
        }

        applyProfile(newProfileName);
    }

    private void handleLocaleChoiceChanged(Locale newLocale) {
        Log.trace(THIS_CLASS_NAME, "->handleLocaleChoiceChanged: " + newLocale.name());

        if (databaseObjects.isEmpty()) {
            return;
        }

        viewDataController.updateAllPropertiesWithItemValues();
    }

    private void handleEntryChoiceChanged(ContentEntryDataItem newEntry) {
        Log.trace(THIS_CLASS_NAME, "->handleEntryChoiceChanged: " + newEntry);

        ofNullable(newEntry)
                .map(entry -> entry.internalEntryIdProperty().get())
                .ifPresent(viewDataController::switchToContentEntry);
    }

    private void initServiceListeners() {
        databaseLoader.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                updateDisplayWithLoadedObjects(databaseLoader.getValue());
            } else if (FAILED == newState) {
                CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_LOAD, DisplayConstants.MESSAGE_DATABASE_LOAD_KO, databaseLoader.getException().getMessage());
            }
        });

        databaseSaver.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SAVE, DisplayConstants.MESSAGE_DATABASE_SAVED, databaseSaver.getValue());
            } else if (FAILED == newState) {
                CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SAVE, DisplayConstants.MESSAGE_DATABASE_SAVE_KO, databaseSaver.getException().getMessage());
            }
        });

        databaseChecker.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> integrityErrors = databaseChecker.integrityErrorsProperty().get();
                if (integrityErrors.isEmpty()) {
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_CHECK_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_CHECK_OK, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_ZERO_ERROR);
                    return;
                }
                if (DatabaseOpsHelper.displayCheckResultDialog(integrityErrors, getWindow(), DisplayConstants.TITLE_APPLICATION)) {
                    fixDatabase();
                }
            } else if (FAILED == newState) {
                CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_CHECK_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_CHECK_KO, databaseChecker.getException().getMessage());
            }
        });

        databaseFixer.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> remainingErrors = databaseFixer.integrityErrorsProperty().get();
                if (remainingErrors.isEmpty()) {
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_OK, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_ZERO_ERROR_AFTER_FIX);
                } else {
                    CommonDialogsHelper.showDialog(WARNING, DisplayConstants.TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_REMAINING_ERRORS);
                }
                refreshAll();
            } else if (FAILED == newState) {
                CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO, databaseFixer.getException().getMessage());
            }
        });
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

    private void initTopToolbar() {
        creditsLabel.setText(DisplayConstants.LABEL_STATUS_VERSION);
    }

    private void initSettingsPane(String databaseDirectory) throws IOException {
        settingsPane.setExpanded(false);

        viewDataController.fillLocales();
        localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue));

        viewDataController.loadAndFillProfiles();
        profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

        databaseLocationTextField.setText(databaseDirectory);
    }

    private void initStatusBar() {
        currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);

        entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new CurrentEntryIndexToStringConverter());
        entryItemsCountLabel.textProperty().bind(size(browsableEntries).asString(DisplayConstants.LABEL_ITEM_ENTRY_COUNT));
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
            profileObject.getGroups().forEach(groupName -> {
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

        refreshAll();
    }

    private void refreshAll() {
        currentEntryIndexProperty.setValue(0L);
        rawValuePropertyByFieldRank.clear();
        resolvedValuePropertyByFieldRank.clear();
        resourceListByTopicLink.clear();

        viewDataController.fillBrowsableEntries();

        initTabPane();
    }

    private void browseForDatabaseDirectory() {
        if (runningServiceProperty.get()) {
            return;
        }

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

    private void loadDatabaseFromDirectory(String databaseLocation) throws IOException {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().bind(databaseLoader.messageProperty());

        databaseLoader.bankSupportProperty().setValue(bankSupport);
        databaseLoader.databaseLocationProperty().setValue(databaseLocation);

        databaseLoader.restart();
    }

    // TODO move to to view data controller
    private void updateDisplayWithLoadedObjects(List<DbDto> loadedDatabaseObjects) {
        this.databaseObjects = loadedDatabaseObjects;
        if (!loadedDatabaseObjects.isEmpty()) {
            databaseMiner = BulkDatabaseMiner.load(loadedDatabaseObjects);

            profilesChoiceBox.getSelectionModel().clearSelection(); // ensures event will be fired even though 1st item is selected
            profilesChoiceBox.getSelectionModel().selectFirst();

            navigationHistory.clear();

            loadDatabaseButton.disableProperty().setValue(true);

            updateConfiguration();
        }
    }

    // TODO move to to view data controller
    private void updateConfiguration() {
        try {
            applicationConfiguration.setDatabasePath(databaseLocationTextField.getText());
            applicationConfiguration.store();
        } catch (IOException ioe) {
            Log.warn(THIS_CLASS_NAME, "Unable to save application configuration", ioe);
        }
    }

    private void saveDatabaseToDirectory(String databaseLocation) throws IOException {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().bind(databaseSaver.messageProperty());

        databaseSaver.bankSupportProperty().setValue(bankSupport);
        databaseSaver.databaseLocationProperty().setValue(databaseLocation);
        databaseSaver.databaseObjectsProperty().setValue(databaseObjects);

        databaseSaver.restart();
    }

    private void addEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.addEntryForCurrentTopic();

        viewDataController.updateEntriesAndSwitchTo(newEntryIndex);
    }

    private void duplicateEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.duplicateCurrentEntry();

        viewDataController.updateEntriesAndSwitchTo(newEntryIndex);
    }

    private void addLinkedEntryWithTargetRef(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, ContentEntryDataItem linkedEntry, TopicLinkDto topicLinkObject) {
        String sourceEntryRef = databaseMiner.getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty.getValue(), currentTopicProperty.getValue()).get();
        Optional<String> targetEntryRef = ofNullable(linkedEntry)
                .map(entry -> entry.referenceProperty().get());

        changeDataController.addLinkedEntry(sourceEntryRef, targetEntryRef, targetTopic);

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectLastRowAndScroll(tableViewSelectionModel.getTableView());
    }

    private void addLinkedEntryWithoutTargetRef(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, TopicLinkDto topicLinkObject) {
        addLinkedEntryWithTargetRef(tableViewSelectionModel, targetTopic, null, topicLinkObject);
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

        changeDataController.removeEntryWithIdentifier(selectedItem.internalEntryIdProperty().get(), topicLinkObject.getTopic());

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

        changeDataController.moveEntryWithIdentifier(-1, selectedItem.internalEntryIdProperty().get(), topicLinkObject.getTopic());

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

        changeDataController.moveEntryWithIdentifier(1, selectedItem.internalEntryIdProperty().get(), topicLinkObject.getTopic());

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

            viewDataController.updateEntriesAndSwitchTo(0);
            viewDataController.updateAllPropertiesWithItemValues();

            String writtenPropertiesPath = "";
            if (potentialPropertiesFile.isPresent()) {
                writtenPropertiesPath += ("Written properties file: " + System.lineSeparator() + potentialPropertiesFile.get());
            }

            CommonDialogsHelper.showDialog(INFORMATION, dialogTitle, DisplayConstants.MESSAGE_DATA_IMPORTED, writtenPropertiesPath);
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, e);
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
            Log.error(THIS_CLASS_NAME, e);
            CommonDialogsHelper.showDialog(ERROR, dialogTitle, DisplayConstants.MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK, DisplayConstants.MESSAGE_SEE_LOGS);
        }
    }

    private void askForReferenceAndSwitchToEntry() {
        CommonDialogsHelper.showInputValueDialog(
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SEARCH_ENTRY,
                DisplayConstants.LABEL_SEARCH_ENTRY)

                .ifPresent(entryReference -> viewDataController.switchToEntryWithReference(entryReference, currentTopicProperty.getValue()));
    }

    private void resetDatabaseCache(String databaseDirectory) throws IOException {
        DatabaseBanksCacheHelper.clearCache(Paths.get(databaseDirectory));

        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_DB_CACHE, DisplayConstants.MESSAGE_DELETED_CACHE, databaseDirectory);
    }

    private void checkDatabase(String databaseLocation) {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().bind(databaseChecker.messageProperty());

        if (!databaseObjects.isEmpty()) {
            databaseChecker.loadedDatabaseObjectsProperty().setValue(databaseObjects);
        }
        databaseChecker.databaseLocationProperty().setValue(databaseLocation);
        databaseChecker.bankSupportProperty().setValue(bankSupport);

        databaseChecker.restart();
    }

    private void fixDatabase() {
        // Do not check for service here, as checker may still be in running state.
        statusLabel.textProperty().bind(databaseFixer.messageProperty());

        databaseFixer.fromService(databaseChecker);

        databaseFixer.restart();
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

    ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    Deque<EditorLocation> getNavigationHistory() {
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

    long getCurrentEntryIndex() {
        return currentEntryIndexProperty.getValue();
    }

    DbDto.Topic getCurrentTopic() {
        return currentTopicProperty.getValue();
    }
}

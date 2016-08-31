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
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
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
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static javafx.beans.binding.Bindings.when;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    final Deque<EditorLocation> navigationHistory = new ArrayDeque<>();

    private final Property<Long> currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);
    private final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>();
    final Property<Locale> currentLocaleProperty = new SimpleObjectProperty<>(UNITED_STATES);
    private final StringProperty currentEntryLabelProperty = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_ENTRY_DEFAULT);

    private EditorLayoutDto layoutObject;
    private EditorLayoutDto.EditorProfileDto profileObject;

    private BulkDatabaseMiner databaseMiner;
    private DialogsHelper dialogsHelper;
    private final BankSupport bankSupport = new GenuineBnkGateway(new CommandLineHelper());
    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;
    private ResourcesStageController resourcesStageController;
    private EntriesStageController entriesStageController;
    private FieldsBrowserStageController fieldsBrowserStageController;

    private final List<DbDto> databaseObjects = new ArrayList<>(18);
    private DbDto currentTopicObject;

    private final BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private final DatabaseLoader databaseLoader = new DatabaseLoader();
    private final DatabaseSaver databaseSaver = new DatabaseSaver();
    private final DatabaseChecker databaseChecker = new DatabaseChecker();
    private final DatabaseFixer databaseFixer = new DatabaseFixer();

    @FXML
    ChoiceBox<Locale> localesChoiceBox;

    @FXML
    ChoiceBox<String> profilesChoiceBox;

    @FXML
    Button loadDatabaseButton;

    @FXML
    TabPane tabPane;

    @FXML
    TextField databaseLocationTextField;

    @FXML
    Label creditsLabel;

    @FXML
    TitledPane settingsPane;

    @FXML
    TextField entryNumberTextField;

    @FXML
    Label entryItemsCountLabel;

    @FXML
    Label currentTopicLabel;

    @FXML
    Label currentEntryLabel;

    @FXML
    ComboBox<ContentEntryDataItem> entryNumberComboBox;

    @FXML
    private Label statusLabel;

    @Override
    protected void init() throws IOException {
        applicationConfiguration.load();

        viewDataController = new MainStageViewDataController(this);
        changeDataController = new MainStageChangeDataController(this);
        resourcesStageController = ResourcesDesigner.init(this);
        entriesStageController = EntriesDesigner.init(this);
        fieldsBrowserStageController = FieldsBrowserDesigner.init(this);

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

        viewDataController.initTopToolbar();

        viewDataController.initSettingsPane(
                initialDatabaseDirectory.orElse(SettingsConstants.DATABASE_DIRECTORY_DEFAULT),
                (observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue),
                (observable, oldValue, newValue) -> handleProfileChoiceChanged(newValue));

        viewDataController.initTopicEntryHeaderPane(
                (observable, oldValue, newValue) -> handleEntryChoiceChanged(newValue));

        viewDataController.initStatusBar();

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
        if (databaseObjects.isEmpty()
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

            viewDataController.switchToProfileAndRemoteEntry(targetProfileName, currentEntryIndexProperty.getValue(), fieldRank, currentTopicProperty.getValue(), targetTopic);
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
                        .ifPresent(selectedEntry -> addLinkedEntryWithTargetRefAndUpdateStage(tableViewSelectionModel, topicLinkObject.getTopic(), selectedEntry, topicLinkObject));
            } else {
                // Direct topic link -> add default entry in target topic
                addLinkedEntryWithoutTargetRefAndUpdateStage(tableViewSelectionModel, targetTopic, topicLinkObject);
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

        viewDataController.applyProfile(newProfileName);
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
            if (SUCCEEDED == newState
                    && !databaseLoader.getValue().isEmpty()) {
                databaseObjects.clear();
                databaseObjects.addAll(databaseLoader.getValue());
                viewDataController.updateDisplayWithLoadedObjects();
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
                viewDataController.refreshAll();
            } else if (FAILED == newState) {
                CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB, fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO, databaseFixer.getException().getMessage());
            }
        });
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

    private void addLinkedEntryWithTargetRefAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, ContentEntryDataItem linkedEntry, TopicLinkDto topicLinkObject) {
        changeDataController.addLinkedEntryWithTargetRef(targetTopic, linkedEntry);
        viewDataController.updateLinkProperties(topicLinkObject);
        TableViewHelper.selectLastRowAndScroll(tableViewSelectionModel.getTableView());
    }

    private void addLinkedEntryWithoutTargetRefAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, TopicLinkDto topicLinkObject) {
        addLinkedEntryWithTargetRefAndUpdateStage(tableViewSelectionModel, targetTopic, null, topicLinkObject);
    }

    private void removeCurrentEntryAndUpdateStage() {
        long currentEntryIndex = currentEntryIndexProperty.getValue();
        changeDataController.removeEntryWithIdentifier(currentEntryIndex, currentTopicProperty.getValue());
        viewDataController.updateEntriesAndSwitchTo(currentEntryIndex - 1);
    }

    private void removeLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();
        changeDataController.removeEntryWithIdentifier(selectedItem.internalEntryIdProperty().get(), topicLinkObject.getTopic());
        viewDataController.updateLinkProperties(topicLinkObject);
        TableViewHelper.selectRowAndScroll(tableViewSelectionModel.getSelectedIndex(), tableViewSelectionModel.getTableView());
    }

    private void moveLinkedEntryUpAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        final TableView<ContentEntryDataItem> tableView = tableViewSelectionModel.getTableView();
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        if (initialRowIndex == 0) {
            return;
        }

        changeDataController.moveEntryWithIdentifier(-1, tableViewSelectionModel.getSelectedItem().internalEntryIdProperty().get(), topicLinkObject.getTopic());
        viewDataController.updateLinkProperties(topicLinkObject);
        TableViewHelper.selectRowAndScroll(initialRowIndex - 1, tableView);
    }

    private void moveLinkedEntryDownAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        final TableView<ContentEntryDataItem> tableView = tableViewSelectionModel.getTableView();
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        if (initialRowIndex == tableView.getItems().size() - 1) {
            return;
        }

        changeDataController.moveEntryWithIdentifier(1, tableViewSelectionModel.getSelectedItem().internalEntryIdProperty().get(), topicLinkObject.getTopic());
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
        final List<String> selectedEntryRefs = viewDataController.selectEntriesFromTopic();
        final List<String> selectedEntryFields = viewDataController.selectFieldsFromTopic();

        Optional<File> potentialFile = CommonDialogsHelper.browseForFilename(false, getWindow());
        if (!potentialFile.isPresent()) {
            return;
        }

        String dialogTitle = DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT;
        String fileLocation = potentialFile.get().getPath();
        if (changeDataController.exportEntriesToPatchFile(currentTopicProperty.getValue(), selectedEntryRefs, selectedEntryFields, fileLocation)) {
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

    void setCurrentTopicObject(DbDto currentTopicObject) {
        this.currentTopicObject = currentTopicObject;
    }

    public EditorLayoutDto.EditorProfileDto getCurrentProfileObject() {
        return profileObject;
    }

    void setCurrentProfileObject(EditorLayoutDto.EditorProfileDto currentProfileObject) {
        this.profileObject = currentProfileObject;
    }

    public EditorLayoutDto getLayoutObject() {
        return layoutObject;
    }

    void setLayoutObject(EditorLayoutDto layoutObject) {
        this.layoutObject = layoutObject;
    }

    public BulkDatabaseMiner getMiner() {
        return databaseMiner;
    }

    void setMiner(BulkDatabaseMiner databaseMiner) {
        this.databaseMiner = databaseMiner;
    }

    Property<DbDto.Topic> getCurrentTopicProperty() {
        return currentTopicProperty;
    }

    Property<Long> getCurrentEntryIndexProperty() {
        return currentEntryIndexProperty;
    }

    Property<Locale> getCurrentLocaleProperty() {
        return currentLocaleProperty;
    }

    StringProperty getCurrentEntryLabelProperty() {
        return currentEntryLabelProperty;
    }

    public MainStageViewDataController getViewData() {
        return viewDataController;
    }

    MainStageChangeDataController getChangeData() {
        return changeDataController;
    }

    EntriesStageController getEntriesStageController() {
        return entriesStageController;
    }

    FieldsBrowserStageController getFieldsBrowserStageController() {
        return fieldsBrowserStageController;
    }

    // For tests
    List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }

    // For tests
    long getCurrentEntryIndex() {
        return currentEntryIndexProperty.getValue();
    }

    // For tests
    ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    ChoiceBox<String> getProfilesChoiceBox() {
        return profilesChoiceBox;
    }
}

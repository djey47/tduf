package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.AppConstants;
import fr.tduf.gui.common.controllers.helper.DatabaseOpsHelper;
import fr.tduf.gui.common.game.helpers.GameSettingsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.DesktopHelper;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.common.services.DatabaseChecker;
import fr.tduf.gui.common.services.DatabaseFixer;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.PluginHandler;
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
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.gui.database.common.DisplayConstants.*;
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

    private final Deque<EditorLocation> navigationHistory = new ArrayDeque<>();

    private final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(-1);
    private final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>();
    private final StringProperty currentEntryLabelProperty = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_ENTRY_DEFAULT);

    private EditorLayoutDto layoutObject;

    private BulkDatabaseMiner databaseMiner;
    private DialogsHelper dialogsHelper;
    private final BankSupport bankSupport = new GenuineBnkGateway(new CommandLineHelper());
    private PluginHandler pluginHandler;

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;
    private ResourcesStageController resourcesStageController;
    private EntriesStageController entriesStageController;
    private FieldsBrowserStageController fieldsBrowserStageController;

    private final List<DbDto> databaseObjects = new ArrayList<>(18);
    private DbDto currentTopicObject;

    private final BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private final DatabaseSaver databaseSaver = new DatabaseSaver();
    private final DatabaseChecker databaseChecker = new DatabaseChecker();
    private final DatabaseFixer databaseFixer = new DatabaseFixer();

    // Removing final allows it to be mocked in tests
    @SuppressWarnings("FieldMayBeFinal")
    private DatabaseLoader databaseLoader = new DatabaseLoader();
    @SuppressWarnings("FieldMayBeFinal")
    private ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    @FXML
    Label creditsLabel;

    @FXML
    TextField entryNumberTextField;

    @FXML
    Label entryItemsCountLabel;

    @FXML
    Label currentTopicLabel;

    @FXML
    ComboBox<ContentEntryDataItem> entryNumberComboBox;

    @FXML
    private TextField databaseLocationTextField;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private ChoiceBox<Locale> localesChoiceBox;

    @FXML
    private ChoiceBox<EditorLayoutDto.EditorProfileDto> profilesChoiceBox;

    @FXML
    private TabPane tabPane;

    @FXML
    private Label statusLabel;

    @Override
    protected void init() throws IOException {
        initConfiguration();

        viewDataController = new MainStageViewDataController(this);
        changeDataController = new MainStageChangeDataController(this);
        resourcesStageController = ResourcesDesigner.init(this);
        entriesStageController = EntriesDesigner.init(this);
        fieldsBrowserStageController = FieldsBrowserDesigner.init(this);

        dialogsHelper = new DialogsHelper();

        pluginHandler = new PluginHandler(root, changeDataController);

        viewDataController.initTopToolbar();

        Optional<String> initialDatabaseDirectory = viewDataController.resolveInitialDatabaseDirectory();
        viewDataController.initSettingsPane(
                initialDatabaseDirectory.orElse(SettingsConstants.DATABASE_DIRECTORY_DEFAULT),
                (observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue),
                (observable, oldValue, newValue) -> handleProfileChoiceChanged(newValue));

        viewDataController.initTopicEntryHeaderPane(
                (observable, oldValue, newValue) -> handleEntryChoiceChanged(newValue));

        viewDataController.initStatusBar();

        initServicePropertiesAndListeners();

        initGUIComponentsProperties();

        initialDatabaseDirectory.ifPresent(databaseLocation -> {
            Log.trace(THIS_CLASS_NAME, "->init: database auto load");
            loadDatabaseFromDirectory(databaseLocation);
        });
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
    public void handleResetSettingsMenuItemAction() throws IOException {
        Log.trace(THIS_CLASS_NAME, "->handleResetSettingsMenuItemAction");

        resetSettings();
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
    public void handleHelpButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleHelpButtonAction");

        DesktopHelper.openInBrowser(AppConstants.URL_WIKI_TOOLS_REF );
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
    public void handleImportEntryTdufPatchMenuAction() {
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

    @FXML
    public void handleImportPchMenuAction() {
        Log.trace(THIS_CLASS_NAME, "->handleImportPchMenuAction");

        ofNullable(currentTopicObject)
                .ifPresent(topicObject -> askForGenuinePatchLocationAndImportDataFromFile());
    }

    void handleDatabaseLoaderSuccess() {
        if (databaseLoader.fetchValue().isEmpty()) {
            return;
        }
        databaseObjects.clear();
        databaseObjects.addAll(databaseLoader.fetchValue());

        databaseMiner = BulkDatabaseMiner.load(databaseObjects);

        initPlugins();

        viewDataController.updateDisplayWithLoadedObjects();
    }

    void handleDatabaseSaverSuccess() {
        if(!applicationConfiguration.isEditorPluginsEnabled()) {
            return;
        }
        pluginHandler.triggerOnSaveForAllPLugins();
    }

    void initConfiguration() throws IOException {
        applicationConfiguration.load();

        // Debugging mode?
        if (applicationConfiguration.isEditorDebuggingEnabled()) {
            Log.set(Log.LEVEL_DEBUG);
            Log.debug(THIS_CLASS_NAME, "/!\\ DEBUG mode enabled via application configuration /!\\");
        }
    }

    private void handleProfileChoiceChanged(EditorLayoutDto.EditorProfileDto newProfile) {
        Log.trace(THIS_CLASS_NAME, "->handleProfileChoiceChanged: " + newProfile);

        if (newProfile == null || databaseObjects.isEmpty()) {
            return;
        }

        viewDataController.applyProfile(newProfile);
    }

    private void handleLocaleChoiceChanged(Locale newLocale) {
        Log.trace(THIS_CLASS_NAME, "->handleLocaleChoiceChanged: " + newLocale.name());

        if (databaseObjects.isEmpty()) {
            return;
        }

        viewDataController.applySelectedLocale();
    }

    private void handleEntryChoiceChanged(ContentEntryDataItem newEntry) {
        Log.trace(THIS_CLASS_NAME, "->handleEntryChoiceChanged: " + newEntry);

        ofNullable(newEntry)
                .map(entry -> entry.internalEntryIdProperty().get())
                .ifPresent(viewDataController::switchToContentEntry);
    }

    private void initGUIComponentsProperties() {
        mouseCursorProperty().bind(
                when(runningServiceProperty)
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );
    }

    private void initServicePropertiesAndListeners() {
        runningServiceProperty.bind(databaseLoader.runningProperty()
                        .or(databaseSaver.runningProperty())
                        .or(databaseChecker.runningProperty())
                        .or(databaseFixer.runningProperty()));

        databaseLoader.stateProperty().addListener(getLoaderStateChangeListener());

        databaseSaver.stateProperty().addListener(getSaverStateChangeListener());

        databaseChecker.stateProperty().addListener(getCheckerStateChangeListener());

        databaseFixer.stateProperty().addListener(getFixerStateChangeListener());
    }

    private void initPlugins() {
        if (!applicationConfiguration.isEditorPluginsEnabled()) {
            Log.info(THIS_CLASS_NAME, "Editor plugins were disabled via application configuration");
            return;
        }
        
        EditorContext editorContext = pluginHandler.getEditorContext();
        editorContext.setDatabaseLocation(databaseLoader.databaseLocationProperty().get());
        editorContext.setGameLocation(applicationConfiguration.getGamePath()
                .map(Path::toString)
                .orElseGet(
                        () -> GameSettingsHelper.askForGameLocationAndUpdateConfiguration(applicationConfiguration, getWindow())
                ));
        editorContext.setMiner(getMiner());
        editorContext.setMainWindow(getWindow());
        editorContext.setMainStageController(this);
        pluginHandler.initializeAllPlugins();
    }

    private ChangeListener<Worker.State> getFixerStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> remainingErrors = databaseFixer.integrityErrorsProperty().get();
                SimpleDialogOptions dialogOptions;
                if (remainingErrors.isEmpty()) {
                    dialogOptions = SimpleDialogOptions.builder()
                            .withContext(INFORMATION)
                            .withTitle(TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB)
                            .withMessage(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_OK)
                            .withDescription(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_ZERO_ERROR_AFTER_FIX)
                            .build();
                } else {
                    dialogOptions = SimpleDialogOptions.builder()
                            .withContext(WARNING)
                            .withTitle(TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB)
                            .withMessage(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO)
                            .withDescription(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_REMAINING_ERRORS)
                            .build();
                }
                CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                viewDataController.refreshAll();
            } else if (FAILED == newState) {
                SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(ERROR)
                        .withTitle(TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_FIX_DB)
                        .withMessage(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_FIX_KO)
                        .withDescription(databaseFixer.getException().getMessage())
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, getWindow());
            }
        };
    }

    private ChangeListener<Worker.State> getCheckerStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> integrityErrors = databaseChecker.integrityErrorsProperty().get();
                if (integrityErrors.isEmpty()) {
                    final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                            .withContext(INFORMATION)
                            .withTitle(TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_CHECK_DB)
                            .withMessage(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_CHECK_OK)
                            .withDescription(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_ZERO_ERROR)
                            .build();
                    CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    return;
                }
                if (DatabaseOpsHelper.displayCheckResultDialog(integrityErrors, getWindow(), TITLE_APPLICATION)) {
                    fixDatabase();
                }
            } else if (FAILED == newState) {
                final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(ERROR)
                        .withTitle(TITLE_APPLICATION + fr.tduf.gui.common.DisplayConstants.TITLE_SUB_CHECK_DB)
                        .withMessage(fr.tduf.gui.common.DisplayConstants.MESSAGE_DB_CHECK_KO)
                        .withDescription(databaseChecker.getException().getMessage())
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, getWindow());
            }
        };
    }

    private ChangeListener<Worker.State> getSaverStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                handleDatabaseSaverSuccess();
                final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(INFORMATION)
                        .withTitle(TITLE_APPLICATION)
                        .withMessage(DisplayConstants.MESSAGE_DATABASE_SAVED)
                        .withDescription(databaseSaver.getValue())
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, getWindow());
            } else if (FAILED == newState) {
                final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(ERROR)
                        .withTitle(TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SAVE)
                        .withMessage(DisplayConstants.MESSAGE_DATABASE_SAVE_KO)
                        .withDescription(databaseSaver.getException().getMessage())
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, getWindow());
            }
        };
    }

    private ChangeListener<Worker.State> getLoaderStateChangeListener() {
        return (observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                handleDatabaseLoaderSuccess();
            } else if (FAILED == newState) {
                final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                        .withContext(ERROR)
                        .withTitle(TITLE_APPLICATION + DisplayConstants.TITLE_SUB_LOAD)
                        .withMessage(DisplayConstants.MESSAGE_DATABASE_LOAD_KO)
                        .withDescription(databaseLoader.getException().getMessage())
                        .build();
                CommonDialogsHelper.showDialog(dialogOptions, getWindow());
            }
        };
    }

    private void browseForDatabaseDirectory() {
        if (runningServiceProperty.get()) {
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();

        File directory = new File(this.databaseLocationTextField.getText());
        directoryChooser.setTitle(DisplayConstants.TITLE_BROWSE_DB_DIRECTORY);
        if (directory.exists()) {
            directoryChooser.setInitialDirectory(directory);
        }

        File selectedDirectory = directoryChooser.showDialog(getWindow());
        if (selectedDirectory != null) {
            this.databaseLocationTextField.setText(selectedDirectory.getPath());
        }
    }

    private void loadDatabaseFromDirectory(String databaseLocation) {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(databaseLoader.messageProperty());

        databaseLoader.bankSupportProperty().setValue(bankSupport);
        databaseLoader.databaseLocationProperty().setValue(databaseLocation);
        databaseLoader.restart();
    }

    private void saveDatabaseToDirectory(String databaseLocation) {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(databaseSaver.messageProperty());

        databaseSaver.bankSupportProperty().setValue(bankSupport);
        databaseSaver.databaseLocationProperty().setValue(databaseLocation);
        databaseSaver.databaseObjectsProperty().setValue(databaseObjects);
        databaseSaver.restart();
    }

    private void addEntryAndUpdateStage() {
        int newEntryIndex = changeDataController.addEntryForCurrentTopic();
        viewDataController.updateEntriesAndSwitchTo(newEntryIndex);
    }

    private void duplicateEntryAndUpdateStage() {
        int newEntryIndex = changeDataController.duplicateCurrentEntry();
        viewDataController.updateEntriesAndSwitchTo(newEntryIndex);
    }

    private void removeCurrentEntryAndUpdateStage() {
        int currentEntryIndex = currentEntryIndexProperty.getValue();
        changeDataController.removeEntryWithIdentifier(currentEntryIndex, currentTopicProperty.getValue());
        viewDataController.updateEntriesAndSwitchTo(currentEntryIndex - 1);
    }

    private void exportCurrentEntryAsLineAndShowResult() {
        dialogsHelper.showExportResultDialog(changeDataController.exportCurrentEntryAsLine(), getWindow());
    }

    private void exportCurrentEntryAsPchValueAndShowResult() {
        dialogsHelper.showExportResultDialog(changeDataController.exportCurrentEntryToPchValue(), getWindow());
    }

    private void askForExportOptionsThenExportToFile() throws IOException {
        final List<String> selectedEntryRefs = viewDataController.selectEntriesFromTopic();
        final List<String> selectedEntryFields = viewDataController.selectFieldsFromTopic();

        dialogsHelper.askForPatchSaveLocation(getWindow())
                .ifPresent(location -> {
                    String dialogTitle = TITLE_APPLICATION + DisplayConstants.TITLE_SUB_EXPORT;
                    try {
                        if (!changeDataController.exportEntriesToPatchFile(currentTopicProperty.getValue(), selectedEntryRefs, selectedEntryFields, location)) {
                            throw new IOException();
                        }

                        String message = selectedEntryRefs.isEmpty() ?
                                DisplayConstants.MESSAGE_ALL_ENTRIES_EXPORTED :
                                DisplayConstants.MESSAGE_ENTRIES_EXPORTED;
                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(INFORMATION)
                                .withTitle(dialogTitle)
                                .withMessage(message)
                                .withDescription(location)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    } catch (IOException ioe) {
                        String message = selectedEntryRefs.isEmpty() ?
                                DisplayConstants.MESSAGE_UNABLE_EXPORT_ALL_ENTRIES :
                                DisplayConstants.MESSAGE_UNABLE_EXPORT_ENTRIES;
                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(ERROR)
                                .withTitle(dialogTitle)
                                .withMessage(message)
                                .withDescription(DisplayConstants.MESSAGE_SEE_LOGS)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    }
        });
    }

    private void askForPatchLocationAndImportDataFromFile() {
        dialogsHelper.askForPatchLocation(getWindow())
                .map(File::new)
                .ifPresent(location -> {
                    String dialogTitle = TITLE_APPLICATION + DisplayConstants.TITLE_SUB_IMPORT;
                    try {
                        final Optional<String> potentialPropertiesFile = changeDataController.importPatch(location);

                        viewDataController.updateEntriesAndSwitchTo(0);
                        viewDataController.updateAllPropertiesWithItemValues();

                        String writtenPropertiesPath = "";
                        if (potentialPropertiesFile.isPresent()) {
                            writtenPropertiesPath += ("Written properties file: " + System.lineSeparator() + potentialPropertiesFile.get());
                        }

                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(INFORMATION)
                                .withTitle(dialogTitle)
                                .withMessage(DisplayConstants.MESSAGE_DATA_IMPORTED)
                                .withDescription(writtenPropertiesPath)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    } catch (Exception e) {
                        Log.error(THIS_CLASS_NAME, e);

                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(ERROR)
                                .withTitle(dialogTitle)
                                .withMessage(DisplayConstants.MESSAGE_UNABLE_IMPORT_PATCH)
                                .withDescription(DisplayConstants.MESSAGE_SEE_LOGS)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    }
                });
    }

    private void askForPerformancePackLocationAndImportData() {
        dialogsHelper.askForPerformancePackLocation(getWindow())
                .ifPresent(location -> {
                    String dialogTitle = TITLE_APPLICATION + DisplayConstants.TITLE_SUB_IMPORT_PERFORMANCE_PACK;
                    try {
                        changeDataController.importPerformancePack(location);
                        viewDataController.updateAllPropertiesWithItemValues();

                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(INFORMATION)
                                .withTitle(dialogTitle)
                                .withMessage(DisplayConstants.MESSAGE_DATA_IMPORTED_PERFORMANCE_PACK)
                                .withDescription(location)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    } catch (Exception e) {
                        Log.error(THIS_CLASS_NAME, e);

                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(ERROR)
                                .withTitle(dialogTitle)
                                .withMessage(DisplayConstants.MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK)
                                .withDescription(DisplayConstants.MESSAGE_SEE_LOGS)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    }
                });
    }

    private void askForGenuinePatchLocationAndImportDataFromFile() {
        dialogsHelper.askForGenuinePatchLocation(getWindow())
                .ifPresent(location -> {
                    String dialogTitle = TITLE_APPLICATION + DisplayConstants.TITLE_SUB_IMPORT_TDUMT_PATCH;
                    try {
                        changeDataController.importLegacyPatch(location);
                        viewDataController.updateAllPropertiesWithItemValues();

                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(INFORMATION)
                                .withTitle(dialogTitle)
                                .withMessage(DisplayConstants.MESSAGE_DATA_IMPORTED_TDUMT_PATCH)
                                .withDescription(location)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    } catch (Exception e) {
                        Log.error(THIS_CLASS_NAME, e);

                        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                                .withContext(ERROR)
                                .withTitle(dialogTitle)
                                .withMessage(DisplayConstants.MESSAGE_UNABLE_IMPORT_TDUMT_PATCH)
                                .withDescription(DisplayConstants.MESSAGE_SEE_LOGS)
                                .build();
                        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
                    }
                });
    }

    private void askForReferenceAndSwitchToEntry() {
        CommonDialogsHelper.showInputValueDialog(TITLE_SEARCH_CONTENTS_ENTRY, LABEL_SEARCH_ENTRY, getWindow())
                .ifPresent(entryReference -> viewDataController.switchToEntryWithReference(entryReference, currentTopicProperty.getValue()));
    }

    private void resetDatabaseCache(String databaseDirectory) throws IOException {
        DatabaseBanksCacheHelper.clearCache(Paths.get(databaseDirectory));

        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(INFORMATION)
                .withTitle(TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_DB_CACHE)
                .withMessage(DisplayConstants.MESSAGE_DELETED_CACHE)
                .withDescription(databaseDirectory)
                .build();
        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
    }

    private void resetSettings() throws IOException {
        applicationConfiguration.reset();

        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(INFORMATION)
                .withTitle(TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_SETTINGS)
                .withMessage(DisplayConstants.MESSAGE_DELETED_SETTINGS)
                .withDescription(DisplayConstants.MESSAGE_RESTART_APP)
                .build();
        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
    }

    private void checkDatabase(String databaseLocation) {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().unbind();
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

        statusLabel.textProperty().unbind();
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

    Deque<EditorLocation> getNavigationHistory() {
        return navigationHistory;
    }

    Property<DbDto.Topic> getCurrentTopicProperty() {
        return currentTopicProperty;
    }

    Property<Integer> getCurrentEntryIndexProperty() {
        return currentEntryIndexProperty;
    }

    StringProperty getCurrentEntryLabelProperty() {
        return currentEntryLabelProperty;
    }

    public MainStageViewDataController getViewData() {
        return viewDataController;
    }

    public MainStageChangeDataController getChangeData() {
        return changeDataController;
    }

    EntriesStageController getEntriesStageController() {
        return entriesStageController;
    }

    ResourcesStageController getResourcesStageController() {
        return resourcesStageController;
    }

    FieldsBrowserStageController getFieldsBrowserStageController() {
        return fieldsBrowserStageController;
    }

    List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }

    public int getCurrentEntryIndex() {
        return currentEntryIndexProperty.getValue();
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    ChoiceBox<EditorLayoutDto.EditorProfileDto> getProfilesChoiceBox() {
        return profilesChoiceBox;
    }

    ChoiceBox<Locale> getLocalesChoiceBox() {
        return localesChoiceBox;
    }

    TabPane getTabPane() {
        return tabPane;
    }

    TitledPane getSettingsPane() {
        return settingsPane;
    }

    TextField getDatabaseLocationTextField() {
        return databaseLocationTextField;
    }

    public PluginHandler getPluginHandler() {
        return pluginHandler;
    }
}

package fr.tduf.gui.database.controllers.main;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.AppConstants;
import fr.tduf.gui.common.game.helpers.GameSettingsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.DesktopHelper;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.common.services.DatabaseChecker;
import fr.tduf.gui.common.services.DatabaseFixer;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.EntriesStageController;
import fr.tduf.gui.database.controllers.FieldsBrowserStageController;
import fr.tduf.gui.database.controllers.ResourcesStageController;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.converter.ModifiedFlagToTitleConverter;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
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
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.gui.database.common.DisplayConstants.*;
import static fr.tduf.libunlimited.common.forever.FileConstants.DIRECTORY_CONFIGURATION;
import static fr.tduf.libunlimited.common.forever.FileConstants.DIRECTORY_LOGS;
import static java.util.Optional.ofNullable;
import static javafx.scene.control.Alert.AlertType.*;
import static javafx.scene.control.ButtonType.OK;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private final Deque<EditorLocation> navigationHistory = new ArrayDeque<>();

    private final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(-1);
    private final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>();
    private final StringProperty currentEntryLabelProperty = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_ENTRY_DEFAULT);

    private final BooleanProperty modifiedProperty = new SimpleBooleanProperty(false);

    private EditorLayoutDto layoutObject;

    private BulkDatabaseMiner databaseMiner;
    private DialogsHelper dialogsHelper;

    private final BankSupport bankSupport = new GenuineBnkGateway(new CommandLineHelper());
    private PluginHandler pluginHandler;

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;
    private MainStageServicesController servicesController;

    private ResourcesStageController resourcesStageController;
    private EntriesStageController entriesStageController;
    private FieldsBrowserStageController fieldsBrowserStageController;

    private final List<DbDto> databaseObjects = new ArrayList<>(DbDto.Topic.values().length);
    private DbDto currentTopicObject;

    @SuppressWarnings("FieldMayBeFinal")
    private ApplicationConfiguration applicationConfiguration = DatabaseEditor.getInstance().getApplicationConfiguration();

    @FXML
    AnchorPane root;

    @FXML
    HBox mainSplashHBox;

    @FXML
    ImageView mainSplashImage;

    @FXML
    VBox mainVBox;

    @FXML
    Label creditsLabel;

    @FXML
    TextField entryNumberTextField;

    @FXML
    Label entryItemsCountLabel;

    @FXML
    Label filteredEntryItemsCountLabel;

    @FXML
    Label unfilteredEntryItemsCountLabel;

    @FXML
    Label currentTopicLabel;

    @FXML
    ComboBox<ContentEntryDataItem> entryNumberComboBox;

    @FXML
    private TextField entryFilterTextField;

    @FXML
    Button entryFilterButton;

    @FXML
    Button entryEmptyFilterButton;

    @FXML
    Label statusLabel;

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

    @Override
    protected void init() throws IOException {
        DatabaseEditor.getInstance().setMainController(this);

        viewDataController = new MainStageViewDataController(this);
        changeDataController = new MainStageChangeDataController(this);
        servicesController = new MainStageServicesController(this);

        resourcesStageController = ResourcesDesigner.init(this);
        entriesStageController = EntriesDesigner.init(this);
        fieldsBrowserStageController = FieldsBrowserDesigner.init(this);

        dialogsHelper = new DialogsHelper();

        pluginHandler = new PluginHandler(root, changeDataController);

        Optional<String> initialDatabaseDirectory = viewDataController.initSubController();

        servicesController.initServicePropertiesAndListeners();

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
    public void handleOpenSettingsFolderMenuItemAction() {
        Log.trace(THIS_CLASS_NAME, "->handleOpenSettingsFolderMenuItemAction");

        openSettings();
    }

    @FXML
    public void handleOpenLogsFolderMenuItemAction() {
        Log.trace(THIS_CLASS_NAME, "->handleOpenLogsFolderMenuItemAction");

        openLogs();
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

    @FXML
    public void handleFilterTextFieldKeyPressed(KeyEvent ke) {
        Log.trace(THIS_CLASS_NAME, "->handleFilterTextFieldKeyPressed");

        if (ke.getCode() != KeyCode.ENTER)
        {
            return;
        }

        viewDataController.applyEntryFilter();
    }

    @FXML
    public void handleEntryFilterButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleEntryFilterButtonAction");

        viewDataController.applyEntryFilter();
    }

    @FXML
    public void handleEmptyEntryFilterButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleEmptyEntryFilterButtonAction");

        viewDataController.resetEntryFilter();
    }

    void handleApplicationExit(WindowEvent event) {
        Log.trace(THIS_CLASS_NAME, "->handleApplicationExit");

        if (!modifiedProperty.get() || confirmLosingChanges(TITLE_SUB_LEAVE)) {
            return;
        }
        event.consume();
    }

    void initAfterDatabaseLoading() {
        databaseMiner = BulkDatabaseMiner.load(databaseObjects);

        initPlugins();

        viewDataController.updateDisplayWithLoadedObjects();

        titleProperty().bindBidirectional(modifiedProperty, new ModifiedFlagToTitleConverter());
        modifiedProperty.setValue(false);

        getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::handleApplicationExit);
    }

    // Visible for testing
    void loadDatabaseFromDirectory(String databaseLocation) {
        if (isServiceRunning() || (modifiedProperty.get() && !confirmLosingChanges(TITLE_SUB_LOAD))) {
            return;
        }

        DatabaseLoader databaseLoader = servicesController.getDatabaseLoader();

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(databaseLoader.messageProperty());

        databaseLoader.bankSupportProperty().setValue(bankSupport);
        databaseLoader.databaseLocationProperty().setValue(databaseLocation);

        Platform.runLater(databaseLoader::restart);
    }

    // Visible for testing
    boolean confirmLosingChanges(String promptSubTitle) {
        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(CONFIRMATION)
                .withTitle(TITLE_APPLICATION + (promptSubTitle == null ? "" : promptSubTitle))
                .withMessage(MESSAGE_LOST_UNSAVED)
                .withDescription(QUESTION_R_U_OK)
                .build();

        return CommonDialogsHelper.showDialog(dialogOptions, getWindow())
                .map(buttonType -> buttonType == OK)
                .orElse(false);
    }

    private void initPlugins() {
        if (!applicationConfiguration.isEditorPluginsEnabled()) {
            Log.info(THIS_CLASS_NAME, "Editor plugins were disabled via application configuration");
            return;
        }
        
        EditorContext editorContext = pluginHandler.getEditorContext();
        editorContext.setDatabaseLocation(servicesController.getDatabaseLoader().databaseLocationProperty().get());
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

    private void browseForDatabaseDirectory() {
        if (isServiceRunning()) {
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

    private void saveDatabaseToDirectory(String databaseLocation) {
        if (isServiceRunning()) {
            return;
        }

        DatabaseSaver databaseSaver = servicesController.getDatabaseSaver();

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
                    try {
                        if (!changeDataController.exportEntriesToPatchFile(currentTopicProperty.getValue(), selectedEntryRefs, selectedEntryFields, location)) {
                            throw new IOException();
                        }

                        String message = selectedEntryRefs.isEmpty() ?
                                DisplayConstants.MESSAGE_ALL_ENTRIES_EXPORTED :
                                DisplayConstants.MESSAGE_ENTRIES_EXPORTED;
                        notifyActionTermination(INFORMATION, TITLE_SUB_EXPORT, message, location);
                    } catch (IOException ioe) {
                        String message = selectedEntryRefs.isEmpty() ?
                                DisplayConstants.MESSAGE_UNABLE_EXPORT_ALL_ENTRIES :
                                DisplayConstants.MESSAGE_UNABLE_EXPORT_ENTRIES;
                        notifyActionTermination(ERROR, TITLE_SUB_EXPORT, message, MESSAGE_SEE_LOGS);
                    }
        });
    }

    private void askForPatchLocationAndImportDataFromFile() {
        dialogsHelper.askForPatchLocation(getWindow())
                .map(File::new)
                .ifPresent(location -> {
                    try {
                        final Optional<String> potentialPropertiesFile = changeDataController.importPatch(location);

                        viewDataController.updateEntriesAndSwitchTo(0);
                        viewDataController.updateAllPropertiesWithItemValues();

                        String writtenPropertiesPath = "";
                        if (potentialPropertiesFile.isPresent()) {
                            writtenPropertiesPath += ("Written properties file: " + System.lineSeparator() + potentialPropertiesFile.get());
                        }

                        notifyActionTermination(INFORMATION, TITLE_SUB_IMPORT, MESSAGE_DATA_IMPORTED, writtenPropertiesPath);
                    } catch (Exception e) {
                        Log.error(THIS_CLASS_NAME, e);

                        notifyActionTermination(ERROR, TITLE_SUB_IMPORT, MESSAGE_UNABLE_IMPORT_PATCH, MESSAGE_SEE_LOGS);
                    }
                });
    }

    private void askForPerformancePackLocationAndImportData() {
        dialogsHelper.askForPerformancePackLocation(getWindow())
                .ifPresent(location -> {
                    try {
                        changeDataController.importPerformancePack(location);
                        viewDataController.updateAllPropertiesWithItemValues();

                        notifyActionTermination(INFORMATION, TITLE_SUB_IMPORT_PERFORMANCE_PACK, MESSAGE_DATA_IMPORTED_PERFORMANCE_PACK, location);
                    } catch (Exception e) {
                        Log.error(THIS_CLASS_NAME, e);

                        notifyActionTermination(INFORMATION, TITLE_SUB_IMPORT_PERFORMANCE_PACK, MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK, MESSAGE_SEE_LOGS);
                    }
                });
    }

    private void askForGenuinePatchLocationAndImportDataFromFile() {
        dialogsHelper.askForGenuinePatchLocation(getWindow())
                .ifPresent(location -> {
                    try {
                        changeDataController.importLegacyPatch(location);
                        viewDataController.updateAllPropertiesWithItemValues();

                        notifyActionTermination(INFORMATION, TITLE_SUB_IMPORT_TDUMT_PATCH, MESSAGE_DATA_IMPORTED_TDUMT_PATCH, location);
                    } catch (Exception e) {
                        Log.error(THIS_CLASS_NAME, e);

                        notifyActionTermination(ERROR, TITLE_SUB_IMPORT_TDUMT_PATCH, MESSAGE_UNABLE_IMPORT_TDUMT_PATCH, MESSAGE_SEE_LOGS);
                    }
                });
    }

    private void askForReferenceAndSwitchToEntry() {
        CommonDialogsHelper.showInputValueDialog(TITLE_SEARCH_CONTENTS_ENTRY, LABEL_SEARCH_ENTRY, getWindow())
                .ifPresent(entryReference -> viewDataController.switchToEntryWithReference(entryReference, currentTopicProperty.getValue()));
    }

    private void resetDatabaseCache(String databaseDirectory) throws IOException {
        DatabaseBanksCacheHelper.clearCache(Paths.get(databaseDirectory));

        notifyActionTermination(INFORMATION, DisplayConstants.TITLE_SUB_RESET_DB_CACHE, DisplayConstants.MESSAGE_DELETED_CACHE, databaseDirectory);
    }

    private void resetSettings() throws IOException {
        applicationConfiguration.reset();

        notifyActionTermination(INFORMATION, DisplayConstants.TITLE_SUB_RESET_SETTINGS, DisplayConstants.MESSAGE_DELETED_SETTINGS, DisplayConstants.MESSAGE_RESTART_APP);
    }

    private void openSettings() {
        DesktopHelper.openInFiles(Paths.get(DIRECTORY_CONFIGURATION));
    }

    private void openLogs() {
        DesktopHelper.openInFiles(Paths.get(DIRECTORY_LOGS));
    }

    private void checkDatabase(String databaseLocation) {
        if (isServiceRunning()) {
            return;
        }

        DatabaseChecker databaseChecker = servicesController.getDatabaseChecker();

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(databaseChecker.messageProperty());

        if (!databaseObjects.isEmpty()) {
            databaseChecker.loadedDatabaseObjectsProperty().setValue(databaseObjects);
        }
        databaseChecker.databaseLocationProperty().setValue(databaseLocation);
        databaseChecker.bankSupportProperty().setValue(bankSupport);
        databaseChecker.restart();
    }

    void fixDatabase() {
        // Do not check for service here, as checker may still be in running state.
        DatabaseFixer databaseFixer = servicesController.getDatabaseFixer();
        DatabaseChecker databaseChecker = servicesController.getDatabaseChecker();

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(databaseFixer.messageProperty());

        databaseFixer.fromService(databaseChecker);
        databaseFixer.restart();
    }

    void notifyActionTermination(Alert.AlertType alertType, String subTitle, String message, String description) {
        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(alertType)
                .withTitle(TITLE_APPLICATION + subTitle)
                .withMessage(message)
                .withDescription(description)
                .build();
        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
    }

    public DbDto getCurrentTopicObject() {
        return this.currentTopicObject;
    }

    public EditorLayoutDto getLayoutObject() {
        return layoutObject;
    }

    public BulkDatabaseMiner getMiner() {
        return databaseMiner;
    }

    public Property<Integer> currentEntryIndexProperty() {
        return currentEntryIndexProperty;
    }

    public MainStageViewDataController getViewData() {
        return viewDataController;
    }

    public MainStageChangeDataController getChangeData() {
        return changeDataController;
    }

    public int getCurrentEntryIndex() {
        return currentEntryIndexProperty.getValue();
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public PluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public BankSupport getBankSupport() {
        return bankSupport;
    }

    protected StringProperty titleProperty() {
        // getter kept for testing
        return super.titleProperty();
    }

    void setCurrentTopicObject(DbDto currentTopicObject) {
        this.currentTopicObject = currentTopicObject;
    }

    void setLayoutObject(EditorLayoutDto layoutObject) {
        this.layoutObject = layoutObject;
    }

    VBox getMainVBox() {
        return mainVBox;
    }

    Deque<EditorLocation> getNavigationHistory() {
        return navigationHistory;
    }

    Property<DbDto.Topic> currentTopicProperty() {
        return currentTopicProperty;
    }

    StringProperty currentEntryLabelProperty() {
        return currentEntryLabelProperty;
    }

    BooleanProperty modifiedProperty() {
        return modifiedProperty;
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

    TextField getEntryFilterTextField() { return entryFilterTextField; }

    ObjectProperty<Cursor> mouseCursorProperty() {
        return rootCursorProperty();
    }

    Button getEntryEmptyFilterButton() {
        return entryEmptyFilterButton;
    }

    Button getEntryFilterButton() {
        return entryFilterButton;
    }

    BooleanProperty runningServiceProperty() {
        return servicesController.runningServiceProperty();
    }

    boolean isServiceRunning() {
        return servicesController.runningServiceProperty().get();
    }

    ImageView getMainSplashImage() {
        return mainSplashImage;
    }

    HBox getMainSplashBox() {
        return mainSplashHBox;
    }
}

package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.helper.DynamicFieldControlsHelper;
import fr.tduf.gui.database.controllers.helper.DynamicLinkControlsHelper;
import fr.tduf.gui.database.converter.CurrentEntryIndexToStringConverter;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.converter.EntryItemsCountToStringConverter;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.stages.EntriesDesigner;
import fr.tduf.gui.database.stages.ResourcesDesigner;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static javafx.scene.control.Alert.AlertType.INFORMATION;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {
    private DynamicFieldControlsHelper dynamicFieldControlsHelper;
    private DynamicLinkControlsHelper dynamicLinkControlsHelper;

    private MainStageViewDataController viewDataController;
    private MainStageChangeDataController changeDataController;
    private ResourcesStageController resourcesStageController;
    private EntriesStageController entriesStageController;

    Property<DbDto.Topic> currentTopicProperty;
    Property<DbResourceDto.Locale> currentLocaleProperty;
    Property<Long> currentEntryIndexProperty;
    SimpleStringProperty currentEntryLabelProperty;
    Property<Integer> entryItemsCountProperty;
    Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();
    Map<TopicLinkDto, ObservableList<RemoteResource>> resourceListByTopicLink = new HashMap<>();

    @FXML
    private Parent root;

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
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        this.viewDataController = new MainStageViewDataController(this);
        this.changeDataController = new MainStageChangeDataController(this);

        this.dynamicFieldControlsHelper = new DynamicFieldControlsHelper(this);
        this.dynamicLinkControlsHelper = new DynamicLinkControlsHelper(this);

        try {
            initSettingsPane();

            initStatusBar();

            initResourcesStageController();

            initEntriesStageController();
        } catch (IOException e) {
            throw new RuntimeException("Window initializing failed.", e);
        }

        initNavigationPane();
    }

    @FXML
    public void handleBrowseDirectoryButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleBrowseDirectoryButtonMouseClick");

        DirectoryChooser directoryChooser = new DirectoryChooser();

        File directory = new File(this.databaseLocationTextField.getText());
        if (directory.exists()) {
            directoryChooser.setInitialDirectory(directory);
        }

        File selectedDirectory = directoryChooser.showDialog(root.getScene().getWindow());

        if (selectedDirectory != null) {
            this.databaseLocationTextField.setText(selectedDirectory.getPath());
        }
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
        if (this.databaseObjects == null || StringUtils.isEmpty(databaseLocation)) {
            return;
        }

        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(this.databaseObjects, databaseLocation);

        Alert alertDialog = new Alert(INFORMATION, databaseLocation, ButtonType.OK);
        alertDialog.setTitle(DisplayConstants.TITLE_APPLICATION);
        alertDialog.setHeaderText(DisplayConstants.MESSAGE_DATABASE_SAVED);
        alertDialog.showAndWait();
    }

    @FXML
    public void handleNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleNextButtonMouseClick");

        long currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex >= currentTopicObject.getData().getEntries().size() - 1) {
            return;
        }

        viewDataController.switchToContentEntry(++currentEntryIndex);
    }

    @FXML
    public void handleFastNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastNextButtonMouseClick");

        long currentEntryIndex = currentEntryIndexProperty.getValue();
        long lastEntryIndex = currentTopicObject.getData().getEntries().size() - 1;
        if (currentEntryIndex + 10 >= lastEntryIndex) {
            currentEntryIndex = lastEntryIndex;
        } else {
            currentEntryIndex += 10;
        }

        viewDataController.switchToContentEntry(currentEntryIndex);
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        long currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex <= 0) {
            return;
        }

        viewDataController.switchToContentEntry(--currentEntryIndex);
    }

    @FXML
    public void handleFastPreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastPreviousButtonMouseClick");

        long currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex - 10 < 0) {
            currentEntryIndex = 0;
        } else {
            currentEntryIndex -= 10;
        }

        this.viewDataController.switchToContentEntry(currentEntryIndex);
    }

    @FXML
    public void handleFirstButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFirstButtonMouseClick");

        this.viewDataController.switchToContentEntry(0);
    }

    @FXML
    public void handleLastButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        viewDataController.switchToContentEntry(this.currentTopicObject.getData().getEntries().size() - 1);
    }

    @FXML
    public void handleBackButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        viewDataController.switchToPreviousLocation();
    }

    @FXML
    public void handleAddEntryButtonAction(ActionEvent actionEvent) {
        System.out.println("handleAddEntryButtonAction");

        addEntryAndUpdateStage();
    }

    @FXML
    public void handleRemoveEntryButtonAction(ActionEvent actionEvent) {
        System.out.println("handleRemoveEntryButtonAction");

        removeCurrentEntryAndUpdateStage();
    }

    public EventHandler<ActionEvent> handleBrowseResourcesButtonMouseClick(DbDto.Topic targetTopic, SimpleStringProperty targetReferenceProperty, int fieldRank) {
        return (actionEvent) -> {
            System.out.println("browseResourcesButton clicked");

            resourcesStageController.initAndShowDialog(targetReferenceProperty, fieldRank, getLocalesChoiceBox().getValue(), targetTopic);
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

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName) {
        return (actionEvent) -> {
            System.out.println("gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            this.viewDataController.switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
        };
    }

    public EventHandler<ActionEvent> handleAddLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            System.out.println("handleAddLinkedEntryButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            Optional<RemoteResource> potentialSelectedEntry = Optional.empty();
            DbDto.Topic finalTopic = targetTopic;
            if (databaseMiner.getUidFieldRank(targetTopic).isPresent()) {
                // Association topic -> browse remote entries
                finalTopic = topicLinkObject.getTopic();
                potentialSelectedEntry = entriesStageController.initAndShowModalDialog(targetTopic, targetProfileName);
            }
            addLinkedEntryAndUpdateStage(tableViewSelectionModel, finalTopic, potentialSelectedEntry, topicLinkObject);
        };
    }

    public EventHandler<ActionEvent> handleRemoveLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return (actionEvent) -> {
            System.out.println("handleRemoveLinkedEntryButton clicked");

            RemoteResource selectedItem = tableViewSelectionModel.getSelectedItem();
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
                Optional<RemoteResource> selectedResource = TableViewHelper.getMouseSelectedItem(event);
                if (selectedResource.isPresent()) {
                    this.viewDataController.switchToSelectedResourceForLinkedTopic(selectedResource.get(), targetTopic, targetProfileName);
                }
            }
        };
    }

    public ChangeListener<Boolean> handleTextFieldFocusChange(int fieldRank, SimpleStringProperty fieldValueProperty) {
        return (observable, oldFocusState, newFocusState) -> {
            System.out.println("handleTextFieldFocusChange, focused=" + newFocusState + ", fieldRank=" + fieldRank + ", fieldValue=" + fieldValueProperty.get());

            if (oldFocusState && !newFocusState) {
                this.changeDataController.updateContentItem(currentTopicObject.getTopic(), fieldRank, fieldValueProperty.get());
            }
        };
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        initTabPane(newProfileName);
    }

    private void handleLocaleChoiceChanged(DbResourceDto.Locale newLocale) {
        System.out.println("handleLocaleChoiceChanged: " + newLocale.name());

        viewDataController.updateAllPropertiesWithItemValues();
    }

    private void initResourcesStageController() throws IOException {
        Stage resourcesStage = new Stage();
        Platform.runLater(() -> resourcesStage.initOwner(root.getScene().getWindow())); // runLater() ensures main stage will be initialized first.

        resourcesStageController = ResourcesDesigner.init(resourcesStage);
        resourcesStageController.setMainStageController(this);
    }

    private void initEntriesStageController() throws IOException {
        Stage entriesStage = new Stage();
        Platform.runLater(() -> entriesStage.initOwner(root.getScene().getWindow())); // runLater() ensures main stage will be initialized first.

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

        databaseLocationTextField.setText(SettingsConstants.DATABASE_DIRECTORY_DEFAULT);
    }

    private void initStatusBar() {
        statusLabel.setText(DisplayConstants.LABEL_STATUS_VERSION);
    }

    private void initNavigationPane() {
        currentTopicProperty = new SimpleObjectProperty<>();
        entryItemsCountProperty = new SimpleObjectProperty<>(-1);
        currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);
        currentEntryLabelProperty = new SimpleStringProperty("");

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
        entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new CurrentEntryIndexToStringConverter());
        entryItemsCountLabel.textProperty().bindBidirectional(entryItemsCountProperty, new EntryItemsCountToStringConverter());
        currentEntryLabel.textProperty().bindBidirectional(currentEntryLabelProperty);
    }

    private void initTabPane(String profileName) {
        if (databaseObjects.isEmpty()) {
            return;
        }

        profileObject = EditorLayoutHelper.getAvailableProfileByName(profileName, layoutObject);
        currentTopicObject = databaseMiner.getDatabaseTopic(profileObject.getTopic()).get();

        rawValuePropertyByFieldRank.clear();
        resolvedValuePropertyByFieldRank.clear();
        resourceListByTopicLink.clear();

        initGroupTabs();

        addDynamicControls();

        viewDataController.updateAllPropertiesWithItemValues();
    }

    private void initGroupTabs() {
        currentTopicProperty.setValue(currentTopicObject.getTopic());
        currentEntryIndexProperty.setValue(0L);
        entryItemsCountProperty.setValue(currentTopicObject.getData().getEntries().size());
        rawValuePropertyByFieldRank.clear();
        resolvedValuePropertyByFieldRank.clear();

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

    private void loadDatabaseFromDirectory(String databaseLocation) {
        databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
        if (!databaseObjects.isEmpty()) {
            databaseMiner = BulkDatabaseMiner.load(this.databaseObjects);

            profilesChoiceBox.getSelectionModel().selectFirst();

            navigationHistory.clear();
        }
    }

    private void addEntryAndUpdateStage() {
        long newEntryIndex = changeDataController.addEntryForCurrentTopic();

        viewDataController.updateEntryCountAndSwitchToEntry(newEntryIndex);
    }

    private void addLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, DbDto.Topic targetTopic, Optional<RemoteResource> potentialLinkedEntry, TopicLinkDto topicLinkObject) {
        String sourceEntryRef = databaseMiner.getContentEntryRefWithInternalIdentifier(currentEntryIndexProperty.getValue(), currentTopicProperty.getValue()).get();
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

        // TODO handle cases of deleting first item or unique item
        viewDataController.updateEntryCountAndSwitchToEntry(currentEntryIndex - 1);
    }

    private void removeLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        RemoteResource selectedItem = tableViewSelectionModel.getSelectedItem();

        changeDataController.removeEntryWithIdentifier(selectedItem.getInternalEntryId(), topicLinkObject.getTopic());

        viewDataController.updateLinkProperties(topicLinkObject);

        TableViewHelper.selectRowAndScroll(initialRowIndex, tableViewSelectionModel.getTableView());
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

    public Map<TopicLinkDto, ObservableList<RemoteResource>> getResourceListByTopicLink() {
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

    ChoiceBox<DbResourceDto.Locale> getLocalesChoiceBox() {
        return localesChoiceBox;
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
}
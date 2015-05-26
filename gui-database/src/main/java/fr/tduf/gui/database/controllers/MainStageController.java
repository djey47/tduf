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
import fr.tduf.gui.database.stages.ResourcesDesigner;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
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

    private ViewDataController viewDataController;
    private ChangeDataController changeDataController;
    private ResourcesStageController resourcesStageController;

    @FXML
    private Parent root;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private Label currentTopicLabel;

    @FXML
    private Label currentEntryLabel;

    @FXML
    private ChoiceBox<DbResourceDto.Locale> localesChoiceBox;

    @FXML
    private ChoiceBox<String> profilesChoiceBox;

    @FXML
    private TabPane tabPane;

    @FXML
    private VBox defaultTab;

    @FXML
    private TextField databaseLocationTextField;

    @FXML
    private TextField entryNumberTextField;

    @FXML
    private Label entryItemsCountLabel;

    private Map<String, VBox> tabContentByName = new HashMap<>();

    private List<DbDto> databaseObjects = new ArrayList<>();
    private DbDto currentTopicObject;

    private EditorLayoutDto layoutObject;
    private EditorLayoutDto.EditorProfileDto profileObject;
    private BulkDatabaseMiner databaseMiner;

    private Map<TopicLinkDto, ObservableList<RemoteResource>> resourceListByTopicLink = new HashMap<>();

    private Stack<EditorLocation> navigationHistory = new Stack<>();

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        this.viewDataController = new ViewDataController(this);
        this.changeDataController = new ChangeDataController(this);

        this.dynamicFieldControlsHelper = new DynamicFieldControlsHelper(this);
        this.dynamicLinkControlsHelper = new DynamicLinkControlsHelper(this);

        try {
            initSettingsPane();

            initResourcesStageController();
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

        long currentEntryIndex = this.viewDataController.getCurrentEntryIndexProperty().getValue();
        if (currentEntryIndex >= this.currentTopicObject.getData().getEntries().size() - 1) {
            return;
        }

        this.viewDataController.switchToContentEntry(++currentEntryIndex);
    }

    @FXML
    public void handleFastNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastNextButtonMouseClick");

        long currentEntryIndex = this.viewDataController.getCurrentEntryIndexProperty().getValue();
        long lastEntryIndex = this.currentTopicObject.getData().getEntries().size() - 1;
        if (currentEntryIndex + 10 >= lastEntryIndex) {
            currentEntryIndex = lastEntryIndex;
        } else {
            currentEntryIndex += 10;
        }

        this.viewDataController.switchToContentEntry(currentEntryIndex);
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        long currentEntryIndex = this.viewDataController.getCurrentEntryIndexProperty().getValue();
        if (currentEntryIndex <= 0) {
            return;
        }

        this.viewDataController.switchToContentEntry(--currentEntryIndex);
    }

    @FXML
    public void handleFastPreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastPreviousButtonMouseClick");

        long currentEntryIndex = this.viewDataController.getCurrentEntryIndexProperty().getValue();
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

        this.viewDataController.switchToContentEntry(this.currentTopicObject.getData().getEntries().size() - 1);
    }

    @FXML
    public void handleBackButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        this.viewDataController.switchToPreviousLocation();
    }

    public EventHandler<ActionEvent> handleBrowseResourcesButtonMouseClick(DbDto.Topic targetTopic, SimpleStringProperty targetReferenceProperty, int fieldRank) {
        return (actionEvent) -> {
            System.out.println("browseResourcesButton clicked");

            this.resourcesStageController.initAndShowDialog(targetReferenceProperty, fieldRank, getLocalesChoiceBox().getValue(), targetTopic);
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(DbDto.Topic targetTopic, int fieldRank, String targetProfileName) {
        return (actionEvent) -> {
            System.out.println("gotoReferenceButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            DbDataDto.Entry remoteContentEntry = this.databaseMiner.getRemoteContentEntryWithInternalIdentifier(this.currentTopicObject.getTopic(), fieldRank, this.viewDataController.getCurrentEntryIndexProperty().getValue(), targetTopic).get();
            this.viewDataController.switchToProfileAndEntry(targetProfileName, remoteContentEntry.getId(), true);
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(DbDto.Topic targetTopic, TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, String targetProfileName) {
        return (actionEvent) -> {
            System.out.println("gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            this.viewDataController.switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
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

        this.viewDataController.updateAllPropertiesWithItemValues();
    }

    private void initResourcesStageController() throws IOException {
        this.resourcesStageController = ResourcesDesigner.init(new Stage());
        this.resourcesStageController.setMainStageController(this);
    }

    private void initSettingsPane() throws IOException {
        this.settingsPane.setExpanded(false);

        this.viewDataController.fillLocales();
        this.localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue)));

        this.viewDataController.loadAndFillProfiles();
        this.profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

        this.databaseLocationTextField.setText(SettingsConstants.DATABASE_DIRECTORY_DEFAULT);
    }

    private void initNavigationPane() {
        this.viewDataController.initNavViewDataProperties();

        this.currentTopicLabel.textProperty().bindBidirectional(this.viewDataController.getCurrentTopicProperty(), new DatabaseTopicToStringConverter());
        this.entryNumberTextField.textProperty().bindBidirectional(this.viewDataController.getCurrentEntryIndexProperty(), new CurrentEntryIndexToStringConverter());
        this.entryItemsCountLabel.textProperty().bindBidirectional(this.viewDataController.getEntryItemsCountProperty(), new EntryItemsCountToStringConverter());
        this.currentEntryLabel.textProperty().bindBidirectional(this.viewDataController.getCurrentEntryLabelProperty());
    }

    private void initTabPane(String profileName) {
        if (databaseObjects.isEmpty()) {
            return;
        }

        this.profileObject = EditorLayoutHelper.getAvailableProfileByName(profileName, this.layoutObject);
        this.currentTopicObject = databaseMiner.getDatabaseTopic(profileObject.getTopic()).get();

        this.getRawValuePropertyByFieldRank().clear();
        this.getResolvedValuePropertyByFieldRank().clear();
        this.resourceListByTopicLink.clear();

        this.viewDataController.initTabViewDataProperties();

        initGroupTabs();

        addDynamicControls();

        this.viewDataController.updateAllPropertiesWithItemValues();
    }

    private void initGroupTabs() {
        this.defaultTab.getChildren().clear();

        this.tabPane.getTabs().remove(1, this.tabPane.getTabs().size());
        tabContentByName.clear();

        if (this.profileObject.getGroups() != null) {
            this.profileObject.getGroups().forEach((groupName) -> {
                VBox vbox = new VBox();
                Tab groupTab = new Tab(groupName, new ScrollPane(vbox));

                this.tabPane.getTabs().add(this.tabPane.getTabs().size(), groupTab);

                tabContentByName.put(groupName, vbox);
            });
        }
    }

    private void addDynamicControls() {
        if (this.profileObject.getFieldSettings() != null) {
            dynamicFieldControlsHelper.addAllFieldsControls(
                    this.layoutObject,
                    this.profilesChoiceBox.getValue(),
                    this.currentTopicObject.getTopic());
        }

        if (this.profileObject.getTopicLinks() != null) {
            dynamicLinkControlsHelper.addAllLinksControls(
                    this.profileObject);
        }
    }

    private void loadDatabaseFromDirectory(String databaseLocation) {
        this.databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
        if (!this.databaseObjects.isEmpty()) {
            this.databaseMiner = BulkDatabaseMiner.load(this.databaseObjects);

            this.profilesChoiceBox.getSelectionModel().selectFirst();

            this.navigationHistory.clear();
        }
    }

    public ChoiceBox<String> getProfilesChoiceBox() {
        return this.profilesChoiceBox;

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

    public Map<Integer, SimpleStringProperty> getRawValuePropertyByFieldRank() {
        return viewDataController.getRawValuePropertyByFieldRank();
    }

    public Map<Integer, SimpleStringProperty> getResolvedValuePropertyByFieldRank() {
        return viewDataController.getResolvedValuePropertyByFieldRank();
    }

    public Map<TopicLinkDto, ObservableList<RemoteResource>> getResourceListByTopicLink() {
        return resourceListByTopicLink;
    }

    public long getCurrentEntryIndex() {
        return this.viewDataController.getCurrentEntryIndexProperty().getValue();
    }

    public BulkDatabaseMiner getDatabaseMiner() {
        return databaseMiner;
    }

    BulkDatabaseMiner getMiner() {
        return this.databaseMiner;
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

    TabPane getTabPane() {
        return tabPane;
    }

    ViewDataController getViewDataController() {
        return viewDataController;
    }

    ChangeDataController getChangeDataController() {
        return changeDataController;
    }
}
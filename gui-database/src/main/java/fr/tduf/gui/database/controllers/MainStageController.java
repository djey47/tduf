package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.converter.CurrentEntryIndexToStringConverter;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.converter.EntryItemsCountToStringConverter;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.helper.EditorLayoutHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    static final String PATH_RESOURCE_PROFILES = "/layout/defaultProfiles.json";

    static final String VALUE_UNKNOWN = "<?>";
    static final String SEPARATOR_VALUES_LABEL = " - ";

    private static final String LABEL_BUTTON_GOTO = "->";
    private static final String COLUMN_HEADER_REF = "#";
    private static final String COLUMN_HEADER_DATA = "Linked data";

    private static final String CSS_CLASS_FIELD_LABEL = "fieldLabel";
    private static final String CSS_CLASS_FIELD_NAME = "fieldName";
    private static final String CSS_CLASS_READONLY_FIELD = "readonlyField";

    private ViewDataController viewDataController;

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

        try {
            // TODO DEBUG
            databaseLocationTextField.setText("/media/sf_DevStore/GIT/tduf/cli/integ-tests/db-json/");

            initSettingsPane();

            initNavigationPane();

        } catch (IOException e) {
            throw new RuntimeException("Window initializing failed.", e);
        }
    }

    @FXML
    public void handleLoadButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLoadButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (StringUtils.isNotEmpty(databaseLocation)) {
            this.databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
            this.databaseMiner = BulkDatabaseMiner.load(this.databaseObjects);

            profilesChoiceBox.setValue(profilesChoiceBox.getItems().get(0));

            navigationHistory.clear();
        }
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
        if (currentEntryIndex <= 0 ) {
            return;
        }

        this.viewDataController.switchToContentEntry(--currentEntryIndex);
    }

    @FXML
    public void handleFastPreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFastPreviousButtonMouseClick");

        long currentEntryIndex = this.viewDataController.getCurrentEntryIndexProperty().getValue();
        if (currentEntryIndex - 10 < 0 ) {
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

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        initTabPane(newProfileName);
    }

    private void handleLocaleChoiceChanged(DbResourceDto.Locale newLocale) {
        System.out.println("handleLocaleChoiceChanged: " + newLocale.name());

        this.viewDataController.updateAllPropertiesWithItemValues();
    }

    private void initSettingsPane() throws IOException {
        this.settingsPane.setExpanded(false);

        this.viewDataController.fillLocales();
        this.localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue)));

        this.viewDataController.loadAndFillProfiles();
        this.profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

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
        this.resourceListByTopicLink.clear();

        this.viewDataController.initTabViewDataProperties();

        initGroupTabs();

        addAllFieldsControls();

        addAllLinksControls();

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

    private void addAllFieldsControls() {
        if (this.profileObject.getFieldSettings() == null) {
            return;
        }

        this.currentTopicObject.getStructure().getFields().stream()

                .sorted((structureField1, structureField2) -> Integer.compare(
                        EditorLayoutHelper.getFieldPrioritySettingByRank(structureField2.getRank(), this.profilesChoiceBox.getValue(), this.layoutObject),
                        EditorLayoutHelper.getFieldPrioritySettingByRank(structureField1.getRank(), this.profilesChoiceBox.getValue(), this.layoutObject)))

                .forEach(this::addFieldControls);
    }

    private void addAllLinksControls() {
        if (this.profileObject.getTopicLinks() == null) {
            return;
        }

        this.profileObject.getTopicLinks().stream()

                .sorted((topicLinkObject1, topicLinkObject2) -> Integer.compare(topicLinkObject2.getPriority(), topicLinkObject1.getPriority()))

                .forEach(this::addLinkControls);
    }

    private void addFieldControls(DbStructureDto.Field field) {
        SimpleStringProperty property = new SimpleStringProperty("");
        this.viewDataController.getRawValuePropertyByFieldRank().put(field.getRank(), property);

        String fieldName = field.getName();
        boolean fieldReadOnly = false;
        String groupName = null;
        Optional<String> potentialToolTip = Optional.empty();
        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(field.getRank(), profilesChoiceBox.getValue(), this.layoutObject);
        if(potentialFieldSettings.isPresent()) {
            FieldSettingsDto fieldSettings = potentialFieldSettings.get();

            if (fieldSettings.isHidden()) {
                return;
            }

            if (fieldSettings.getLabel() != null) {
                fieldName = fieldSettings.getLabel();
            }

            fieldReadOnly = fieldSettings.isReadOnly();

            groupName = fieldSettings.getGroup();

            potentialToolTip = Optional.ofNullable(fieldSettings.getToolTip());
        }

        HBox fieldBox = addFieldBox(Optional.ofNullable(groupName), 25.0);

        addFieldLabel(fieldBox, fieldReadOnly, fieldName);

        addTextField(fieldBox, fieldReadOnly, property, potentialToolTip);

        if (field.isAResourceField()) {
            DbDto.Topic topic = currentTopicObject.getTopic();
            if (field.getTargetRef() != null) {
                topic = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            }

            addResourceValueControls(fieldBox, field.getRank(), topic);
        }

        // TODO handle bitfield -> requires resolver (0.7.0+)

        if (DbStructureDto.FieldType.REFERENCE == field.getFieldType()) {
            DbDto.Topic topic = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            addReferenceValueControls(fieldBox, field.getRank(), topic);
        }
    }

    private void addLinkControls(TopicLinkDto topicLinkObject) {
        ObservableList<RemoteResource> resourceData = FXCollections.observableArrayList();
        resourceListByTopicLink.put(topicLinkObject, resourceData);

        HBox fieldBox = addFieldBox(Optional.ofNullable(topicLinkObject.getGroup()), 250.0);

        addFieldLabelForLinkedTopic(fieldBox, topicLinkObject);

        String targetProfileName = topicLinkObject.getRemoteReferenceProfile();
        DbDto.Topic targetTopic = retrieveTargetTopicForLink(topicLinkObject);
        TableView<RemoteResource> tableView = addTableViewForLinkedTopic(fieldBox, resourceData, targetProfileName, targetTopic);

        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));

        addCustomLabel(fieldBox, targetTopic.name());

        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));

        if (targetProfileName != null) {
            addGoToReferenceButtonForLinkedTopic(fieldBox, targetTopic, tableView.getSelectionModel(), targetProfileName);
        }
    }

    private void addResourceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic topic) {
        SimpleStringProperty property = new SimpleStringProperty("");
        this.viewDataController.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        addResourceValueLabel(fieldBox, property);

        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));

        addCustomLabel(fieldBox, topic.name());
    }

    private void addResourceValueLabel(HBox fieldBox, SimpleStringProperty property) {
        Label resourceValueLabel = new Label();
        resourceValueLabel.setPrefWidth(450);
        resourceValueLabel.getStyleClass().add(CSS_CLASS_FIELD_LABEL);
        resourceValueLabel.textProperty().bindBidirectional(property);
        fieldBox.getChildren().add(resourceValueLabel);
    }

    private void addReferenceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic targetTopic) {
        SimpleStringProperty property = new SimpleStringProperty("Reference to another topic.");
        this.viewDataController.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        Label remoteValueLabel = addCustomLabel(fieldBox, VALUE_UNKNOWN);
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.textProperty().bindBidirectional(property);

        addCustomLabel(fieldBox, targetTopic.name());

        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));

        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRank(fieldRank, this.profileObject);
        if (potentialFieldSettings.isPresent() && potentialFieldSettings.get().getRemoteReferenceProfile() != null) {
            addGoToReferenceButton(fieldBox, fieldRank, targetTopic, potentialFieldSettings.get().getRemoteReferenceProfile());
        }
    }

    private HBox addFieldBox(Optional<String> potentialGroupName, double boxHeight) {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(boxHeight);
        fieldBox.setPadding(new Insets(5.0));

        if (potentialGroupName.isPresent()) {
            String groupName = potentialGroupName.get();
            if (!tabContentByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Unknown group name: " + groupName);
            }
            tabContentByName.get(groupName).getChildren().add(fieldBox);
        } else {
            defaultTab.getChildren().add(fieldBox);
        }
        return fieldBox;
    }

    private Label addCustomLabel(HBox fieldBox, String text) {
        Label customLabel = new Label(text);
        customLabel.getStyleClass().add(CSS_CLASS_FIELD_LABEL);
        fieldBox.getChildren().add(customLabel);
        return customLabel;
    }

    private void addFieldLabel(HBox fieldBox, boolean readOnly, String fieldName) {
        Label fieldNameLabel = new Label(fieldName);

        fieldNameLabel.getStyleClass().add(CSS_CLASS_FIELD_NAME);
        if (readOnly) {
            fieldNameLabel.getStyleClass().add(CSS_CLASS_READONLY_FIELD);
        }
        fieldNameLabel.setPrefWidth(225.0);
        fieldBox.getChildren().add(fieldNameLabel);
    }

    private void addFieldLabelForLinkedTopic(HBox fieldBox, TopicLinkDto topicLinkObject) {
        String fieldName = topicLinkObject.getTopic().name();
        if (topicLinkObject.getLabel() != null) {
            fieldName = topicLinkObject.getLabel();
        }
        addFieldLabel(fieldBox, false, fieldName);
    }

    private void addTextField(HBox fieldBox, boolean readOnly, Property<String> property, Optional<String> toolTip) {
        TextField fieldValue = new TextField();

        if (readOnly) {
            fieldValue.getStyleClass().add(CSS_CLASS_READONLY_FIELD);
        }
        fieldValue.setPrefWidth(110.0);
        fieldValue.setEditable(!readOnly);
        if (toolTip.isPresent()) {
            fieldValue.setTooltip(new Tooltip(toolTip.get()));
        }

        fieldValue.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(fieldValue);
    }

    private void addGoToReferenceButton(HBox fieldBox, int fieldRank, DbDto.Topic targetTopic, String targetProfileName) {
        Button gotoReferenceButton = new Button(LABEL_BUTTON_GOTO);
        gotoReferenceButton.setOnAction((actionEvent) -> {
            System.out.println("gotoReferenceButton clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            DbDataDto.Entry remoteContentEntry = this.databaseMiner.getRemoteContentEntryWithInternalIdentifier(this.currentTopicObject.getTopic(), fieldRank, this.viewDataController.getCurrentEntryIndexProperty().getValue(), targetTopic).get();
            this.viewDataController.switchToProfileAndEntry(targetProfileName, remoteContentEntry.getId(), true);
        });
        fieldBox.getChildren().add(gotoReferenceButton);
    }

    private void addGoToReferenceButtonForLinkedTopic(HBox fieldBox, DbDto.Topic targetTopic, TableView.TableViewSelectionModel<RemoteResource> tableViewSelectionModel, String targetProfileName) {
        Button gotoReferenceButton = new Button(LABEL_BUTTON_GOTO);
        gotoReferenceButton.setOnAction((actionEvent) -> {
            System.out.println("gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + ", targetProfileName:" + targetProfileName);

            this.viewDataController.switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
        });
        fieldBox.getChildren().add(gotoReferenceButton);
    }

    private TableView<RemoteResource> addTableViewForLinkedTopic(HBox fieldBox, ObservableList<RemoteResource> resourceData, String targetProfileName, DbDto.Topic targetTopic) {
        TableView<RemoteResource> tableView = new TableView<>();
        tableView.setPrefWidth(555);

        TableColumn<RemoteResource, String> refColumn = new TableColumn<>(COLUMN_HEADER_REF);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());
        refColumn.setPrefWidth(100);

        TableColumn<RemoteResource, String> valueColumn = new TableColumn<>(COLUMN_HEADER_DATA);
        valueColumn.setCellValueFactory((cellData) -> cellData.getValue().valueProperty());
        valueColumn.setPrefWidth(455);

        tableView.getColumns().add(refColumn);
        tableView.getColumns().add(valueColumn);

        tableView.setItems(resourceData);

        if(targetProfileName != null) {
            final DbDto.Topic finalTargetTopic = targetTopic;
            tableView.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    Optional<RemoteResource> selectedResource = TableViewHelper.getMouseSelectedItem(event);
                    if (selectedResource.isPresent()) {
                        this.viewDataController.switchToSelectedResourceForLinkedTopic(selectedResource.get(), finalTargetTopic, targetProfileName);
                    }
                }
            });
        }

        fieldBox.getChildren().add(tableView);

        return tableView;
    }

    private DbDto.Topic retrieveTargetTopicForLink(TopicLinkDto topicLinkObject) {
        List<DbStructureDto.Field> structureFields = databaseMiner.getDatabaseTopic(topicLinkObject.getTopic()).get().getStructure().getFields();
        DbDto.Topic targetTopic = topicLinkObject.getTopic();
        if (structureFields.size() == 2) {
            String targetRef = structureFields.get(1).getTargetRef();
            if (targetRef != null) {
                targetTopic = databaseMiner.getDatabaseTopicFromReference(targetRef).getTopic();
            }
        }
        return targetTopic;
    }

    BulkDatabaseMiner getMiner() {
        return this.databaseMiner;
    }

    ChoiceBox<String> getProfilesChoiceBox() {
        return this.profilesChoiceBox;
    }

    DbDto getCurrentTopicObject() {
        return this.currentTopicObject;
    }

    EditorLayoutDto.EditorProfileDto getCurrentProfileObject() {
        return profileObject;
    }

    Map<TopicLinkDto, ObservableList<RemoteResource>> getResourceListByTopicLink() {
        return resourceListByTopicLink;
    }

    EditorLayoutDto getLayoutObject() {
        return layoutObject;
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
}
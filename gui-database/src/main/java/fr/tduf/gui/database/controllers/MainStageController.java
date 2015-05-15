package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.converter.CurrentEntryIndexToStringConverter;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.converter.EntryItemsCountToStringConverter;
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
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
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
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    private static final Class<MainStageController> thisClass = MainStageController.class;

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

    private Property<DbDto.Topic> currentTopicProperty;
    private Property<DbResourceDto.Locale> currentLocaleProperty;
    private Property<Long> currentEntryIndexProperty;
    private Property<Integer> entryItemsCountProperty;
    private SimpleStringProperty currentEntryLabelProperty;

    private Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    private Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();
    private Map<TopicLinkDto, ObservableList<RemoteResource>> resourceListByTopicLink = new HashMap<>();


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            // DEBUG
            databaseLocationTextField.setText("/media/DevStore/GIT/tduf/cli/integ-tests/db-json/");

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
        }
    }

    @FXML
    public void handleNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleNextButtonMouseClick");

        long currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex >= this.currentTopicObject.getData().getEntries().size() - 1) {
            return;
        }

        switchToContentEntry(++currentEntryIndex);
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        long currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex <= 0 ) {
            return;
        }

        switchToContentEntry(--currentEntryIndex);
    }

    @FXML
    public void handleFirstButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFirstButtonMouseClick");

        switchToContentEntry(0);
    }

    @FXML
    public void handleLastButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        switchToContentEntry(this.currentTopicObject.getData().getEntries().size() - 1);
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        initTabPane(newProfileName);
    }

    private void handleLocaleChoiceChanged(DbResourceDto.Locale newLocale) {
        System.out.println("handleLocaleChoiceChanged: " + newLocale.name());

        updateAllPropertiesWithItemValues();
    }

    private void initSettingsPane() throws IOException {

        this.settingsPane.setExpanded(false);

        fillLocales();
        this.localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue)));

        loadAndFillProfiles();
        this.profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

    }

    private void initNavigationPane() {
        this.currentTopicProperty = new SimpleObjectProperty<>();
        this.currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());

        this.entryItemsCountProperty = new SimpleObjectProperty<>(-1);
        this.entryItemsCountLabel.textProperty().bindBidirectional(entryItemsCountProperty, new EntryItemsCountToStringConverter());

        this.currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);
        this.entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new CurrentEntryIndexToStringConverter());

        this.currentEntryLabelProperty = new SimpleStringProperty("");
        this.currentEntryLabel.textProperty().bindBidirectional(currentEntryLabelProperty);
    }

    private void initTabPane(String profileName) {
        if (databaseObjects.isEmpty()) {
            return;
        }

        this.profileObject = EditorLayoutHelper.getAvailableProfileByName(profileName, this.layoutObject);

        this.currentTopicObject = databaseMiner.getDatabaseTopic(profileObject.getTopic()).get();
        this.currentTopicProperty.setValue(this.currentTopicObject.getTopic());

        this.currentEntryIndexProperty.setValue(0L);
        this.entryItemsCountProperty.setValue(this.currentTopicObject.getData().getEntries().size());

        this.rawValuePropertyByFieldRank.clear();
        this.resolvedValuePropertyByFieldRank.clear();
        this.resourceListByTopicLink.clear();

        initGroupTabs();

        addAllFieldsControls();

        addAllLinksControls();

        updateAllPropertiesWithItemValues();
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

    private void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> this.localesChoiceBox.getItems().add(locale));

        this.currentLocaleProperty = new SimpleObjectProperty<>(DbResourceDto.Locale.UNITED_STATES);
        this.localesChoiceBox.valueProperty().bindBidirectional(this.currentLocaleProperty);
    }

    private void loadAndFillProfiles() throws IOException {
        this.layoutObject = new ObjectMapper().readValue(thisClass.getResource("/layout/defaultProfiles.json"), EditorLayoutDto.class);
        this.layoutObject.getProfiles()
                .forEach((profileObject) -> profilesChoiceBox.getItems().add(profileObject.getName()));
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
        rawValuePropertyByFieldRank.put(field.getRank(), property);

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

        HBox fieldBox = addFieldBox(Optional.ofNullable(groupName));

        addFieldLabel(fieldBox, fieldReadOnly, fieldName);

        addTextField(fieldBox, fieldReadOnly, property, potentialToolTip);

        if (field.isAResourceField()) {
            DbDto.Topic topic = currentTopicObject.getTopic();
            if (field.getTargetRef() != null) {
                topic = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            }

            addResourceValueControls(fieldBox, field.getRank(), topic);
        }

        if (DbStructureDto.FieldType.REFERENCE == field.getFieldType()) {
            DbDto.Topic topic = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            addReferenceValueControls(fieldBox, field.getRank(), topic);
        }
    }

    private void addLinkControls(TopicLinkDto topicLinkDto) {
        HBox fieldBox = addFieldBox(Optional.ofNullable(topicLinkDto.getGroup()));
        fieldBox.setPrefHeight(250);

        String fieldName = topicLinkDto.getTopic().name();
        if (topicLinkDto.getLabel() != null) {
            fieldName = topicLinkDto.getLabel();
        }
        addFieldLabel(fieldBox, false, fieldName);

        ObservableList<RemoteResource> resourceData = FXCollections.observableArrayList();
        resourceListByTopicLink.put(topicLinkDto, resourceData);

        TableView<RemoteResource> tableView = new TableView<>();
        tableView.setPrefWidth(450);

        TableColumn<RemoteResource, String> refColumn = new TableColumn<>();
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());
        refColumn.setPrefWidth(100);

        TableColumn<RemoteResource, String> valueColumn = new TableColumn<>();
        valueColumn.setCellValueFactory((cellData) -> cellData.getValue().valueProperty());
        valueColumn.setPrefWidth(400);

        tableView.getColumns().add(refColumn);
        tableView.getColumns().add(valueColumn);

        tableView.setItems(resourceData);

        fieldBox.getChildren().add(tableView);
    }

    private HBox addFieldBox(Optional<String> groupName) {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(25.0);
        fieldBox.setPadding(new Insets(5.0));

        if (groupName.isPresent()) {
            VBox groupTab = tabContentByName.get(groupName.get());
            groupTab.getChildren().add(fieldBox);
        } else {
            defaultTab.getChildren().add(fieldBox);
        }

        return fieldBox;
    }

    private void addFieldLabel(HBox fieldBox, boolean readOnly, String fieldName) {
        Label fieldNameLabel = new Label(fieldName);

        fieldNameLabel.getStyleClass().add("fieldName");
        if (readOnly) {
            fieldNameLabel.getStyleClass().add("readonlyField");
        }
        fieldNameLabel.setPrefWidth(225.0);
        fieldBox.getChildren().add(fieldNameLabel);
    }

    private void addTextField(HBox fieldBox, boolean readOnly, Property<String> property, Optional<String> toolTip) {
        TextField fieldValue = new TextField();

        if (readOnly) {
            fieldValue.getStyleClass().add("readonlyField");
        }
        fieldValue.setPrefWidth(110.0);
        fieldValue.setEditable(!readOnly);
        if (toolTip.isPresent()) {
            fieldValue.setTooltip(new Tooltip(toolTip.get()));
        }

        fieldValue.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(fieldValue);
    }

    private void addResourceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic topic) {
        Label resourceValueLabel = new Label();
        resourceValueLabel.setPrefWidth(450);
        resourceValueLabel.getStyleClass().add("fieldLabel");

        SimpleStringProperty property = new SimpleStringProperty("");
        resolvedValuePropertyByFieldRank.put(fieldRank, property);
        resourceValueLabel.textProperty().bindBidirectional(property);

        Label resourceTopicLabel = new Label(topic.name());
        resourceTopicLabel.getStyleClass().add("fieldLabel");

        fieldBox.getChildren().add(resourceValueLabel);
        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));
        fieldBox.getChildren().add(resourceTopicLabel);
    }

    private void addReferenceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic targetTopic) {
        Label remoteValueLabel = new Label();
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.getStyleClass().add("fieldLabel");

        SimpleStringProperty property = new SimpleStringProperty("Reference to another topic.");
        resolvedValuePropertyByFieldRank.put(fieldRank, property);
        remoteValueLabel.textProperty().bindBidirectional(property);

        Label resourceTopicLabel = new Label(targetTopic.name());
        resourceTopicLabel.getStyleClass().add("fieldLabel");

        Button gotoReferenceButton = new Button("->");
        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRank(fieldRank, this.profileObject);
        if (potentialFieldSettings.isPresent()) {
            gotoReferenceButton.setOnAction((actionEvent) -> {
                System.out.println("gotoReferenceButton clicked");

                String profileName = potentialFieldSettings.get().getRemoteReferenceProfile();
                DbDataDto.Entry remoteContentEntry = this.databaseMiner.getRemoteContentEntryWithInternalIdentifier(this.currentTopicObject.getTopic(), fieldRank, currentEntryIndexProperty.getValue(), targetTopic).get();
                switchToProfileAndEntry(profileName, remoteContentEntry.getId());
            });
        } else {
            gotoReferenceButton.setDisable(true);
        }

        fieldBox.getChildren().add(remoteValueLabel);
        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));
        fieldBox.getChildren().add(resourceTopicLabel);
        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));
        fieldBox.getChildren().add(gotoReferenceButton);
    }

    private void updateAllPropertiesWithItemValues() {
        long entryIndex = this.currentEntryIndexProperty.getValue();
        DbDataDto.Entry entry = this.databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryIndex, currentTopicObject.getTopic());

        String entryLabel = "<?>";
        if (this.profileObject.getEntryLabelFieldRanks() != null) {
            entryLabel = fetchContentsWithEntryId(this.currentTopicProperty.getValue(), entryIndex, this.profileObject.getEntryLabelFieldRanks());
        }
        this.currentEntryLabelProperty.setValue(entryLabel);

        entry.getItems().forEach(this::updateItemProperties);

        this.resourceListByTopicLink.entrySet().forEach(this::updateLinkProperties);
    }

    private void updateItemProperties(DbDataDto.Item item) {
        rawValuePropertyByFieldRank.get(item.getFieldRank()).set(item.getRawValue());

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, this.currentTopicObject.getStructure().getFields());
        if (structureField.isAResourceField()) {
            updateResourceProperties(item, structureField);
        }

        if (DbStructureDto.FieldType.REFERENCE == structureField.getFieldType()
                && resolvedValuePropertyByFieldRank.containsKey(item.getFieldRank())) {
            updateReferenceProperties(item, structureField);
        }
    }

    private void updateResourceProperties(DbDataDto.Item resourceItem, DbStructureDto.Field structureField) {
        DbDto.Topic resourceTopic = this.currentTopicObject.getTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = this.databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        Optional<DbResourceDto.Entry> potentialResourceEntry = this.databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceItem.getRawValue(), resourceTopic, this.currentLocaleProperty.getValue());
        if (potentialResourceEntry.isPresent()) {
            String resourceValue = potentialResourceEntry.get().getValue();
            resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(resourceValue);
        }
    }

    private void updateReferenceProperties(DbDataDto.Item referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

        List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), profilesChoiceBox.getValue(), this.layoutObject);
        if (fieldSettings.isPresent()) {
            remoteFieldRanks = fieldSettings.get().getRemoteFieldRanks();
        }

        String remoteContents = fetchRemoteContentsWithEntryRef(remoteTopic, referenceItem.getRawValue(), remoteFieldRanks);
        resolvedValuePropertyByFieldRank.get(referenceItem.getFieldRank()).set(remoteContents);
    }

    private void updateLinkProperties(Map.Entry<TopicLinkDto, ObservableList<RemoteResource>> remoteEntry) {
        TopicLinkDto linkObject = remoteEntry.getKey();
        ObservableList<RemoteResource> values = remoteEntry.getValue();
        values.clear();

        DbDto topicObject = this.databaseMiner.getDatabaseTopic(linkObject.getTopic()).get();
        topicObject.getData().getEntries().stream()

                .filter((contentEntry) -> {
                    // TODO find another way of getting current reference
                    String currentRef = contentEntry.getItems().get(0).getRawValue();
                    return rawValuePropertyByFieldRank.get(1).getValue().equals(currentRef);
                })

                .map((contentEntry) -> getLinkResourceFromContentEntry(contentEntry, linkObject, topicObject))

                .forEach(values::add);
    }

    private RemoteResource getLinkResourceFromContentEntry(DbDataDto.Entry contentEntry, TopicLinkDto linkObject, DbDto topicObject) {
        RemoteResource remoteResource = new RemoteResource();
        if (topicObject.getStructure().getFields().size() == 2) {
            // Association topic (e.g. Car_Rims)
            String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
            DbDto.Topic remoteTopic = databaseMiner.getDatabaseTopicFromReference(remoteTopicRef).getTopic();

            String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
            remoteResource.setReference(remoteEntryReference);
            remoteResource.setValue(fetchRemoteContentsWithEntryRef(remoteTopic, remoteEntryReference, linkObject.getRemoteFieldRanks()));
        } else {
            long entryId = contentEntry.getId();
            remoteResource.setReference(Long.valueOf(entryId).toString());
            remoteResource.setValue(fetchContentsWithEntryId(linkObject.getTopic(), entryId, linkObject.getRemoteFieldRanks()));
        }
        return remoteResource;
    }

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        long remoteEntryId = this.databaseMiner.getContentEntryIdFromReference(remoteEntryReference, remoteTopic).getAsLong();
        return fetchContentsWithEntryId(remoteTopic, remoteEntryId, remoteFieldRanks);
    }

    private String fetchContentsWithEntryId(DbDto.Topic topic, long entryId, List<Integer> fieldRanks) {
        requireNonNull(fieldRanks, "A list of field ranks (even empty) must be provided.");

        if (fieldRanks.isEmpty()) {
            return "??";
        }

        List<String> contents = fieldRanks.stream()

                .map((fieldRank) -> {
                    Optional<DbResourceDto.Entry> potentialRemoteResourceEntry = this.databaseMiner.getRemoteResourceEntryWithInternalIdentifier(topic, fieldRank, entryId, currentLocaleProperty.getValue());
                    if (potentialRemoteResourceEntry.isPresent()) {
                        return potentialRemoteResourceEntry.get().getValue();
                    }
                    return "??";
                })

                .collect(toList());

        return String.join(" - ", contents);
    }

    // TODO feature: handle navigation history to go back
    private void switchToProfileAndEntry(String profileName, long entryIndex) {
        this.profilesChoiceBox.setValue(profileName);
        switchToContentEntry(entryIndex);
    }

    private void switchToContentEntry(long entryIndex) {
        this.currentEntryIndexProperty.setValue(entryIndex);
        updateAllPropertiesWithItemValues();
    }
}
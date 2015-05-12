package fr.tduf.gui.database.controllers;

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
import javafx.util.StringConverter;
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
// TODO Resolve fields in profile with rank (name is just to make things clear)
public class MainStageController implements Initializable {

    private static final Class<MainStageController> thisClass = MainStageController.class;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private Label currentTopicLabel;

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
    private Property<Integer> currentEntryIndexProperty;
    private Property<Integer> entryItemsCountProperty;

    private Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    private Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();
    private Map<TopicLinkDto, ObservableList<RemoteResource>> resourceListByTopicLink = new HashMap<>();


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            initSettingsPane();

            initNavigationPane();

            // DEBUG
            databaseLocationTextField.setText("/media/DevStore/GIT/tduf/cli/integ-tests/db-json/");
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

        int currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex >= this.currentTopicObject.getData().getEntries().size() - 1) {
            return;
        }

        currentEntryIndex++;
        this.currentEntryIndexProperty.setValue(currentEntryIndex);
        updateAllPropertiesWithItemValues();
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        int currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex <= 0 ) {
            return;
        }

        currentEntryIndex--;
        this.currentEntryIndexProperty.setValue(currentEntryIndex);
        updateAllPropertiesWithItemValues();
    }

    @FXML
    public void handleFirstButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFirstButtonMouseClick");

        this.currentEntryIndexProperty.setValue(0);
        updateAllPropertiesWithItemValues();
    }

    @FXML
    public void handleLastButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        this.currentEntryIndexProperty.setValue(this.currentTopicObject.getData().getEntries().size() - 1);
        updateAllPropertiesWithItemValues();
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        fillTabPaneDynamically(newProfileName);
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
        this.currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new StringConverter<DbDto.Topic>() {
            @Override
            public String toString(DbDto.Topic object) {
                if (object == null) {
                    return "<?>";
                }
                return object.name();
            }

            @Override
            public DbDto.Topic fromString(String string) {
                return DbDto.Topic.valueOf(string);
            }
        });

        this.entryItemsCountProperty = new SimpleObjectProperty<>(-1);
        this.entryItemsCountLabel.textProperty().bindBidirectional(entryItemsCountProperty, new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == -1) {
                    return "/ <?>";
                }
                return "/ " + object;
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });

        this.currentEntryIndexProperty = new SimpleObjectProperty<>(-1);
        this.entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == -1) {
                    return "<?>";
                }
                return "" + (object + 1);

            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });
    }

    private void loadAndFillProfiles() throws IOException {
        this.layoutObject = new ObjectMapper().readValue(thisClass.getResource("/layout/defaultProfiles.json"), EditorLayoutDto.class);
        this.layoutObject.getProfiles()
                .forEach((profileObject) -> profilesChoiceBox.getItems().add(profileObject.getName()));
    }

    private void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> this.localesChoiceBox.getItems().add(locale));

        this.currentLocaleProperty = new SimpleObjectProperty<>(DbResourceDto.Locale.UNITED_STATES);
        this.localesChoiceBox.valueProperty().bindBidirectional(this.currentLocaleProperty);
    }

    private void fillTabPaneDynamically(String profileName) {

        if (databaseObjects.isEmpty()) {
            return;
        }

        this.profileObject = EditorLayoutHelper.getAvailableProfileByName(profileName, this.layoutObject);
        initGroupTabs();

        DbDto.Topic startTopic = profileObject.getTopic();
        this.currentTopicObject = databaseMiner.getDatabaseTopic(startTopic).get();
        this.currentTopicProperty.setValue(this.currentTopicObject.getStructure().getTopic());

        this.currentEntryIndexProperty.setValue(0);
        this.entryItemsCountProperty.setValue(this.currentTopicObject.getData().getEntries().size());

        this.rawValuePropertyByFieldRank.clear();
        this.resolvedValuePropertyByFieldRank.clear();
        this.resourceListByTopicLink.clear();

        assignFieldControls();

        assignAllLinkControls();

        updateAllPropertiesWithItemValues();
    }

    private void assignFieldControls() {
        this.currentTopicObject.getStructure().getFields()

                .forEach(this::assignControls);
    }

    private void assignControls(DbStructureDto.Field field) {

        SimpleStringProperty property = new SimpleStringProperty("");
        rawValuePropertyByFieldRank.put(field.getRank(), property);

        Optional<FieldSettingsDto> potentialFieldSettings = getFieldSettings(field, profilesChoiceBox.getValue());
        String fieldName = field.getName();
        boolean fieldReadOnly = false;
        String groupName = null;
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
        }

        HBox fieldBox = createFieldBox(Optional.ofNullable(groupName));

        addFieldLabel(fieldBox, fieldReadOnly, fieldName);

        addTextField(fieldBox, fieldReadOnly, property);

        if (isAResourceField(field)) {
            DbDto.Topic topic = currentTopicObject.getStructure().getTopic();
            if (field.getTargetRef() != null) {
                topic = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef()).getStructure().getTopic();
            }

            addResourceValueControls(fieldBox, field.getRank(), topic);
        }

        if (DbStructureDto.FieldType.REFERENCE == field.getFieldType()) {
            DbDto.Topic topic = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef()).getStructure().getTopic();
            addReferenceValueControls(fieldBox, field.getRank(), topic);
        }
    }

    private void assignAllLinkControls() {
        if (this.profileObject.getTopicLinks() == null) {
            return;
        }

        this.profileObject.getTopicLinks()

                .forEach(this::assignLinkControls);
    }

    private void assignLinkControls(TopicLinkDto topicLinkDto) {
        HBox fieldBox = createFieldBox(Optional.ofNullable(topicLinkDto.getGroup()));
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
        valueColumn.setPrefWidth(350);

        tableView.getColumns().add(refColumn);
        tableView.getColumns().add(valueColumn);

        tableView.setItems(resourceData);

        fieldBox.getChildren().add(tableView);
    }

    private Optional<FieldSettingsDto> getFieldSettings(DbStructureDto.Field field, String profileName) {
        EditorLayoutDto.EditorProfileDto currentProfile = EditorLayoutHelper.getAvailableProfileByName(profileName, this.layoutObject);

        // TODO extract to method in EditorLayoutHelper
        return currentProfile.getFieldSettings().stream()

                .filter((settings) -> settings.getName().equals(field.getName()))

                .findAny();
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

    private void updateAllPropertiesWithItemValues() {
        int entryIndex = this.currentEntryIndexProperty.getValue();
        DbDataDto.Entry entry = this.currentTopicObject.getData().getEntries().get(entryIndex);

        entry.getItems().forEach((item) -> {
            String rawValue = item.getRawValue();

            rawValuePropertyByFieldRank.get(item.getFieldRank()).set(rawValue);

            DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, this.currentTopicObject.getStructure().getFields());
            if (isAResourceField(structureField)) {

                DbDto.Topic resourceTopic = this.currentTopicObject.getStructure().getTopic();
                if (structureField.getTargetRef() != null) {
                    resourceTopic = databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef()).getStructure().getTopic();
                }

                Optional<DbResourceDto.Entry> potentialResourceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(rawValue, resourceTopic, this.currentLocaleProperty.getValue());
                if (potentialResourceEntry.isPresent()) {
                    String resourceValue = potentialResourceEntry.get().getValue();
                    resolvedValuePropertyByFieldRank.get(item.getFieldRank()).set(resourceValue);
                }
            }

            if (DbStructureDto.FieldType.REFERENCE == structureField.getFieldType()) {
                if (resolvedValuePropertyByFieldRank.containsKey(item.getFieldRank())) {
                    DbDto.Topic remoteTopic = databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef()).getStructure().getTopic();

                    List<Integer> remoteFieldRanks = new ArrayList<>();
                    Optional<FieldSettingsDto> fieldSettings = getFieldSettings(structureField, profilesChoiceBox.getValue());
                    if (fieldSettings.isPresent()) {
                        remoteFieldRanks = fieldSettings.get().getRemoteFieldRanks();
                    }

                    String remoteContents = fetchRemoteContents(remoteTopic, item.getRawValue(), remoteFieldRanks);
                    resolvedValuePropertyByFieldRank.get(item.getFieldRank()).set(remoteContents);
                }
            }
        });

        this.resourceListByTopicLink.entrySet().forEach((remoteEntry) -> {
            TopicLinkDto linkObject = remoteEntry.getKey();
            ObservableList<RemoteResource> values = remoteEntry.getValue();
            values.clear();

            DbDto topicObject = this.databaseMiner.getDatabaseTopic(linkObject.getTopic()).get();
            topicObject.getData().getEntries().stream()

                    .filter((contentEntry) -> {
                        String currentRef = contentEntry.getItems().get(0).getRawValue();
                        // TODO find another way of getting current reference
                        return rawValuePropertyByFieldRank.get(1).getValue().equals(currentRef);
                    })

                    .map((contentEntry) -> {
                        String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
                        DbDto.Topic remoteTopic = databaseMiner.getDatabaseTopicFromReference(remoteTopicRef).getStructure().getTopic();

                        String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
                        RemoteResource remoteResource = new RemoteResource();
                        remoteResource.setReference(remoteEntryReference);
                        remoteResource.setValue(fetchRemoteContents(remoteTopic, remoteEntryReference, asList(2,4,5,6,7)));
                        return remoteResource;
                    })

                    .forEach(values::add);
        });
    }

    private String fetchRemoteContents(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        if (remoteFieldRanks.isEmpty()) {
            return "Reference to another topic.";
        }

        List<String> contents = remoteFieldRanks.stream()

                .map((remoteFieldRank) -> {

                    Optional<DbDataDto.Entry> potentialContentEntry = databaseMiner.getContentEntryFromTopicWithReference(remoteEntryReference, remoteTopic);
                    if (!potentialContentEntry.isPresent()) {
                        return "<?>";
                    }

                    DbDataDto.Entry contentEntry = potentialContentEntry.get();
                    String resourceReference = contentEntry.getItems().stream()

                            .filter((contentsItem) -> contentsItem.getFieldRank() == remoteFieldRank)

                            .findAny().get().getRawValue();

                    Optional<DbResourceDto.Entry> potentialRemoteResourceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceReference, remoteTopic, this.currentLocaleProperty.getValue());
                    if (potentialRemoteResourceEntry.isPresent()) {
                        return potentialRemoteResourceEntry.get().getValue();
                    }

                    return resourceReference;
                })

                .collect(toList());

        return String.join(" - ", contents);
    }

    private HBox createFieldBox(Optional<String> groupName) {
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

    private void addTextField(HBox fieldBox, boolean readOnly, Property<String> property) {
        TextField fieldValue = new TextField();

        if (readOnly) {
            fieldValue.getStyleClass().add("readonlyField");
        }
        fieldValue.setPrefWidth(110.0);
        fieldValue.setEditable(!readOnly);


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

    private void addReferenceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic topic) {
        Label remoteValueLabel = new Label();
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.getStyleClass().add("fieldLabel");

        SimpleStringProperty property = new SimpleStringProperty("Reference to another topic.");
        resolvedValuePropertyByFieldRank.put(fieldRank, property);
        remoteValueLabel.textProperty().bindBidirectional(property);

        Label resourceTopicLabel = new Label(topic.name());
        resourceTopicLabel.getStyleClass().add("fieldLabel");

        fieldBox.getChildren().add(remoteValueLabel);
        fieldBox.getChildren().add(new Separator(Orientation.VERTICAL));
        fieldBox.getChildren().add(resourceTopicLabel);
    }

    // TODO extract to FieldType object
    private static boolean isAResourceField(DbStructureDto.Field field) {
        return DbStructureDto.FieldType.RESOURCE_CURRENT == field.getFieldType()
                || DbStructureDto.FieldType.RESOURCE_CURRENT_AGAIN == field.getFieldType()
                || DbStructureDto.FieldType.RESOURCE_REMOTE == field.getFieldType();
    }
}
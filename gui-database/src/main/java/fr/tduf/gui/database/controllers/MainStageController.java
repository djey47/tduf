package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.helper.EditorLayoutHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    private static final Class<MainStageController> thisClass = MainStageController.class;

    @FXML
    private TitledPane settingsPane;

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

    private List<DbDto> databaseObjects = new ArrayList<>();
    private EditorLayoutDto layoutObject;
    private BulkDatabaseMiner databaseMiner;

    private Map<Integer, SimpleStringProperty> propertyByFieldRank = new HashMap<>();
    private int currentEntryIndex = 0;
    private DbDto currentTopicObject;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        try {
            initSettingsPane();

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

        this.currentEntryIndex++;

        updateAllPropertiesWithItemValues(this.currentEntryIndex);
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        this.currentEntryIndex--;

        updateAllPropertiesWithItemValues(this.currentEntryIndex);
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        EditorLayoutDto.EditorProfileDto profileObject = EditorLayoutHelper.getAvailableProfileByName(newProfileName, this.layoutObject);
        fillTabPaneDynamically(profileObject);
    }

    private void initSettingsPane() throws IOException {

        this.settingsPane.setExpanded(false);

        fillLocales();

        loadAndFillProfiles();

        this.profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));
    }

    private void loadAndFillProfiles() throws IOException {
        URL resourceURL = thisClass.getResource("/layout/defaultProfiles.json");

        this.layoutObject = new ObjectMapper().readValue(resourceURL, EditorLayoutDto.class);
        this.layoutObject.getProfiles()
                .forEach((profileObject) -> profilesChoiceBox.getItems().add(profileObject.getName()));
    }

    private void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> localesChoiceBox.getItems().add(locale));

        localesChoiceBox.setValue(DbResourceDto.Locale.UNITED_STATES);
    }

    private void fillTabPaneDynamically(EditorLayoutDto.EditorProfileDto profileObject) {

        if (databaseObjects.isEmpty()) {
            return;
        }

        DbDto.Topic startTopic = profileObject.getTopic();

        this.currentTopicObject = databaseMiner.getDatabaseTopic(startTopic).get();

        propertyByFieldRank.clear();
        this.currentTopicObject.getStructure().getFields()

                .forEach(this::assignControl);

        this.currentEntryIndex = 0;
        updateAllPropertiesWithItemValues(currentEntryIndex);
    }

    private void updateAllPropertiesWithItemValues(int entryIndex) {
        DbDataDto.Entry entry = this.currentTopicObject.getData().getEntries().get(entryIndex);

        entry.getItems().forEach((item) -> propertyByFieldRank.get(item.getFieldRank()).set(item.getRawValue()));
    }

    private void assignControl(DbStructureDto.Field field) {

        EditorLayoutDto.EditorProfileDto currentProfile = EditorLayoutHelper.getAvailableProfileByName(profilesChoiceBox.getValue(), layoutObject);

        Optional<FieldSettingsDto> potentialFieldSettings = currentProfile.getFieldSettings().stream()

                .filter((settings) -> settings.getName().equals(field.getName()))

                .findAny();

        String fieldName = field.getName();
        boolean fieldReadOnly = false;
        if(potentialFieldSettings.isPresent()) {
            FieldSettingsDto fieldSettings = potentialFieldSettings.get();

            if (fieldSettings.getLabel() != null) {
                fieldName = potentialFieldSettings.get().getLabel();
            }

            fieldReadOnly = fieldSettings.isReadOnly();
        }

        HBox fieldBox = createFieldBox();

        addFieldLabel(fieldBox, fieldName);

        addTextField(fieldBox, fieldReadOnly, field.getRank());
    }

    private HBox createFieldBox() {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(25.0);
        fieldBox.setPadding(new Insets(5.0));
        defaultTab.getChildren().add(fieldBox);
        return fieldBox;
    }

    private void addFieldLabel(HBox fieldBox, String fieldName) {
        Label fieldNameLabel = new Label(fieldName);
        fieldNameLabel.setPrefWidth(225.0);
        fieldNameLabel.getStyleClass().add("fieldName");
        fieldBox.getChildren().add(fieldNameLabel);
    }

    private void addTextField(HBox fieldBox, boolean readOnly, int fieldRank) {
        TextField fieldValue = new TextField();
        fieldValue.setPrefWidth(95.0);
        fieldValue.setEditable(!readOnly);

        SimpleStringProperty property = new SimpleStringProperty("");
        propertyByFieldRank.put(fieldRank, property);
        fieldValue.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(fieldValue);
    }
}
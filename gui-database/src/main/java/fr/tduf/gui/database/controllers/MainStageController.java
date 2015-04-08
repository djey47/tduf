package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.helper.EditorLayoutHelper;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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

        DbDto topicObject = databaseMiner.getDatabaseTopic(startTopic).get();

        topicObject.getStructure().getFields()

                .forEach(this::assignControl);
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

        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(25.0);
        fieldBox.setPadding(new Insets(5.0));

        Label fieldNameLabel = new Label(fieldName);
        fieldNameLabel.setPrefWidth(250.0);

        TextField fieldValue = new TextField();
        fieldValue.setPrefWidth(75.0);
        fieldValue.setEditable(!fieldReadOnly);

        fieldBox.getChildren().add(fieldNameLabel);
        fieldBox.getChildren().add(fieldValue);

        defaultTab.getChildren().add(fieldBox);

    }
}
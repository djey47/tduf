package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    private Stage mainStage;

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


    private List<DbDto> databaseObjects;
    private EditorLayoutDto layoutObject;
    private BulkDatabaseMiner databaseMiner;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));

    }

    @FXML
    public void handleLoadButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLoadButtonMouseClick");

        String databaseLocation = getDatabaseLocationTextField().getText();
        if (StringUtils.isNotEmpty(databaseLocation)) {
            this.databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
            this.databaseMiner = BulkDatabaseMiner.load(this.databaseObjects);

            profilesChoiceBox.setValue(profilesChoiceBox.getItems().get(0));
        }
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        EditorLayoutDto.EditorProfileDto profileObject = getAvailableProfileByName(newProfileName);
        fillTabPaneDynamically(profileObject);
    }

    private void fillTabPaneDynamically(EditorLayoutDto.EditorProfileDto profileObject) {

        if (databaseMiner == null) {
            return;
        }

        DbDto.Topic startTopic = profileObject.getTopic();

        DbDto topicObject = databaseMiner.getDatabaseTopic(startTopic).get();

        topicObject.getStructure().getFields()

                .forEach(this::assignControl);
    }

    private void assignControl(DbStructureDto.Field field) {

        defaultTab.getChildren().add(new Label(field.getName()));

        defaultTab.getChildren().add(new TextField());

    }

    private EditorLayoutDto.EditorProfileDto getAvailableProfileByName(String profileName) {
        return this.layoutObject.getProfiles().stream()

                .filter((profile) -> profile.getName().equals(profileName))

                .findAny().get();
    }

    public TitledPane getSettingsPane() {
        return this.settingsPane;
    }

    public TextField getDatabaseLocationTextField() {
        return this.databaseLocationTextField;
    }

    public ChoiceBox getLocalesChoiceBox() {
        return this.localesChoiceBox;
    }

    public ChoiceBox getProfilesChoiceBox() {
        return profilesChoiceBox;
    }

    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    public void setLayoutObject(EditorLayoutDto layoutObject) {
        this.layoutObject = layoutObject;
    }

}
package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController {

    private Stage mainStage;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private ChoiceBox localesChoiceBox;

    @FXML
    private ChoiceBox profilesChoiceBox;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField databaseLocationTextField;


    private List<DbDto> databaseObjects;

    @FXML
    public void handleLoadButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLoadButtonMouseClick");

        String databaseLocation = getDatabaseLocationTextField().getText();
        if (StringUtils.isNotEmpty(databaseLocation)) {
            this.databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
        }
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
}
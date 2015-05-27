package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.domain.RemoteResource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EntriesStageController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TableView<RemoteResource> entriesTableView;

    private MainStageController mainStageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        System.out.println("handleEntriesTableMouseClick");

    }

    @FXML
    private void handleSelectEntryButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleSelectEntryButtonMouseClick");

    }

    void initAndShowDialog() {
        Stage stage = (Stage)root.getScene().getWindow();
        stage.show();
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }
}
package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

public class ResourcesStageController implements Initializable {
    @FXML
    public AnchorPane parentAnchorPane;

    @FXML
    public ChoiceBox<DbDto.Topic> topicsChoiceBox;

    @FXML
    public TableView<RemoteResource> resourcesTableView;

    private ObservableList<RemoteResource> resourceData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTopicPane();

        initTablePane();
    }

    private void initTopicPane() {
        fillTopics();
        this.topicsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleTopicChoiceChanged(newValue));
    }

    private void initTablePane() {
        TableColumn<RemoteResource, String> refColumn = (TableColumn<RemoteResource, String>) this.resourcesTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());

        TableColumn<RemoteResource, String> valueColumn = (TableColumn<RemoteResource, String>) this.resourcesTableView.getColumns().get(1);
        valueColumn.setCellValueFactory((cellData) -> cellData.getValue().valueProperty());

        this.resourcesTableView.setItems(this.resourceData);
    }

    private void fillTopics() {
        asList(DbDto.Topic.values())
                .forEach((topic) -> this.topicsChoiceBox.getItems().add(topic));
    }

    private void handleTopicChoiceChanged(DbDto.Topic newTopic) {
        this.resourceData.clear();

        DbResourceDto.Locale locale = getMainStageController().getViewDataController().getCurrentLocaleProperty().getValue();
        DbResourceDto resourceObject = getMiner().getResourceFromTopicAndLocale(newTopic, locale).get();

        resourceObject.getEntries().forEach((resourceEntry) -> {
            RemoteResource remoteResource = new RemoteResource();
            remoteResource.setReference(resourceEntry.getReference());
            remoteResource.setValue(resourceEntry.getValue());
            this.resourceData.add(remoteResource);
        });
    }

    private MainStageController getMainStageController() {
        return (MainStageController) this.parentAnchorPane.getScene().getUserData();    // Hack to get main controller reference
    }

    private BulkDatabaseMiner getMiner() {
        return getMainStageController().getMiner();
    }
}
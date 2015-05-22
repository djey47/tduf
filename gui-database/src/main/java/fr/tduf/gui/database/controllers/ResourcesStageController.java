package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.domain.BrowsedResource;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

public class ResourcesStageController implements Initializable {
    @FXML
    private Parent root;

    @FXML
    private ChoiceBox<DbDto.Topic> topicsChoiceBox;

    @FXML
    private TableView<RemoteResource> resourcesTableView;

    private MainStageController mainStageController;

    private ObservableList<RemoteResource> resourceData = FXCollections.observableArrayList();

    private Property<BrowsedResource> browsedResourceProperty;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.browsedResourceProperty = new SimpleObjectProperty<>();
        this.browsedResourceProperty
                .addListener((observable, oldValue, newValue) -> handleBrowseToResource(newValue));

        initTopicPane();

        initTablePane();
    }

    void showDialog() {
        Stage stage = (Stage)this.root.getScene().getWindow();
        stage.show();
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
        System.out.println("handleTopicChoiceChanged: " + newTopic);

        this.resourceData.clear();

        DbResourceDto.Locale locale = this.mainStageController.getViewDataController().getCurrentLocaleProperty().getValue();
        DbResourceDto resourceObject = getMiner().getResourceFromTopicAndLocale(newTopic, locale).get();

        resourceObject.getEntries().forEach((resourceEntry) -> {
            RemoteResource remoteResource = new RemoteResource();
            remoteResource.setReference(resourceEntry.getReference());
            remoteResource.setValue(resourceEntry.getValue());
            this.resourceData.add(remoteResource);
        });
    }

    private void handleBrowseToResource(BrowsedResource newResource) {
        System.out.println("handleBrowseToResource: " + newResource);

        this.topicsChoiceBox.setValue(newResource.getTopic());

        selectResourceInTableAndScroll(newResource.getReference());
    }

    private void selectResourceInTableAndScroll(String reference) {
        RemoteResource browsedResource = this.resourceData.stream()

                .filter((remoteResource) -> remoteResource.referenceProperty().get().equals(reference))

                .findAny().get();

        this.resourcesTableView.scrollTo(browsedResource);
        this.resourcesTableView.getSelectionModel().select(browsedResource);
    }

    Property<BrowsedResource> getBrowsedResourceProperty() {
        return browsedResourceProperty;
    }

    void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }
}
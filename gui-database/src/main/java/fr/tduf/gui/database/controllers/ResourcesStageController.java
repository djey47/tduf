package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.domain.BrowsedResource;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

public class ResourcesStageController implements Initializable {
    @FXML
    private Parent root;

    @FXML
    private ChoiceBox<DbDto.Topic> topicsChoiceBox;

    @FXML
    private TableView<RemoteResource> resourcesTableView;

    @FXML
    private Button selectResourceButton;

    private MainStageController mainStageController;

    private ObservableList<RemoteResource> resourceData = FXCollections.observableArrayList();

    private Property<BrowsedResource> browsedResourceProperty;

    private SimpleStringProperty resourceReferenceProperty;

    private int fieldRank;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.browsedResourceProperty = new SimpleObjectProperty<>();
        this.browsedResourceProperty
                .addListener((observable, oldValue, newValue) -> handleBrowseToResource(newValue));

        initTopicPane();

        initTablePane();
    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        System.out.println("handleResourceTableMouseClick");

        if (MouseButton.PRIMARY == mouseEvent.getButton() && mouseEvent.getClickCount() == 2) {
            Optional<RemoteResource> selectedResource = TableViewHelper.getMouseSelectedItem(mouseEvent);
            if (selectedResource.isPresent()) {
                applyResourceSelectionToMainStage(selectedResource.get());
            }
        }
    }

    private void handleSelectResourceButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleSelectResourceButtonMouseClick");

        RemoteResource selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource != null && resourceReferenceProperty != null) {
            applyResourceSelectionToMainStage(selectedResource);
        }
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

    void showDialog() {
        Stage stage = (Stage)this.root.getScene().getWindow();
        stage.show();
    }

    private void initTopicPane() {
        fillTopics();
        this.topicsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleTopicChoiceChanged(newValue));

        this.selectResourceButton.setOnAction(this::handleSelectResourceButtonMouseClick);
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

    private void selectResourceInTableAndScroll(String reference) {
        RemoteResource browsedResource = this.resourceData.stream()

                .filter((remoteResource) -> remoteResource.referenceProperty().get().equals(reference))

                .findAny().get();

        this.resourcesTableView.scrollTo(browsedResource);
        this.resourcesTableView.getSelectionModel().select(browsedResource);
    }

    private void applyResourceSelectionToMainStage(RemoteResource selectedResource) {
        String resourceReference = selectedResource.referenceProperty().getValue();
        resourceReferenceProperty.set(resourceReference);

        // TODO see to update item properties automatically upon property change
        mainStageController.getChangeDataController().updateContentItem(fieldRank, resourceReference);
    }

    Property<BrowsedResource> getBrowsedResourceProperty() {
        return browsedResourceProperty;
    }

    void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    void setResourceReferenceProperty(SimpleStringProperty resourceReferenceProperty) {
        this.resourceReferenceProperty = resourceReferenceProperty;
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }

    public void setFieldRank(int fieldRank) {
        this.fieldRank = fieldRank;
    }
}
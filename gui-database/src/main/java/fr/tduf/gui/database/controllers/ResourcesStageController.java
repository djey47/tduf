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
    private ChoiceBox<DbResourceDto.Locale> localesChoiceBox;

    @FXML
    private TableView<RemoteResource> resourcesTableView;

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
                applyResourceSelectionToMainStageAndClose(selectedResource.get());
            }
        }
    }

    @FXML
    private void handleSelectResourceButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleSelectResourceButtonMouseClick");

        RemoteResource selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource != null && resourceReferenceProperty != null) {
            applyResourceSelectionToMainStageAndClose(selectedResource);
        }
    }

    @FXML
    private void handleAddResourceButtonMouseClick(ActionEvent actionEvent){
        System.out.println("handleAddResourceButtonMouseClick");

    }

    @FXML
    private void handleRemoveResourceButtonMouseClick(ActionEvent actionEvent){
        System.out.println("handleRemoveResourceButtonMouseClick");

    }

    private void handleTopicChoiceChanged(DbDto.Topic newTopic) {
        System.out.println("handleTopicChoiceChanged: " + newTopic);

        updateResourceData();
    }

    private void handleLocaleChoiceChanged(DbResourceDto.Locale newLocale) {
        System.out.println("handleLocaleChoiceChanged: " + newLocale);

        updateResourceData();
    }

    private void handleBrowseToResource(BrowsedResource newResource) {
        System.out.println("handleBrowseToResource: " + newResource);

        this.topicsChoiceBox.setValue(newResource.getTopic());

        selectResourceInTableAndScroll(newResource.getReference());
    }

    private void updateResourceData() {
        this.resourceData.clear();

        DbResourceDto.Locale locale = localesChoiceBox.valueProperty().get();
        DbDto.Topic topic = topicsChoiceBox.valueProperty().get();
        DbResourceDto resourceObject = getMiner().getResourceFromTopicAndLocale(topic, locale).get();

        resourceObject.getEntries().forEach((resourceEntry) -> {
            RemoteResource remoteResource = new RemoteResource();
            remoteResource.setReference(resourceEntry.getReference());
            remoteResource.setValue(resourceEntry.getValue());
            this.resourceData.add(remoteResource);
        });
    }

    void updateAndShowDialog(SimpleStringProperty referenceProperty, int entryFieldRank, DbResourceDto.Locale locale, DbDto.Topic targetTopic) {
        resourceReferenceProperty = referenceProperty;
        fieldRank = entryFieldRank;
        localesChoiceBox.setValue(locale);
        browsedResourceProperty.setValue(new BrowsedResource(targetTopic, referenceProperty.get()));

        Stage stage = (Stage)this.root.getScene().getWindow();
        stage.show();
    }

    private void initTopicPane() {
        fillTopics();
        topicsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleTopicChoiceChanged(newValue));

        fillLocales();
        localesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue)));
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
                .forEach((topic) -> topicsChoiceBox.getItems().add(topic));
    }

    void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> localesChoiceBox.getItems().add(locale));
    }

    private void selectResourceInTableAndScroll(String reference) {
        RemoteResource browsedResource = this.resourceData.stream()

                .filter((remoteResource) -> remoteResource.referenceProperty().get().equals(reference))

                .findAny().get();

        this.resourcesTableView.scrollTo(browsedResource);
        this.resourcesTableView.getSelectionModel().select(browsedResource);
    }

    private void applyResourceSelectionToMainStageAndClose(RemoteResource selectedResource) {
        String resourceReference = selectedResource.referenceProperty().getValue();
        resourceReferenceProperty.set(resourceReference);

        // TODO see to update item properties automatically upon property change
        mainStageController.getChangeDataController().updateContentItem(fieldRank, resourceReference);

        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }
}
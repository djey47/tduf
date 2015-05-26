package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
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
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;

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

        RemoteResource selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource != null) {
            Alert alert = new Alert(CONFIRMATION);
            alert.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
            alert.setHeaderText(selectedResource.toDisplayableValue());
            alert.setContentText(DisplayConstants.MESSAGE_DELETED_RESOURCE + "\n" + DisplayConstants.QUESTION_AFFECTED_LOCALES);

            DbResourceDto.Locale currentLocale = localesChoiceBox.valueProperty().get();
            ButtonType currentLocaleButtonType = new ButtonType(String.format(DisplayConstants.LABEL_BUTTON_CURRENT_LOCALE, currentLocale.getCode()));
            ButtonType allLocalesButtonType = new ButtonType(DisplayConstants.LABEL_BUTTON_ALL);
            ButtonType cancelButtonType = new ButtonType("Cancel", CANCEL_CLOSE);

            alert.getButtonTypes().setAll(currentLocaleButtonType, allLocalesButtonType, cancelButtonType);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()
                    && cancelButtonType != result.get()) {
                removeResourceAndUpdateMainStage(topicsChoiceBox.getValue(), selectedResource, currentLocale, allLocalesButtonType == result.get());
            }
        }
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

    void initAndShowDialog(SimpleStringProperty referenceProperty, int entryFieldRank, DbResourceDto.Locale locale, DbDto.Topic targetTopic) {
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
        mainStageController.getChangeDataController().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, resourceReference);

        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void removeResourceAndUpdateMainStage(DbDto.Topic currentTopic, RemoteResource selectedResource, DbResourceDto.Locale currentLocale, boolean forAllLocales) {
        mainStageController.getChangeDataController().removeResourceWithReference(currentTopic, currentLocale, selectedResource.referenceProperty().getValue(), forAllLocales);

        updateResourceData();

        mainStageController.getViewDataController().updateAllPropertiesWithItemValues();
    }

    private void updateResourceData() {
        this.resourceData.clear();

        DbResourceDto.Locale locale = localesChoiceBox.valueProperty().get();
        DbDto.Topic topic = topicsChoiceBox.valueProperty().get();
        Optional<DbResourceDto> potentialResourceObject = getMiner().getResourceFromTopicAndLocale(topic, locale);
        if (potentialResourceObject.isPresent()) {
            potentialResourceObject.get().getEntries().forEach((resourceEntry) -> {
                RemoteResource remoteResource = new RemoteResource();
                remoteResource.setReference(resourceEntry.getReference());
                remoteResource.setValue(resourceEntry.getValue());
                this.resourceData.add(remoteResource);
            });
        }
    }

    void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }
}
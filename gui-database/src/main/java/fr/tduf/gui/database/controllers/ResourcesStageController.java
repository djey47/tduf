package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
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
import javafx.util.Pair;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class ResourcesStageController implements Initializable {

    private DialogsHelper dialogsHelper = new DialogsHelper();

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
        browsedResourceProperty = new SimpleObjectProperty<>();
        browsedResourceProperty
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
    private void handleEditResourceButtonMouseClick(ActionEvent actionEvent){
        System.out.println("handleEditResourceButtonMouseClick");

        RemoteResource selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource == null) {
            return;
        }

        String currentResourceReference = selectedResource.referenceProperty().get();
        DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic()).get();
        Optional<Pair<String, String>> result = dialogsHelper.showEditResourceDialog(currentTopicObject, Optional.of(selectedResource));
        if (result.isPresent()) {
            try {
                editResourceAndUpdateMainStage(getCurrentTopic(), Optional.of(currentResourceReference), result.get(), getCurrentLocale());
            } catch(IllegalArgumentException iae) {
                dialogsHelper.showErrorDialog(iae.getMessage(), DisplayConstants.MESSAGE_DIFFERENT_RESOURCE);
            }
        }
    }

    @FXML
    private void handleAddResourceButtonMouseClick(ActionEvent actionEvent){
        System.out.println("handleAddResourceButtonMouseClick");

        DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic()).get();
        Optional<Pair<String, String>> result = dialogsHelper.showEditResourceDialog(currentTopicObject, Optional.empty());
        if (result.isPresent()) {
            try {
                // TODO add resource for all locales. Update when done.
                editResourceAndUpdateMainStage(getCurrentTopic(), Optional.empty(), result.get(), getCurrentLocale());
            } catch(IllegalArgumentException iae) {
                dialogsHelper.showErrorDialog(iae.getMessage(), DisplayConstants.MESSAGE_DIFFERENT_RESOURCE);
            }
        }
    }

    @FXML
    private void handleRemoveResourceButtonMouseClick(ActionEvent actionEvent){
        System.out.println("handleRemoveResourceButtonMouseClick");

        RemoteResource selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource == null) {
            return;
        }

        DbResourceDto.Locale currentLocale = getCurrentLocale();
        DbDto.Topic currentTopic = getCurrentTopic();
        Optional<Boolean> result = dialogsHelper.showResourceDeletionDialog(currentTopic, selectedResource, currentLocale.getCode());
        if (result.isPresent()) {
            int selectedRow = resourcesTableView.getSelectionModel().getSelectedIndex();
            removeResourceAndUpdateMainStage(currentTopic, selectedResource, currentLocale, result.get(), selectedRow);
        }
    }

    private void handleTopicChoiceChanged(DbDto.Topic newTopic) {
        System.out.println("handleTopicChoiceChanged: " + newTopic);

        updateResourcesStageData();
    }

    private void handleLocaleChoiceChanged(DbResourceDto.Locale newLocale) {
        System.out.println("handleLocaleChoiceChanged: " + newLocale);

        updateResourcesStageData();
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
        this.resourceData.stream()

                .filter((remoteResource) -> remoteResource.referenceProperty().get().equals(reference))

                .findAny()

                .ifPresent((browsedResource) -> {
                    resourcesTableView.getSelectionModel().select(browsedResource);
                    resourcesTableView.scrollTo(browsedResource);
                });
    }

    private void applyResourceSelectionToMainStageAndClose(RemoteResource selectedResource) {
        String resourceReference = selectedResource.referenceProperty().getValue();
        resourceReferenceProperty.set(resourceReference);

        // TODO see to update item properties automatically upon property change
        mainStageController.getChangeDataController().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, resourceReference);

        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void removeResourceAndUpdateMainStage(DbDto.Topic topic, RemoteResource selectedResource, DbResourceDto.Locale locale, boolean forAllLocales, int selectedRowIndex) {
        mainStageController.getChangeDataController().removeResourceWithReference(topic, locale, selectedResource.referenceProperty().getValue(), forAllLocales);

        updateAllStages(Optional.<String>empty());

        TableViewHelper.selectRowAndScroll(selectedRowIndex, resourcesTableView);
    }

    private void editResourceAndUpdateMainStage(DbDto.Topic topic, Optional<String> currentResourceReference, Pair<String, String> referenceValuePair, DbResourceDto.Locale locale) {
        if (referenceValuePair == null) {
            return;
        }

        boolean updateResourceMode = currentResourceReference.isPresent();
        String newResourceReference = referenceValuePair.getKey();
        String newResourceValue = referenceValuePair.getValue();
        if (updateResourceMode) {
            mainStageController.getChangeDataController().updateResourceWithReference(topic, locale, currentResourceReference.get(), newResourceReference, newResourceValue);
        } else {
            mainStageController.getChangeDataController().addResourceWithReference(topic, locale, newResourceReference, newResourceValue);
        }

        updateAllStages(Optional.of(newResourceReference));
    }

    private void updateAllStages(Optional<String> resourceReference) {
        updateResourcesStageData();
        resourceReference.ifPresent(this::selectResourceInTableAndScroll);

        mainStageController.getViewDataController().updateAllPropertiesWithItemValues();
    }

    private void updateResourcesStageData() {
        resourceData.clear();

        getMiner().getResourceFromTopicAndLocale(getCurrentTopic(), getCurrentLocale())
                .ifPresent((resourceObject) -> resourceData.addAll(resourceObject.getEntries().stream()

                                .map((resourceEntry) -> {
                                    RemoteResource remoteResource = new RemoteResource();
                                    remoteResource.setReference(resourceEntry.getReference());
                                    remoteResource.setValue(resourceEntry.getValue());
                                    return remoteResource;
                                })

                                .collect(toList()))
                );
    }

    void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private DbDto.Topic getCurrentTopic() {
        return topicsChoiceBox.getValue();
    }

    private DbResourceDto.Locale getCurrentLocale() {
        return localesChoiceBox.getValue();
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }
}
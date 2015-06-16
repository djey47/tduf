package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.domain.BrowsedResource;
import fr.tduf.gui.database.domain.DatabaseEntry;
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
import java.util.stream.Stream;

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
    private TableView<DatabaseEntry> resourcesTableView;

    private MainStageController mainStageController;

    private ObservableList<DatabaseEntry> resourceData = FXCollections.observableArrayList();

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
            TableViewHelper.getMouseSelectedItem(mouseEvent)
                    .ifPresent((selectedResource) -> applyResourceSelectionToMainStageAndClose((DatabaseEntry) selectedResource));
        }
    }

    @FXML
    private void handleSelectResourceButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleSelectResourceButtonMouseClick");

        DatabaseEntry selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource != null && resourceReferenceProperty != null) {
            applyResourceSelectionToMainStageAndClose(selectedResource);
        }
    }

    @FXML
    private void handleEditResourceButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleEditResourceButtonMouseClick");

        DatabaseEntry selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource == null) {
            return;
        }

        String currentResourceReference = selectedResource.referenceProperty().get();
        DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic()).get();
        // TODO edit for current or all locales
        dialogsHelper.showEditResourceDialog(currentTopicObject, Optional.of(selectedResource), getCurrentLocale())
                .ifPresent((pair) -> editResourceAndUpdateMainStage(getCurrentTopic(), Optional.of(currentResourceReference), pair, getCurrentLocale()));
    }

    @FXML
    private void handleAddResourceButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleAddResourceButtonMouseClick");

        DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic()).get();
        // TODO add for current or all locales
        dialogsHelper.showEditResourceDialog(currentTopicObject, Optional.empty(), getCurrentLocale())
                .ifPresent((pair) -> editResourceAndUpdateMainStage(getCurrentTopic(), Optional.empty(), pair, getCurrentLocale()));
    }

    @FXML
    private void handleRemoveResourceButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleRemoveResourceButtonMouseClick");

        DatabaseEntry selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource == null) {
            return;
        }

        DbResourceDto.Locale currentLocale = getCurrentLocale();
        DbDto.Topic currentTopic = getCurrentTopic();
        dialogsHelper.showResourceDeletionDialog(currentTopic, selectedResource, currentLocale.getCode())
                .ifPresent((forAllLocales) -> {
                    int selectedRow = resourcesTableView.getSelectionModel().getSelectedIndex();
                    removeResourceAndUpdateMainStage(currentTopic, selectedResource, currentLocale, forAllLocales, selectedRow);
                });
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

        Stage stage = (Stage) this.root.getScene().getWindow();
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
        TableColumn<DatabaseEntry, String> refColumn = (TableColumn<DatabaseEntry, String>) this.resourcesTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());

        TableColumn<DatabaseEntry, String> valueColumn = (TableColumn<DatabaseEntry, String>) this.resourcesTableView.getColumns().get(1);
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

    private void applyResourceSelectionToMainStageAndClose(DatabaseEntry selectedResource) {
        String resourceReference = selectedResource.referenceProperty().getValue();
        resourceReferenceProperty.set(resourceReference);

        mainStageController.getChangeDataController().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, resourceReference);

        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void removeResourceAndUpdateMainStage(DbDto.Topic topic, DatabaseEntry selectedResource, DbResourceDto.Locale locale, boolean forAllLocales, int selectedRowIndex) {
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

        try {
            if (updateResourceMode) {
                // TODO Update for all locales at once ?
                mainStageController.getChangeDataController().updateResourceWithReference(topic, locale, currentResourceReference.get(), newResourceReference, newResourceValue);
            } else {
                Stream.of(DbResourceDto.Locale.values())

                        .forEach((affectedLocale) -> mainStageController.getChangeDataController().addResourceWithReference(topic, affectedLocale, newResourceReference, newResourceValue));
            }
        } catch(IllegalArgumentException iae) {
            dialogsHelper.showErrorDialog(iae.getMessage(), DisplayConstants.MESSAGE_DIFFERENT_RESOURCE);
        } finally {
            updateAllStages(Optional.of(newResourceReference));
        }
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
                                    DatabaseEntry databaseEntry = new DatabaseEntry();
                                    databaseEntry.setReference(resourceEntry.getReference());
                                    databaseEntry.setValue(resourceEntry.getValue());
                                    return databaseEntry;
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
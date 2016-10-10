package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.gui.database.domain.javafx.ResourceEntryDataItem;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Controller to display and modify database resources via dedicated dialog.
 */
public class ResourcesStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = ResourcesStageController.class.getSimpleName();

    private DialogsHelper dialogsHelper = new DialogsHelper();

    @FXML
    private ChoiceBox<DbDto.Topic> topicsChoiceBox;

    @FXML
    private TableView<ResourceEntryDataItem> resourcesTableView;

    private MainStageController mainStageController;

    private final ObservableList<ResourceEntryDataItem> resourceData = FXCollections.observableArrayList();

    private final Property<LocalizedResource> browsedResourceProperty = new SimpleObjectProperty<>();

    private SimpleStringProperty resourceReferenceProperty;

    private int fieldRank;

    private Locale currentLocale;

    @Override
    public void init() {
        browsedResourceProperty
                .addListener((observable, oldValue, newValue) -> handleBrowseToResource(newValue));

        initTopicPane();

        initTablePane();
    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleResourceTableMouseClick");

        if (MouseButton.PRIMARY == mouseEvent.getButton() && mouseEvent.getClickCount() == 2) {
            TableViewHelper.getMouseSelectedItem(mouseEvent, ResourceEntryDataItem.class)
                    .ifPresent(this::applyResourceSelectionToMainStageAndClose);
        }
    }

    @FXML
    private void handleSelectResourceButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleSelectResourceButtonMouseClick");

        ResourceEntryDataItem selectedResource = resourcesTableView.getSelectionModel().selectedItemProperty().getValue();
        if (selectedResource != null && resourceReferenceProperty != null) {
            applyResourceSelectionToMainStageAndClose(selectedResource);
        }
    }

    @FXML
    private void handleEditResourceButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleEditResourceButtonMouseClick");

        ofNullable(resourcesTableView.getSelectionModel().selectedItemProperty().getValue())
                .ifPresent(selectedResource -> {
                    String currentResourceReference = selectedResource.referenceProperty().get();
                    DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic()).<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException("Topic not found: " + getCurrentTopic()));
                    dialogsHelper.showEditResourceDialog(currentTopicObject, selectedResource, currentLocale)
                            .ifPresent(localizedResource -> editResourceAndUpdateMainStage(getCurrentTopic(), currentResourceReference, localizedResource));
                });
    }

    @FXML
    private void handleAddResourceButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleAddResourceButtonMouseClick");

        DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic()).<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException("Topic not found: " + getCurrentTopic()));
        dialogsHelper.showAddResourceDialog(currentTopicObject, currentLocale)
                .ifPresent(newLocalizedResource -> editNewResourceAndUpdateMainStage(getCurrentTopic(), newLocalizedResource));
    }

    @FXML
    private void handleRemoveResourceButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleRemoveResourceButtonMouseClick");

        ofNullable(resourcesTableView.getSelectionModel().selectedItemProperty().getValue())
                .ifPresent(selectedResource -> {
                    DbDto.Topic currentTopic = getCurrentTopic();
                    dialogsHelper.showResourceDeletionDialog(currentTopic, selectedResource, currentLocale)
                            .ifPresent(forAllLocales -> {
                                int selectedRow = resourcesTableView.getSelectionModel().getSelectedIndex();
                                removeResourceAndUpdateMainStage(currentTopic, selectedResource, currentLocale, forAllLocales, selectedRow);
                            });
                });
    }

    @FXML
    private void handleSearchEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchEntryButtonAction");

        askForReferenceAndSelectItem();
    }

    private void handleTopicChoiceChanged(DbDto.Topic newTopic) {
        Log.trace(THIS_CLASS_NAME, "->handleTopicChoiceChanged: " + newTopic);

        if (newTopic == null) {
            return;
        }

        updateResourcesStageData();
    }

    private void handleBrowseToResource(LocalizedResource newResource) {
        Log.trace(THIS_CLASS_NAME, "->handleBrowseToResource: " + newResource);

        topicsChoiceBox.setValue(newResource.getTopic()
                .<IllegalArgumentException>orElseThrow(IllegalArgumentException::new));

        selectResourceInTableAndScroll(newResource.getReference());
    }

    void initAndShowDialog(SimpleStringProperty referenceProperty, int entryFieldRank, Locale locale, DbDto.Topic targetTopic) {
        resourceReferenceProperty = referenceProperty;
        fieldRank = entryFieldRank;
        currentLocale = locale;

        topicsChoiceBox.getSelectionModel().clearSelection();

        browsedResourceProperty.setValue(new LocalizedResource(targetTopic, referenceProperty.get()));

        showWindow();
    }

    // TODO tests
    void editResourceAndUpdateMainStage(DbDto.Topic topic, String currentResourceReference, LocalizedResource newLocalizedResource) {
        // TODO necessary condition?
        if (newLocalizedResource == null) {
            return;
        }

        try {
            updateResource(topic, currentResourceReference, newLocalizedResource.getReferenceValuePair(), newLocalizedResource.getLocale());
        } catch (IllegalArgumentException iae) {
            Log.error(THIS_CLASS_NAME, "Unable to update resource", iae);
            CommonDialogsHelper.showDialog(Alert.AlertType.ERROR, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES, iae.getMessage(), DisplayConstants.MESSAGE_DIFFERENT_RESOURCE);
        } finally {
            updateAllStagesWithResourceReference(newLocalizedResource.getReferenceValuePair().getKey());
        }
    }

    // TODO tests
    void editNewResourceAndUpdateMainStage(DbDto.Topic topic, LocalizedResource newLocalizedResource) {
        // TODO necessary condition?
        if (newLocalizedResource == null) {
            return;
        }

        try {
            createResource(topic, newLocalizedResource.getReferenceValuePair(), newLocalizedResource.getLocale());
        } catch (IllegalArgumentException iae) {
            Log.error(THIS_CLASS_NAME, "Unable to create resource", iae);
            CommonDialogsHelper.showDialog(Alert.AlertType.ERROR, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES, iae.getMessage(), DisplayConstants.MESSAGE_DIFFERENT_RESOURCE);
        } finally {
            updateAllStagesWithResourceReference(newLocalizedResource.getReferenceValuePair().getKey());
        }
    }

    private void initTopicPane() {
        fillTopics();
        topicsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleTopicChoiceChanged(newValue));
    }

    private void initTablePane() {
        TableColumn<ResourceEntryDataItem, ?> refColumn = resourcesTableView.getColumns().get(0);
        refColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().referenceProperty());

        for (int columnIndex = 1; columnIndex < resourcesTableView.getColumns().size(); columnIndex++) {
            TableColumn<ResourceEntryDataItem, ?> valueColumn = resourcesTableView.getColumns().get(columnIndex);
            Locale locale = Locale.values()[columnIndex - 1];
            valueColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().valuePropertyForLocale(locale));
        }

        resourcesTableView.setItems(resourceData);
    }

    private void fillTopics() {
        final ObservableList<DbDto.Topic> items = topicsChoiceBox.getItems();
        DbDto.Topic.valuesAsStream().forEach(items::add);
    }

    private void selectResourceInTableAndScroll(String reference) {
        resourceData.stream()
                .filter(remoteResource -> remoteResource.referenceProperty().get().equals(reference))
                .findAny()
                .ifPresent(browsedResource -> {
                    resourcesTableView.getSelectionModel().select(browsedResource);
                    resourcesTableView.scrollTo(browsedResource);
                });
    }

    private void applyResourceSelectionToMainStageAndClose(ResourceEntryDataItem selectedResource) {
        String resourceReference = selectedResource.referenceProperty().getValue();
        resourceReferenceProperty.set(resourceReference);

        mainStageController.getChangeData().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, resourceReference);

        closeWindow();
    }

    private void removeResourceAndUpdateMainStage(DbDto.Topic topic, ResourceEntryDataItem selectedResource, Locale locale, boolean forAllLocales, int selectedRowIndex) {
        mainStageController.getChangeData().removeResourceWithReference(topic, locale, selectedResource.referenceProperty().getValue(), forAllLocales);

        updateAllStages();

        TableViewHelper.selectRowAndScroll(selectedRowIndex, resourcesTableView);
    }

    private void updateResource(DbDto.Topic topic, String currentRef, Pair<String, String> referenceValuePair, Optional<Locale> potentialAffectedLocale) {
        String newResourceReference = referenceValuePair.getKey();
        String newResourceValue = referenceValuePair.getValue();

        if (currentRef.equals(newResourceReference)) {
            if (potentialAffectedLocale.isPresent()) {
                mainStageController.getChangeData().updateResourceWithReferenceForLocale(topic, potentialAffectedLocale.get(), currentRef, newResourceValue);
            } else {
                mainStageController.getChangeData().updateResourceWithReferenceForAllLocales(topic, currentRef, newResourceValue);
            }
        } else {
            mainStageController.getChangeData().updateResourceWithReferenceForAllLocales(topic, currentRef, newResourceReference, newResourceValue);
        }
    }

    private void createResource(DbDto.Topic topic, Pair<String, String> referenceValuePair, Optional<Locale> potentialAffectedLocale) {
        String newResourceReference = referenceValuePair.getKey();
        String newResourceValue = referenceValuePair.getValue();

        if (potentialAffectedLocale.isPresent()) {
            mainStageController.getChangeData().addResourceWithReference(topic, potentialAffectedLocale.get(), newResourceReference, newResourceValue);
        } else {
            Locale.valuesAsStream()
                    .forEach(affectedLocale -> mainStageController.getChangeData().addResourceWithReference(topic, affectedLocale, newResourceReference, newResourceValue));
        }
    }

    private void updateAllStagesWithResourceReference(String resourceReference) {
        updateResourcesStageData();
        selectResourceInTableAndScroll(resourceReference);

        mainStageController.getViewData().updateAllPropertiesWithItemValues();
    }

    private void updateAllStages() {
        updateResourcesStageData();

        mainStageController.getViewData().updateAllPropertiesWithItemValues();
    }

    private void updateResourcesStageData() {
        resourceData.clear();

        DbDto.Topic currentTopic = getCurrentTopic();
        List<ResourceEntryDataItem> resourceEntryDataItems = getMiner().getResourcesFromTopic(currentTopic)
                .map(resourceObject -> resourceObject.getEntries().stream()
                        .map(entry -> {
                            ResourceEntryDataItem tableResource = new ResourceEntryDataItem();
                            tableResource.setReference(entry.getReference());

                            Locale.valuesAsStream()
                                    .forEach(locale -> tableResource.setValueForLocale(
                                            locale,
                                            entry.getValueForLocale(locale)
                                                    .orElse("")));

                            return tableResource;
                        })
                        .collect(toList()))
                .orElse(new ArrayList<>());

        resourceData.addAll(resourceEntryDataItems);
    }

    private void askForReferenceAndSelectItem() {
        CommonDialogsHelper.showInputValueDialog(
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SEARCH_RESOURCE_ENTRY,
                DisplayConstants.LABEL_SEARCH_ENTRY)
                .ifPresent(entryReference -> TableViewHelper.selectItemAndScroll(
                        oneItem -> oneItem.referenceProperty().getValue().equals(entryReference),
                        resourcesTableView));
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private DbDto.Topic getCurrentTopic() {
        return topicsChoiceBox.getValue();
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }

    void setTopicsChoiceBox(ChoiceBox<DbDto.Topic> topicsChoiceBox) {
        this.topicsChoiceBox = topicsChoiceBox;
    }
}

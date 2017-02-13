package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.common.javafx.scene.control.SearchValueDialog;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.gui.database.domain.javafx.ResourceEntryDataItem;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static fr.tduf.libunlimited.common.game.domain.Locale.fromOrder;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.size;
import static javafx.scene.control.Alert.AlertType.ERROR;

/**
 * Controller to display and modify database resources via dedicated dialog.
 */
public class ResourcesStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = ResourcesStageController.class.getSimpleName();

    private DialogsHelper dialogsHelper = new DialogsHelper();

    private SearchValueDialog searchValueDialog;

    @FXML
    private ChoiceBox<DbDto.Topic> topicsChoiceBox;

    @FXML
    private TableView<ResourceEntryDataItem> resourcesTableView;

    @FXML
    private Label resourceEntryCountLabel;

    private MainStageController mainStageController;

    private final ObservableList<ResourceEntryDataItem> resourceData = FXCollections.observableArrayList();

    private final Property<LocalizedResource> browsedResourceProperty = new SimpleObjectProperty<>();

    private StringProperty resourceReferenceProperty;

    private int fieldRank;

    private Locale currentLocale;

    @Override
    public void init() {
        searchValueDialog = new SearchValueDialog(DisplayConstants.TITLE_SEARCH_RESOURCE_ENTRY);
        browsedResourceProperty
                .addListener((observable, oldValue, newValue) -> handleBrowseToResource(newValue));

        initTopicPane();

        initTablePane();

        initStatusBar();
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
                    DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic())
                            .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + getCurrentTopic()));
                    dialogsHelper.showEditResourceDialog(currentTopicObject, selectedResource, currentLocale)
                            .ifPresent(localizedResource -> editResourceAndUpdateMainStage(getCurrentTopic(), currentResourceReference, localizedResource));
                });
    }

    @FXML
    private void handleAddResourceButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleAddResourceButtonMouseClick");

        DbDto currentTopicObject = getMiner().getDatabaseTopic(getCurrentTopic())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + getCurrentTopic()));
        dialogsHelper.showAddResourceDialog(currentTopicObject, currentLocale)
                .ifPresent(newLocalizedResource -> editNewResourceAndUpdateMainStage(getCurrentTopic(), newLocalizedResource));
    }

    @FXML
    private void handleRemoveResourceButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleRemoveResourceButtonMouseClick");

        ofNullable(resourcesTableView.getSelectionModel().selectedItemProperty().getValue())
                .ifPresent(selectedResource -> {
                    int selectedRow = resourcesTableView.getSelectionModel().getSelectedIndex();
                    removeResourceAndUpdateMainStage(getCurrentTopic(), selectedResource, selectedRow);
                });
    }

    @FXML
    private void handleSearchEntryByREFAction() {
        askForReferenceAndSelectItem();
    }

    @FXML
    private void handleSearchEntryByValueAction() {
        openSearchValueDialog();
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
                .orElseThrow(IllegalArgumentException::new));

        selectResourceInTableAndScroll(newResource.getReference());
    }

    void initAndShowDialog(StringProperty referenceProperty, int entryFieldRank, Locale locale, DbDto.Topic targetTopic) {
        resourceReferenceProperty = referenceProperty;
        fieldRank = entryFieldRank;
        currentLocale = locale;

        topicsChoiceBox.getSelectionModel().clearSelection();

        browsedResourceProperty.setValue(new LocalizedResource(targetTopic, referenceProperty.get()));

        showWindow();
    }

    void editResourceAndUpdateMainStage(DbDto.Topic topic, String currentResourceReference, LocalizedResource newLocalizedResource) {
        try {
            updateResource(topic, currentResourceReference, newLocalizedResource.getReferenceValuePair(), newLocalizedResource.getLocale());
        } catch (IllegalArgumentException iae) {
            Log.error(THIS_CLASS_NAME, "Unable to update resource", iae);
            showResourceErrorDialog(iae);
        } finally {
            updateAllStagesWithResourceReference(newLocalizedResource.getReferenceValuePair().getKey());
        }
    }

    void editNewResourceAndUpdateMainStage(DbDto.Topic topic, LocalizedResource newLocalizedResource) {
        try {
            createResource(topic, newLocalizedResource.getReferenceValuePair(), newLocalizedResource.getLocale());
        } catch (IllegalArgumentException iae) {
            Log.error(THIS_CLASS_NAME, "Unable to create resource", iae);
            showResourceErrorDialog(iae);
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
            Locale locale = fromOrder(columnIndex);
            valueColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().valuePropertyForLocale(locale));
        }

        resourcesTableView.setItems(resourceData);
    }

    private void initStatusBar() {
        resourceEntryCountLabel.textProperty().bind(size(resourceData).asString());
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

    private void removeResourceAndUpdateMainStage(DbDto.Topic topic, ResourceEntryDataItem selectedResource, int selectedRowIndex) {
        mainStageController.getChangeData().removeResourceWithReference(topic, selectedResource.referenceProperty().getValue());

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

        MainStageChangeDataController changeDataController = mainStageController.getChangeData();
        if (potentialAffectedLocale.isPresent()) {
            changeDataController.addResourceWithReference(topic, potentialAffectedLocale.get(), newResourceReference, newResourceValue);
        } else {
            changeDataController.addResourceWithReference(topic, DEFAULT, newResourceReference, newResourceValue);
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
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SEARCH_RESOURCE_ENTRY,
                DisplayConstants.LABEL_SEARCH_ENTRY, getWindow())
                .ifPresent(entryReference -> TableViewHelper.selectItemAndScroll(
                        oneItem -> oneItem.referenceProperty().getValue().equals(entryReference),
                        resourcesTableView));
    }

    private void openSearchValueDialog() {
        // TODO Move callbacks to init
        Function<String, Boolean> nextResult = pattern -> TableViewHelper.selectItemAndScroll((resource, rowIndex) -> {
            int currentRowIndex = resourcesTableView.getSelectionModel().getSelectedIndex();
            return (rowIndex > currentRowIndex)
                    &&
                    Locale.valuesAsStream()
                            .map(resource::valuePropertyForLocale)
                            .map(StringExpression::getValue)
                            .anyMatch(resourceValue -> StringUtils.containsIgnoreCase(resourceValue, pattern));
        }, resourcesTableView).isPresent();
        Function<String, Boolean> firstResult = pattern -> TableViewHelper.selectItemAndScroll(resource -> Locale.valuesAsStream()
                .map(resource::valuePropertyForLocale)
                .map(StringExpression::getValue)
                .anyMatch(resourceValue -> StringUtils.containsIgnoreCase(resourceValue, pattern)), resourcesTableView)
                .isPresent();

        searchValueDialog.setCallbacks(firstResult, nextResult);
        searchValueDialog.show(getWindow());
    }

    private void showResourceErrorDialog(IllegalArgumentException iae) {
        SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(ERROR)
                .withTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES)
                .withMessage(iae.getMessage())
                .withDescription(DisplayConstants.MESSAGE_DIFFERENT_RESOURCE)
                .build();
        CommonDialogsHelper.showDialog(dialogOptions, getWindow());
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

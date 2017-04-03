package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static fr.tduf.gui.database.common.DisplayConstants.LABEL_SEARCH_ENTRY;
import static fr.tduf.gui.database.common.DisplayConstants.TITLE_SEARCH_CONTENTS_ENTRY;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * FX Controller for content entry selection dialog
 */
public class EntriesStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = EntriesStageController.class.getSimpleName();

    @FXML
    private Label currentTopicLabel;

    @FXML
    private Label instructionsLabel;

    @FXML
    private Button selectMultiButton;

    @FXML
    private TableView<ContentEntryDataItem> entriesTableView;

    private MainStageController mainStageController;

    private ObservableList<ContentEntryDataItem> entriesData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private OptionalInt fieldRankForUpdate = OptionalInt.empty();

    private List<ContentEntryDataItem> selectedEntries = new ArrayList<>();

    private boolean multiSelectMode;

    @Override
    public void init() {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleEntriesTableMouseClick");

        if (!multiSelectMode && MouseButton.PRIMARY == mouseEvent.getButton()) {
            TableViewHelper.getMouseSelectedItem(mouseEvent, ContentEntryDataItem.class)
                    .ifPresent(this::applySingleEntrySelectionToMainStageAndClose);
        }
    }

    @FXML
    private void handleSelectEntriesButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleSelectEntriesButtonMouseClick");

        selectedEntries.addAll(entriesTableView.getSelectionModel().getSelectedItems());

        closeWindow();
    }

    @FXML
    private void handleSearchEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchEntryButtonAction");

        askForReferenceAndSelectItem();
    }

    void initAndShowDialog(String entryReference, int entryFieldRank, DbDto.Topic topic, List<Integer> labelFieldRanks) {
        switchMultiSelectMode(false);

        fieldRankForUpdate = OptionalInt.of(entryFieldRank);

        currentTopicProperty.setValue(topic);

        updateEntriesStageData(labelFieldRanks);

        selectedEntries.clear();

        selectEntryInTableAndScroll(entryReference);

        showWindow();
    }

    Optional<ContentEntryDataItem> initAndShowModalDialog(String entryReference, DbDto.Topic topic, String targetProfileName) {
        initAndShowModalDialog(entryReference, topic, targetProfileName, false);

        return selectedEntries.stream().findAny();
    }

    List<ContentEntryDataItem> initAndShowModalDialogForMultiSelect(String entryReference, DbDto.Topic topic, String targetProfileName) {
        initAndShowModalDialog(entryReference, topic, targetProfileName, true);

        return selectedEntries;
    }

    private void initAndShowModalDialog(String entryReference, DbDto.Topic topic, String targetProfileName, boolean multiSelect) {
        switchMultiSelectMode(multiSelect);

        fieldRankForUpdate = OptionalInt.empty();

        currentTopicProperty.setValue(topic);

        List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, mainStageController.getLayoutObject()).getEntryLabelFieldRanks();
        updateEntriesStageData(labelFieldRanks);

        selectedEntries.clear();

        ofNullable(entryReference)
                .ifPresent(this::selectEntryInTableAndScroll);

        showModalWindow();
    }

    private void switchMultiSelectMode(boolean multiSelectEnable) {
        multiSelectMode = multiSelectEnable;

        selectMultiButton.setVisible(multiSelectEnable);

        instructionsLabel.setText( multiSelectEnable ?
                DisplayConstants.LABEL_ENTRY_SELECT_MANY :
                DisplayConstants.LABEL_ENTRY_SELECT_SINGLE );

        entriesTableView.getSelectionModel().setSelectionMode(multiSelectEnable ?
                SelectionMode.MULTIPLE :
                SelectionMode.SINGLE);
    }

    private void initHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
    }

    private void initTablePane() {
        TableColumn<ContentEntryDataItem, ?> refColumn = entriesTableView.getColumns().get(0);
        //noinspection unchecked
        refColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().referenceProperty());

        TableColumn<ContentEntryDataItem, ?> valueColumn = entriesTableView.getColumns().get(1);
        //noinspection unchecked
        valueColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().valueProperty());

        entriesTableView.setItems(entriesData);
    }

    private void selectEntryInTableAndScroll(String entryReference) {
        entriesData.stream()
                .filter(resource -> resource.referenceProperty().get().equals(entryReference))
                .findAny()
                .ifPresent(browsedResource -> {
                    entriesTableView.getSelectionModel().select(browsedResource);
                    entriesTableView.scrollTo(browsedResource);
                });
    }

    private void updateEntriesStageData(List<Integer> labelFieldRanks) {
        entriesData.clear();

        DbDto.Topic topic = currentTopicProperty.getValue();
        getMiner().getDatabaseTopic(topic)
                .ifPresent(topicObject -> entriesData.addAll(fetchEntriesItems(labelFieldRanks, topic, topicObject)));
    }

    private List<ContentEntryDataItem> fetchEntriesItems(List<Integer> labelFieldRanks, DbDto.Topic topic, DbDto topicObject) {
        return topicObject.getData().getEntries().stream()
            .map(entry -> {
                ContentEntryDataItem contentEntryDataItem = new ContentEntryDataItem();

                int entryInternalIdentifier = entry.getId();
                contentEntryDataItem.setInternalEntryId(entryInternalIdentifier);

                String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                        entryInternalIdentifier,
                        topic,
                        mainStageController.getViewData().currentLocaleProperty.getValue(),
                        labelFieldRanks,
                        getMiner(),
                        getLayoutObject());
                contentEntryDataItem.setValue(entryValue);

                getMiner().getContentEntryReferenceWithInternalIdentifier(entryInternalIdentifier, topic)
                        .ifPresent(contentEntryDataItem::setReference);

                return contentEntryDataItem;
            })
            .collect(toList());
    }

    // Ignore warning (method reference)
    private void applySingleEntrySelectionToMainStageAndClose(ContentEntryDataItem selectedEntry) {
        selectedEntries.add(selectedEntry);

        fieldRankForUpdate.ifPresent(fieldRank -> {
            // Update mode: will update a particular field in main stage
            String entryReference = selectedEntry.referenceProperty().getValue();
            mainStageController.getChangeData().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, entryReference);
        });

        closeWindow();
    }

    private void askForReferenceAndSelectItem() {
        CommonDialogsHelper.showInputValueDialog(TITLE_SEARCH_CONTENTS_ENTRY, LABEL_SEARCH_ENTRY, getWindow())
                .ifPresent(entryReference -> TableViewHelper.selectItemAndScroll(
                        (oneItem, row) -> oneItem.referenceProperty().getValue().equals(entryReference),
                        entriesTableView));
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }

    private EditorLayoutDto getLayoutObject() {
        return mainStageController.getLayoutObject();
    }
}

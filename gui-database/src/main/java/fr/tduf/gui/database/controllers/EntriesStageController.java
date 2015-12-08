package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.gui.common.helper.javafx.CommonDialogsHelper;
import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.stream.Collectors.toList;

public class EntriesStageController extends AbstractGuiController {

    private static final String THIS_CLASS_NAME = EntriesStageController.class.getSimpleName();

    @FXML
    private Label currentTopicLabel;

    @FXML
    TableView<ContentEntryDataItem> entriesTableView;

    private MainStageController mainStageController;

    private ObservableList<ContentEntryDataItem> entriesData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private OptionalInt fieldRankForUpdate = OptionalInt.empty();

    private Optional<ContentEntryDataItem> selectedEntry = Optional.empty();

    @Override
    public void init() {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleEntriesTableMouseClick");

        if (MouseButton.PRIMARY == mouseEvent.getButton()) {
            TableViewHelper.getMouseSelectedItem(mouseEvent, ContentEntryDataItem.class)
                    .ifPresent(this::applyEntrySelectionToMainStageAndClose);
        }
    }

    @FXML
    private void handleSearchEntryButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchEntryButtonAction");

        askForReferenceAndSelectItem();
    }

    void initAndShowDialog(String entryReference, int entryFieldRank, DbDto.Topic topic, List<Integer> labelFieldRanks) {
        fieldRankForUpdate = OptionalInt.of(entryFieldRank);

        currentTopicProperty.setValue(topic);

        updateEntriesStageData(labelFieldRanks);

        showWindow();

        selectEntryInTableAndScroll(entryReference);
    }

    Optional<ContentEntryDataItem> initAndShowModalDialog(DbDto.Topic topic, String targetProfileName) {
        fieldRankForUpdate = OptionalInt.empty();

        currentTopicProperty.setValue(topic);

        List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, mainStageController.getLayoutObject()).getEntryLabelFieldRanks();
        updateEntriesStageData(labelFieldRanks);

        selectedEntry = Optional.empty();

        showModalWindow();

        return selectedEntry;
    }

    private void initHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
    }

    private void initTablePane() {
        TableColumn<ContentEntryDataItem, ?> refColumn = entriesTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().referenceProperty());

        TableColumn<ContentEntryDataItem, ?> valueColumn = entriesTableView.getColumns().get(1);
        valueColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().valueProperty());

        entriesTableView.setItems(entriesData);
    }

    private void selectEntryInTableAndScroll(String entryReference) {
        entriesData.stream()

                .filter((resource) -> resource.referenceProperty().get().equals(entryReference))

                .findAny()

                .ifPresent((browsedResource) -> {
                    entriesTableView.getSelectionModel().select(browsedResource);
                    entriesTableView.scrollTo(browsedResource);
                });
    }

    private void updateEntriesStageData(List<Integer> labelFieldRanks) {
        entriesData.clear();

        DbDto.Topic topic = currentTopicProperty.getValue();
        getMiner().getDatabaseTopic(topic)
                .ifPresent((topicObject) -> entriesData.addAll(topicObject.getData().getEntries().stream()

                                .map((entry) -> {
                                    ContentEntryDataItem contentEntryDataItem = new ContentEntryDataItem();

                                    long entryInternalIdentifier = entry.getId();
                                    contentEntryDataItem.setInternalEntryId(entryInternalIdentifier);

                                    String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(entryInternalIdentifier, topic, mainStageController.currentLocaleProperty.getValue(), labelFieldRanks, getMiner());
                                    contentEntryDataItem.setValue(entryValue);

                                    String entryReference = getMiner().getContentEntryReferenceWithInternalIdentifier(entryInternalIdentifier, topic).get();
                                    contentEntryDataItem.setReference(entryReference);

                                    return contentEntryDataItem;
                                })

                                .collect(toList()))
                );
    }

    private void applyEntrySelectionToMainStageAndClose(ContentEntryDataItem selectedEntry) {
        this.selectedEntry = Optional.of(selectedEntry);

        fieldRankForUpdate.ifPresent((fieldRank) -> {
            // Update mode: will update a particular field in main stage
            String entryReference = selectedEntry.referenceProperty().getValue();
            mainStageController.getChangeDataController().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, entryReference);
        });

        closeWindow();
    }

    private void askForReferenceAndSelectItem() {
        CommonDialogsHelper.showInputValueDialog(
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SEARCH_ENTRY,
                DisplayConstants.LABEL_SEARCH_ENTRY)

                .ifPresent((entryReference) -> TableViewHelper.selectItemAndScroll(
                        oneItem -> oneItem.referenceProperty().getValue().equals(entryReference),
                        entriesTableView));
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }
}
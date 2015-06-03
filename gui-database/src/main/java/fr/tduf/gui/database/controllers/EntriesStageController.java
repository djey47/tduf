package fr.tduf.gui.database.controllers;

import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class EntriesStageController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private Label currentTopicLabel;

    @FXML
    TableView<RemoteResource> entriesTableView;

    private MainStageController mainStageController;

    private ObservableList<RemoteResource> entriesData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private OptionalInt fieldRankForUpdate = OptionalInt.empty();

    private Optional<RemoteResource> selectedEntry = Optional.empty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        System.out.println("handleEntriesTableMouseClick");

        if (MouseButton.PRIMARY == mouseEvent.getButton()) {
            Optional<RemoteResource> potentialSelectedEntry = TableViewHelper.getMouseSelectedItem(mouseEvent);
            if (potentialSelectedEntry.isPresent()) {
                selectedEntry = potentialSelectedEntry;

                applyEntrySelectionToMainStageAndClose(potentialSelectedEntry.get());
            }
        }
    }

    void initAndShowDialog(String entryReference, int entryFieldRank, DbDto.Topic topic, List<Integer> labelFieldRanks) {
        fieldRankForUpdate = OptionalInt.of(entryFieldRank);

        currentTopicProperty.setValue(topic);

        updateEntriesStageData(labelFieldRanks);

        Stage stage = (Stage) root.getScene().getWindow();
        stage.show();

        selectEntryInTableAndScroll(entryReference);
    }

    Optional<RemoteResource> initAndShowModalDialog(DbDto.Topic topic, String targetProfileName) {
        fieldRankForUpdate = OptionalInt.empty();

        currentTopicProperty.setValue(topic);

        List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, mainStageController.getLayoutObject()).getEntryLabelFieldRanks();
        updateEntriesStageData(labelFieldRanks);

        Stage stage = (Stage) root.getScene().getWindow();
        stage.showAndWait();

        return selectedEntry;
    }

    private void initHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
    }

    private void initTablePane() {
        TableColumn<RemoteResource, String> refColumn = (TableColumn<RemoteResource, String>) entriesTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());

        TableColumn<RemoteResource, String> valueColumn = (TableColumn<RemoteResource, String>) entriesTableView.getColumns().get(1);
        valueColumn.setCellValueFactory((cellData) -> cellData.getValue().valueProperty());

        entriesTableView.setItems(entriesData);
    }

    private void selectEntryInTableAndScroll(String entryReference) {
        RemoteResource browsedResource = entriesData.stream()

                .filter((resource) -> resource.referenceProperty().get().equals(entryReference))

                .findAny().get();

        entriesTableView.getSelectionModel().select(browsedResource);
        entriesTableView.scrollTo(browsedResource);
    }

    private void updateEntriesStageData(List<Integer> labelFieldRanks) {
        entriesData.clear();

        DbDto.Topic topic = currentTopicProperty.getValue();
        getMiner().getDatabaseTopic(topic)
                .ifPresent((topicObject) -> entriesData.addAll(topicObject.getData().getEntries().stream()

                        .map((entry) -> {
                            RemoteResource remoteResource = new RemoteResource();

                            long entryInternalIdentifier = entry.getId();
                            remoteResource.setInternalEntryId(entryInternalIdentifier);

                            String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(entryInternalIdentifier, topic, mainStageController.currentLocaleProperty.getValue(), labelFieldRanks, getMiner());
                            remoteResource.setValue(entryValue);

                            String entryReference = getMiner().getContentEntryRefWithInternalIdentifier(entryInternalIdentifier, topic).get();
                            remoteResource.setReference(entryReference);

                            return remoteResource;
                        })

                        .collect(toList()))
                );
    }

    private void applyEntrySelectionToMainStageAndClose(RemoteResource selectedEntry) {
        fieldRankForUpdate.ifPresent((fieldRank) -> {
            // Update mode: will update a particular field in main stage
            String entryReference = selectedEntry.referenceProperty().getValue();
            mainStageController.getChangeDataController().updateContentItem(mainStageController.getCurrentTopicObject().getTopic(), fieldRank, entryReference);
        });

        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }
}
package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class EntriesStageController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TableView<RemoteResource> entriesTableView;

    private MainStageController mainStageController;

    private ObservableList<RemoteResource> entriesData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTablePane();

    }

    @FXML
    private void handleResourceTableMouseClick(MouseEvent mouseEvent) {
        System.out.println("handleEntriesTableMouseClick");

    }

    @FXML
    private void handleSelectEntryButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleSelectEntryButtonMouseClick");

    }

    void initAndShowDialog(DbDto.Topic targetTopic, List<Integer> labelFieldRanks) {
        updateResourcesStageData(targetTopic, labelFieldRanks);

        Stage stage = (Stage)root.getScene().getWindow();
        stage.show();
    }

    private void initTablePane() {
        TableColumn<RemoteResource, String> refColumn = (TableColumn<RemoteResource, String>) entriesTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());

        TableColumn<RemoteResource, String> valueColumn = (TableColumn<RemoteResource, String>) entriesTableView.getColumns().get(1);
        valueColumn.setCellValueFactory((cellData) -> cellData.getValue().valueProperty());

        entriesTableView.setItems(entriesData);
    }

    private void updateResourcesStageData(DbDto.Topic topic, List<Integer> labelFieldRanks) {
        entriesData.clear();

        getMiner().getDatabaseTopic(topic)
                .ifPresent((topicObject) -> entriesData.addAll(topicObject.getData().getEntries().stream()

                                .map((entry) -> {
                                    RemoteResource remoteResource = new RemoteResource();

                                    long entryInternalIdentifier = entry.getId();
                                    Integer refFieldRank = BulkDatabaseMiner.getUidFieldRank(topicObject.getStructure().getFields()).get();
                                    String entryReference =   getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic, refFieldRank, entryInternalIdentifier).get().getRawValue();
                                    String entryValue = fetchContentsWithEntryId(topic, entryInternalIdentifier, labelFieldRanks);

                                    remoteResource.setReference(entryReference);
                                    remoteResource.setValue(entryValue);

                                    return remoteResource;
                                })

                                .collect(toList()))
                );
    }

    // TODO factorize with viewdata controller
    private String fetchContentsWithEntryId(DbDto.Topic topic, long entryId, List<Integer> fieldRanks) {
        requireNonNull(fieldRanks, "A list of field ranks (even empty) must be provided.");

        if (fieldRanks.isEmpty()) {
            return DisplayConstants.VALUE_UNKNOWN;
        }

        List<String> contents = fieldRanks.stream()

                .map((fieldRank) -> {
                    Optional<DbResourceDto.Entry> potentialRemoteResourceEntry = getMiner().getResourceEntryWithInternalIdentifier(topic, fieldRank, entryId, mainStageController.currentLocaleProperty.getValue());
                    if (potentialRemoteResourceEntry.isPresent()) {
                        return potentialRemoteResourceEntry.get().getValue();
                    }

                    return this.getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic, fieldRank, entryId).get().getRawValue();
                })

                .collect(toList());

        return String.join(DisplayConstants.SEPARATOR_VALUES, contents);
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }
}
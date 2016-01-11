package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.domain.javafx.ContentFieldDataItem;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class FieldsBrowserStageController extends AbstractGuiController {

    private static final String THIS_CLASS_NAME = FieldsBrowserStageController.class.getSimpleName();

    @FXML
    private Label currentTopicLabel;

    @FXML
    TableView<ContentFieldDataItem> fieldsTableView;

    private MainStageController mainStageController;

    private ObservableList<ContentFieldDataItem> fieldsData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private List<ContentFieldDataItem> selectedFields = new ArrayList<>();

    @Override
    public void init() {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleSelectFieldsButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleSelectFieldsButtonMouseClick");

        selectedFields.addAll(fieldsTableView.getSelectionModel().getSelectedItems());

        closeWindow();
    }

    List<ContentFieldDataItem> initAndShowModalDialog(DbDto.Topic topic, String targetProfileName) {
        currentTopicProperty.setValue(topic);

        List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, mainStageController.getLayoutObject()).getEntryLabelFieldRanks();
        updateFieldsBrowserStageData(labelFieldRanks);

        selectedFields.clear();

        showModalWindow();

        return selectedFields;
    }

    private void initHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();

        currentTopicLabel.textProperty().bindBidirectional(currentTopicProperty, new DatabaseTopicToStringConverter());
    }

    private void initTablePane() {
        TableColumn<ContentFieldDataItem, ?> rankColumn = fieldsTableView.getColumns().get(0);
        rankColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().rankProperty());

        TableColumn<ContentFieldDataItem, ?> nameColumn = fieldsTableView.getColumns().get(1);
        nameColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().nameProperty());

        TableColumn<ContentFieldDataItem, ?> helpColumn = fieldsTableView.getColumns().get(2);
        helpColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().helpProperty());

        fieldsTableView.setItems(fieldsData);

        fieldsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void updateFieldsBrowserStageData(List<Integer> labelFieldRanks) {
        fieldsData.clear();

        DbDto.Topic topic = currentTopicProperty.getValue();
        getMiner().getDatabaseTopic(topic)
                .ifPresent((topicObject) -> fieldsData.addAll(topicObject.getStructure().getFields().stream()

                                .map((structureField) -> {
                                    ContentFieldDataItem contentFieldDataItem = new ContentFieldDataItem();

                                    final int fieldRank = structureField.getRank();

                                    contentFieldDataItem.setRank(fieldRank);
                                    contentFieldDataItem.setName(structureField.getName());

                                    EditorLayoutHelper.getFieldSettingsByRankAndProfileName(fieldRank,
                                            mainStageController.getCurrentProfileObject().getName(),
                                            mainStageController.getLayoutObject())

                                            .ifPresent((fieldSettings) -> contentFieldDataItem.setHelp(fieldSettings.getToolTip()));

                                    return contentFieldDataItem;
                                })

                                .collect(toList()))
                );
    }

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }
}

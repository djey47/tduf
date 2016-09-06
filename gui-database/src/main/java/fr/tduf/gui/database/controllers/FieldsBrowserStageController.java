package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
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

// TODO apply code rules
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

    @FXML
    private void handleSelectAllFieldsButtonMouseClick() {
        Log.trace(THIS_CLASS_NAME, "->handleSelectAllFieldsButtonMouseClick");

        closeWindow();
    }

    /**
     * @return list of selected fields to export, or empty if global export should be performed
     */
    List<ContentFieldDataItem> initAndShowModalDialog(DbDto.Topic topic) {
        currentTopicProperty.setValue(topic);

        updateFieldsBrowserStageData();

        selectedFields.clear();

        fieldsTableView.getSelectionModel().selectAll();

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

    private void updateFieldsBrowserStageData() {
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
                                            mainStageController.getViewData().currentProfileProperty.getValue().getName(),
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

package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javafx.geometry.Orientation.VERTICAL;

/**
 * Helper class to be used to generate Editor controls for linked fields at runtime.
 */
public class DynamicLinkControlsHelper extends AbstractDynamicControlsHelper {

    public DynamicLinkControlsHelper(MainStageController controller) {
        super(controller);
    }

    public void addAllLinksControls(EditorLayoutDto.EditorProfileDto currentProfileObject) {
        currentProfileObject.getTopicLinks().stream()

                .sorted((topicLinkObject1, topicLinkObject2) -> Integer.compare(topicLinkObject2.getPriority(), topicLinkObject1.getPriority()))

                .forEach((topicLinkObject) -> addLinkControls(controller.getDefaultTab(), topicLinkObject, controller.getResourceListByTopicLink()));
    }

    private void addLinkControls(VBox defaultTab, TopicLinkDto topicLinkObject, Map<TopicLinkDto, ObservableList<RemoteResource>> resourceListByTopicLinkIndex) {
        ObservableList<RemoteResource> resourceData = FXCollections.observableArrayList();
        resourceListByTopicLinkIndex.put(topicLinkObject, resourceData);

        HBox fieldBox = addFieldBox(Optional.ofNullable(topicLinkObject.getGroup()), 250.0, defaultTab);

        addFieldLabelForLinkedTopic(fieldBox, topicLinkObject);

        String targetProfileName = topicLinkObject.getRemoteReferenceProfile();
        DbDto.Topic targetTopic = retrieveTargetTopicForLink(topicLinkObject);
        TableView<RemoteResource> tableView = addTableViewForLinkedTopic(fieldBox, resourceData, targetProfileName, targetTopic);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addButtonsForLinkedTopic(fieldBox, targetProfileName, targetTopic, tableView.getSelectionModel());
    }

    private TableView<RemoteResource> addTableViewForLinkedTopic(HBox fieldBox, ObservableList<RemoteResource> resourceData, String targetProfileName, DbDto.Topic targetTopic) {
        TableView<RemoteResource> tableView = new TableView<>();
        tableView.setPrefWidth(560);

        TableColumn<RemoteResource, String> refColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_REF);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());
        refColumn.setPrefWidth(100);

        TableColumn<RemoteResource, String> valueColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_DATA);
        valueColumn.setCellValueFactory((cellData) -> cellData.getValue().valueProperty());
        valueColumn.setPrefWidth(455);

        tableView.getColumns().add(refColumn);
        tableView.getColumns().add(valueColumn);

        tableView.setItems(resourceData);

        if (targetProfileName != null) {
            tableView.setOnMousePressed(
                    controller.handleLinkTableMouseClick(targetProfileName, targetTopic));
        }

        fieldBox.getChildren().add(tableView);

        return tableView;
    }

    private void addButtonsForLinkedTopic(HBox fieldBox, String targetProfileName, DbDto.Topic targetTopic, TableView.TableViewSelectionModel<RemoteResource> tableSelectionModel) {
        VBox buttonsBox = new VBox(5);

        if (targetProfileName != null) {
            addGoToReferenceButton(
                    buttonsBox,
                    controller.handleGotoReferenceButtonMouseClick(targetTopic, tableSelectionModel, targetProfileName));
            addAddLinkedEntryButton(
                    buttonsBox,
                    controller.handleAddLinkedEntryButtonMouseClick());
            addRemoveLinkedEntryButton(
                    buttonsBox,
                    controller.handleRemoveLinkedEntryButtonMouseClick());
        }

        fieldBox.getChildren().add(buttonsBox);
    }

    private void addAddLinkedEntryButton(Pane fieldPane, EventHandler<ActionEvent> addLinkedEntryAction) {
        Button addLinkedEntryButton = new Button(DisplayConstants.LABEL_BUTTON_PLUS);
        addLinkedEntryButton.setPrefWidth(34);

        addLinkedEntryButton.setOnAction(addLinkedEntryAction);

        fieldPane.getChildren().add(addLinkedEntryButton);
    }

    private void addRemoveLinkedEntryButton(Pane fieldPane, EventHandler<ActionEvent> removeLinkedEntryAction) {
        Button removeLinkedEntryButton = new Button(DisplayConstants.LABEL_BUTTON_MINUS);
        removeLinkedEntryButton.setPrefWidth(34);

        removeLinkedEntryButton.setOnAction(removeLinkedEntryAction);

        fieldPane.getChildren().add(removeLinkedEntryButton);
    }

    private DbDto.Topic retrieveTargetTopicForLink(TopicLinkDto topicLinkObject) {
        List<DbStructureDto.Field> structureFields = getMiner().getDatabaseTopic(topicLinkObject.getTopic()).get().getStructure().getFields();
        DbDto.Topic targetTopic = topicLinkObject.getTopic();
        if (structureFields.size() == 2) {
            String targetRef = structureFields.get(1).getTargetRef();
            if (targetRef != null) {
                targetTopic = getMiner().getDatabaseTopicFromReference(targetRef).getTopic();
            }
        }
        return targetTopic;
    }

    private BulkDatabaseMiner getMiner() {
            return controller.getMiner();
    }

    private static void addFieldLabelForLinkedTopic(HBox fieldBox, TopicLinkDto topicLinkObject) {
        String fieldName = topicLinkObject.getTopic().name();
        if (topicLinkObject.getLabel() != null) {
            fieldName = topicLinkObject.getLabel();
        }
        addFieldLabel(fieldBox, false, fieldName);
    }
}
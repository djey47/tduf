package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.domain.DatabaseEntry;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
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

    private void addLinkControls(VBox defaultTab, TopicLinkDto topicLinkObject, Map<TopicLinkDto, ObservableList<DatabaseEntry>> resourceListByTopicLinkIndex) {
        ObservableList<DatabaseEntry> resourceData = FXCollections.observableArrayList();
        resourceListByTopicLinkIndex.put(topicLinkObject, resourceData);

        HBox fieldBox = addFieldBox(Optional.ofNullable(topicLinkObject.getGroup()), 250.0, defaultTab);

        addFieldLabelForLinkedTopic(fieldBox, topicLinkObject);

        String targetProfileName = topicLinkObject.getRemoteReferenceProfile();
        DbDto.Topic targetTopic = retrieveTargetTopicForLink(topicLinkObject);
        TableView<DatabaseEntry> tableView = addTableViewForLinkedTopic(fieldBox, resourceData, targetProfileName, targetTopic);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addButtonsForLinkedTopic(fieldBox, targetProfileName, targetTopic, tableView.getSelectionModel(), topicLinkObject);
    }

    private TableView<DatabaseEntry> addTableViewForLinkedTopic(HBox fieldBox, ObservableList<DatabaseEntry> resourceData, String targetProfileName, DbDto.Topic targetTopic) {
        TableView<DatabaseEntry> tableView = new TableView<>();
        tableView.setPrefWidth(560);

        TableColumn<DatabaseEntry, String> refColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_REF);
        refColumn.setCellValueFactory((cellData) -> cellData.getValue().referenceProperty());
        refColumn.setPrefWidth(100);

        TableColumn<DatabaseEntry, String> valueColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_DATA);
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

    private void addButtonsForLinkedTopic(HBox fieldBox, String targetProfileName, DbDto.Topic targetTopic, TableView.TableViewSelectionModel<DatabaseEntry> tableSelectionModel, TopicLinkDto topicLinkObject) {
        VBox buttonsBox = new VBox(5);

        if (targetProfileName != null) {
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_GOTO,
                    DisplayConstants.TOOLTIP_BUTTON_GOTO_SELECTED_ENTRY,
                    controller.handleGotoReferenceButtonMouseClick(tableSelectionModel, targetTopic, targetProfileName));
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_PLUS,
                    DisplayConstants.TOOLTIP_BUTTON_ADD_LINKED_ENTRY,
                    controller.handleAddLinkedEntryButtonMouseClick(tableSelectionModel, targetTopic, targetProfileName, topicLinkObject));
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_MINUS,
                    DisplayConstants.TOOLTIP_BUTTON_DELETE_LINKED_ENTRY,
                    controller.handleRemoveLinkedEntryButtonMouseClick(tableSelectionModel, topicLinkObject));
        }

        fieldBox.getChildren().add(buttonsBox);
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
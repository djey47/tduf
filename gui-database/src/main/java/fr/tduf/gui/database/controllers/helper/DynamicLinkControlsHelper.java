package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.main.MainStageChangeDataController;
import fr.tduf.gui.database.controllers.main.MainStageController;
import fr.tduf.gui.database.domain.ItemViewModel;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.tduf.gui.database.common.FxConstants.CSS_CLASS_TABLEVIEW;
import static java.util.Optional.ofNullable;
import static javafx.geometry.Orientation.VERTICAL;

/**
 * Helper class to be used to generate Editor controls for linked fields at runtime.
 */
public class DynamicLinkControlsHelper extends AbstractDynamicControlsHelper {

    /**
     * Main constructor
     * @param controller    : main controller
     */
    public DynamicLinkControlsHelper(MainStageController controller) {
        super(controller);
    }

    /**
     * Create FX controls for linked entries
     * @param profileObject : selected profile
     */
    public void addAllLinksControls(EditorLayoutDto.EditorProfileDto profileObject) {
        AtomicInteger linkId = new AtomicInteger(0);
        profileObject.getTopicLinks().stream()
                .sorted((topicLinkObject1, topicLinkObject2) -> Integer.compare(topicLinkObject2.getPriority(), topicLinkObject1.getPriority()))
                .forEach(topicLinkObject -> {
                    topicLinkObject.setId(linkId.decrementAndGet());
                    addLinkControls(topicLinkObject, controller.getViewData().getResourcesByTopicLink());
                });
    }

    private void addLinkControls(TopicLinkDto topicLinkObject, Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourceListByTopicLinkIndex) {
        ObservableList<ContentEntryDataItem> resourceData = FXCollections.observableArrayList();
        resourceListByTopicLinkIndex.put(topicLinkObject, resourceData);

        HBox fieldBox = addFieldBox(topicLinkObject.getGroup(), 250.0);

        addFieldLabelForLinkedTopic(fieldBox, topicLinkObject);

        String targetProfileName = topicLinkObject.getRemoteReferenceProfile();
        DbDto.Topic targetTopic = retrieveTargetTopicForLink(topicLinkObject);
        TableView<ContentEntryDataItem> tableView = addTableViewForLinkedTopic(fieldBox, topicLinkObject, resourceData, targetTopic);
        tableView.getStyleClass().addAll(CSS_CLASS_TABLEVIEW);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, topicLinkObject.isReadOnly(), targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addButtonsForLinkedTopic(fieldBox, targetProfileName, targetTopic, tableView.getSelectionModel(), topicLinkObject);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        ItemViewModel itemViewModel = controller.getViewData().getItemPropsByFieldRank();
        int linkId = topicLinkObject.getId();
        addErrorSign(fieldBox, itemViewModel.errorPropertyAtFieldRank(linkId), itemViewModel.errorMessagePropertyAtFieldRank(linkId));
    }

    private TableView<ContentEntryDataItem> addTableViewForLinkedTopic(HBox fieldBox, TopicLinkDto topicLinkObject, ObservableList<ContentEntryDataItem> resourceData, DbDto.Topic targetTopic) {
        TableView<ContentEntryDataItem> tableView = new TableView<>();
        tableView.setPrefWidth(560);

        String toolTipText = ofNullable(topicLinkObject.getToolTip()).orElse("");
        tableView.setTooltip(new Tooltip(toolTipText));

        TableColumn<ContentEntryDataItem, Number> idColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_ID);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().internalEntryIdProperty());
        idColumn.setPrefWidth(100);

        TableColumn<ContentEntryDataItem, String> refColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_REF);
        refColumn.setCellValueFactory(cellData -> cellData.getValue().referenceProperty());
        refColumn.setPrefWidth(100);

        TableColumn<ContentEntryDataItem, String> valueColumn = new TableColumn<>(DisplayConstants.COLUMN_HEADER_DATA);
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueColumn.setPrefWidth(455);

        if (DatabaseStructureQueryHelper.isUidSupportForTopic(targetTopic)) {
            tableView.getColumns().add(refColumn);
        } else {
            tableView.getColumns().add(idColumn);
        }

        tableView.getColumns().add(valueColumn);

        tableView.setItems(resourceData);

        ofNullable(topicLinkObject.getRemoteReferenceProfile())
                .ifPresent(targetProfileName -> tableView.setOnMousePressed(controller.getViewData().handleLinkTableMouseClick(targetProfileName, targetTopic)));

        fieldBox.getChildren().add(tableView);

        return tableView;
    }

    private void addButtonsForLinkedTopic(HBox fieldBox, String targetProfileName, DbDto.Topic targetTopic, TableView.TableViewSelectionModel<ContentEntryDataItem> tableSelectionModel, TopicLinkDto topicLinkObject) {
        VBox buttonsBox = new VBox();
        buttonsBox.getStyleClass().add(FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX);

        ofNullable(targetProfileName)
                .ifPresent(profileName -> buildButtonsForLinkedTopic(targetTopic, tableSelectionModel, topicLinkObject, buttonsBox, profileName));

        fieldBox.getChildren().add(buttonsBox);
    }

    private void buildButtonsForLinkedTopic(DbDto.Topic targetTopic, TableView.TableViewSelectionModel<ContentEntryDataItem> tableSelectionModel, TopicLinkDto topicLinkObject, VBox buttonsBox, String profileName) {
        addContextualButton(
                buttonsBox,
                DisplayConstants.LABEL_BUTTON_GOTO,
                DisplayConstants.TOOLTIP_BUTTON_GOTO_SELECTED_ENTRY,
                controller.getViewData().handleGotoReferenceButtonMouseClick(tableSelectionModel, targetTopic, profileName));
        if (!topicLinkObject.isReadOnly()) {
            MainStageChangeDataController changeDataController = controller.getChangeData();
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_PLUS,
                    DisplayConstants.TOOLTIP_BUTTON_ADD_LINKED_ENTRY,
                    changeDataController.handleAddLinkedEntryButtonMouseClick(tableSelectionModel, targetTopic, profileName, topicLinkObject));
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_MINUS,
                    DisplayConstants.TOOLTIP_BUTTON_DELETE_LINKED_ENTRY,
                    changeDataController.handleRemoveLinkedEntryButtonMouseClick(tableSelectionModel, topicLinkObject));
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_UP,
                    DisplayConstants.TOOLTIP_BUTTON_MOVE_LINKED_ENTRY_UP,
                    changeDataController.handleMoveLinkedEntryUpButtonMouseClick(tableSelectionModel, topicLinkObject));
            addContextualButton(
                    buttonsBox,
                    DisplayConstants.LABEL_BUTTON_DOWN,
                    DisplayConstants.TOOLTIP_BUTTON_MOVE_LINKED_ENTRY_DOWN,
                    changeDataController.handleMoveLinkedEntryDownButtonMouseClick(tableSelectionModel, topicLinkObject));
        }
    }

    private DbDto.Topic retrieveTargetTopicForLink(TopicLinkDto topicLinkObject) {
        List<DbStructureDto.Field> structureFields = getMiner().getDatabaseTopic(topicLinkObject.getTopic())
                .orElseThrow(() -> new IllegalStateException("No database object for topic: " + topicLinkObject.getTopic()))
                .getStructure()
                .getFields();
        DbDto.Topic targetTopic = topicLinkObject.getTopic();
        if (structureFields.size() == 2) {
            String targetRef = structureFields.get(1).getTargetRef();
            if (targetRef != null) {
                targetTopic = getMiner().getDatabaseTopicFromReference(targetRef).getTopic();
            }
        }
        return targetTopic;
    }

    private void addFieldLabelForLinkedTopic(HBox fieldBox, TopicLinkDto topicLinkObject) {
        String fieldName = topicLinkObject.getTopic().name();
        if (topicLinkObject.getLabel() != null) {
            fieldName = topicLinkObject.getLabel();
        }

        String toolTipText = ofNullable(topicLinkObject.getToolTip()).orElse("");

        addFieldLabel(fieldBox, topicLinkObject.isReadOnly(), fieldName, toolTipText, topicLinkObject.getId());
    }
}

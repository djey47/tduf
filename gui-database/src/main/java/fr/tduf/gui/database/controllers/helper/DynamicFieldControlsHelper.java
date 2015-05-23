package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Optional;

import static javafx.geometry.Orientation.VERTICAL;

/**
 * Helper class to be used to generate Editor controls for regular fields at runtime.
 */
public class DynamicFieldControlsHelper extends AbstractDynamicControlsHelper {

    public DynamicFieldControlsHelper(MainStageController controller) {
        super(controller);
    }

    /**
     *
     * @param currentLayoutObject
     * @param currentProfileName
     * @param currentTopic
     */
    public void addAllFieldsControls(EditorLayoutDto currentLayoutObject, String currentProfileName, DbDto.Topic currentTopic) {
        controller.getCurrentTopicObject().getStructure().getFields().stream()

                .sorted((structureField1, structureField2) -> Integer.compare(
                        EditorLayoutHelper.getFieldPrioritySettingByRank(structureField2.getRank(), currentProfileName, currentLayoutObject),
                        EditorLayoutHelper.getFieldPrioritySettingByRank(structureField1.getRank(), currentProfileName, currentLayoutObject)))

                .forEach((structureField) -> addFieldControls(
                        controller.getDefaultTab(),
                        structureField,
                        currentTopic,
                        controller.getRawValuePropertyByFieldRank(),
                        controller.getResolvedValuePropertyByFieldRank()   ) );
    }

    private void addFieldControls(VBox defaultTab, DbStructureDto.Field field, DbDto.Topic currentTopic, Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRankIndex, Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRankIndex) {
        SimpleStringProperty property = new SimpleStringProperty("");
        rawValuePropertyByFieldRankIndex.put(field.getRank(), property);

        String fieldName = field.getName();
        boolean fieldReadOnly = false;
        String groupName = null;
        Optional<String> potentialToolTip = Optional.empty();
        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(field.getRank(), controller.getProfilesChoiceBox().getValue(), controller.getLayoutObject());
        if (potentialFieldSettings.isPresent()) {
            FieldSettingsDto fieldSettings = potentialFieldSettings.get();

            if (fieldSettings.isHidden()) {
                return;
            }

            if (fieldSettings.getLabel() != null) {
                fieldName = fieldSettings.getLabel();
            }

            fieldReadOnly = fieldSettings.isReadOnly();

            groupName = fieldSettings.getGroup();

            potentialToolTip = Optional.ofNullable(fieldSettings.getToolTip());
        }

        HBox fieldBox = addFieldBox(Optional.ofNullable(groupName), 25.0, defaultTab);

        addFieldLabel(fieldBox, fieldReadOnly, fieldName);

        addTextField(fieldBox, fieldReadOnly, property, potentialToolTip);

        if (field.isAResourceField()) {
            DbDto.Topic topic = currentTopic;
            if (field.getTargetRef() != null) {
                topic = getMiner().getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            }

            addResourceValueControls(fieldBox, field.getRank(), property, topic, resolvedValuePropertyByFieldRankIndex);
        }

        // TODO handle bitfield -> requires resolver (0.7.0+)

        if (DbStructureDto.FieldType.REFERENCE == field.getFieldType()) {
            DbDto.Topic topic = getMiner().getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            addReferenceValueControls(fieldBox, field.getRank(), topic, resolvedValuePropertyByFieldRankIndex);
        }
    }

    private void addReferenceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic targetTopic, Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRankIndex) {
        SimpleStringProperty property = new SimpleStringProperty("Reference to another topic.");
        resolvedValuePropertyByFieldRankIndex.put(fieldRank, property);

        Label remoteValueLabel = addCustomLabel(fieldBox, DisplayConstants.VALUE_UNKNOWN);
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRank(fieldRank, controller.getCurrentProfileObject());
        if (potentialFieldSettings.isPresent() && potentialFieldSettings.get().getRemoteReferenceProfile() != null) {
            addGoToReferenceButton(fieldBox, fieldRank, targetTopic, potentialFieldSettings.get().getRemoteReferenceProfile());
        }
    }

    private void addResourceValueControls(HBox fieldBox, int fieldRank, SimpleStringProperty rawValueProperty, DbDto.Topic topic, Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRankIndex) {
        SimpleStringProperty property = new SimpleStringProperty("");
        resolvedValuePropertyByFieldRankIndex.put(fieldRank, property);

        addResourceValueLabel(fieldBox, property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, topic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addBrowseResourcesButton(fieldBox, topic, rawValueProperty);
    }

    private void addGoToReferenceButton(HBox fieldBox, int fieldRank, DbDto.Topic targetTopic, String targetProfileName) {
        Button gotoReferenceButton = new Button(DisplayConstants.LABEL_BUTTON_GOTO);
        gotoReferenceButton.setOnAction(
                controller.handleGotoReferenceButtonMouseClick(targetTopic, fieldRank, targetProfileName));
        fieldBox.getChildren().add(gotoReferenceButton);
    }

    private void addBrowseResourcesButton(HBox fieldBox, DbDto.Topic targetTopic, SimpleStringProperty targetReferenceProperty) {
        Button browseResourcesButton = new Button(DisplayConstants.LABEL_BUTTON_BROWSE);
        browseResourcesButton.setOnAction(
                controller.handleBrowseResourcesButtonMouseClick(targetTopic, targetReferenceProperty));
        fieldBox.getChildren().add(browseResourcesButton);
    }

    private static void addResourceValueLabel(HBox fieldBox, SimpleStringProperty property) {
        Label resourceValueLabel = new Label();
        resourceValueLabel.setPrefWidth(450);
        resourceValueLabel.getStyleClass().add(FxConstants.CSS_CLASS_FIELD_LABEL);
        resourceValueLabel.textProperty().bindBidirectional(property);
        fieldBox.getChildren().add(resourceValueLabel);
    }

    private static void addTextField(HBox fieldBox, boolean readOnly, Property<String> property, Optional<String> toolTip) {
        TextField fieldValue = new TextField();

        if (readOnly) {
            fieldValue.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        fieldValue.setPrefWidth(110.0);
        fieldValue.setEditable(!readOnly);
        if (toolTip.isPresent()) {
            fieldValue.setTooltip(new Tooltip(toolTip.get()));
        }

        fieldValue.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(fieldValue);
    }

    private BulkDatabaseMiner getMiner() {
        return controller.getDatabaseMiner();
    }
}
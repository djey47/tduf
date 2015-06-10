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
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
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
     * Generates all field controls.
     * @param layoutObject  : layout to use
     * @param profileName   : name of profile to apply
     * @param topic         : topic to be rendered.
     */
    public void addAllFieldsControls(EditorLayoutDto layoutObject, String profileName, DbDto.Topic topic) {
        controller.getCurrentTopicObject().getStructure().getFields().stream()

                .sorted((structureField1, structureField2) -> Integer.compare(
                        EditorLayoutHelper.getFieldPrioritySettingByRank(structureField2.getRank(), profileName, layoutObject),
                        EditorLayoutHelper.getFieldPrioritySettingByRank(structureField1.getRank(), profileName, layoutObject)))

                .forEach((structureField) -> addFieldControls(
                        controller.getDefaultTab(),
                        structureField,
                        topic
                ) );
    }

    private void addFieldControls(VBox defaultTab, DbStructureDto.Field field, DbDto.Topic currentTopic) {
        SimpleStringProperty property = new SimpleStringProperty("");
        controller.getRawValuePropertyByFieldRank().put(field.getRank(), property);

        int fieldRank = field.getRank();
        String fieldName = field.getName();
        boolean fieldReadOnly = false;
        String groupName = null;
        Optional<String> potentialToolTip = Optional.empty();
        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(fieldRank, controller.getCurrentProfileObject().getName(), controller.getLayoutObject());
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

        TextField valueTextField = addTextField(fieldBox, fieldReadOnly, potentialToolTip);
        valueTextField.textProperty().bindBidirectional(property);
        valueTextField.focusedProperty().addListener(controller.handleTextFieldFocusChange(fieldRank, property));

        if (field.isAResourceField()) {
            DbDto.Topic topic = currentTopic;
            if (field.getTargetRef() != null) {
                topic = getMiner().getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            }

            addResourceValueControls(fieldBox, field.getRank(), property, topic);
        }

        // TODO handle bitfield -> requires resolver (0.7.0+)

        if (DbStructureDto.FieldType.REFERENCE == field.getFieldType()) {
            DbDto.Topic topic = getMiner().getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
            addReferenceValueControls(fieldBox, field.getRank(), topic);
        }
    }

    private void addReferenceValueControls(HBox fieldBox, int fieldRank, DbDto.Topic targetTopic) {
        SimpleStringProperty property = new SimpleStringProperty("Reference to another topic.");
        controller.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        Label remoteValueLabel = addCustomLabel(fieldBox, DisplayConstants.VALUE_UNKNOWN);
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRank(fieldRank, controller.getCurrentProfileObject());
        if (potentialFieldSettings.isPresent() && potentialFieldSettings.get() != null) {
            String targetProfileName = potentialFieldSettings.get().getRemoteReferenceProfile();
            List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, controller.getLayoutObject()).getEntryLabelFieldRanks();
            addBrowseEntriesButton(fieldBox, targetTopic, labelFieldRanks, controller.getRawValuePropertyByFieldRank().get(fieldRank), fieldRank);
            addGoToReferenceButton(
                    fieldBox,
                    controller.handleGotoReferenceButtonMouseClick(targetTopic, fieldRank, targetProfileName));
        }
    }

    private void addResourceValueControls(HBox fieldBox, int fieldRank, SimpleStringProperty rawValueProperty, DbDto.Topic topic) {
        SimpleStringProperty property = new SimpleStringProperty("");
        controller.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        addResourceValueLabel(fieldBox, property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, topic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addBrowseResourcesButton(fieldBox, topic, rawValueProperty, fieldRank);
    }

    private void addBrowseEntriesButton(HBox fieldBox, DbDto.Topic targetTopic, List<Integer> labelFieldRanks, SimpleStringProperty entryReferenceProperty, int fieldRank) {
        Button browseEntriesButton = new Button(DisplayConstants.LABEL_BUTTON_BROWSE);
        browseEntriesButton.setPrefWidth(34);

        browseEntriesButton.setOnAction(
                controller.handleBrowseEntriesButtonMouseClick(targetTopic, labelFieldRanks, entryReferenceProperty, fieldRank));
        fieldBox.getChildren().add(browseEntriesButton);
    }

    private void addBrowseResourcesButton(HBox fieldBox, DbDto.Topic targetTopic, SimpleStringProperty targetReferenceProperty, int fieldRank) {
        Button browseResourcesButton = new Button(DisplayConstants.LABEL_BUTTON_BROWSE);
        browseResourcesButton.setPrefWidth(34);

        browseResourcesButton.setOnAction(
                controller.handleBrowseResourcesButtonMouseClick(targetTopic, targetReferenceProperty, fieldRank));
        fieldBox.getChildren().add(browseResourcesButton);
    }

    private static void addResourceValueLabel(HBox fieldBox, SimpleStringProperty property) {
        Label resourceValueLabel = new Label();
        resourceValueLabel.setPrefWidth(450);
        resourceValueLabel.getStyleClass().add(FxConstants.CSS_CLASS_FIELD_LABEL);
        resourceValueLabel.textProperty().bindBidirectional(property);
        fieldBox.getChildren().add(resourceValueLabel);
    }

    private static TextField addTextField(HBox fieldBox, boolean readOnly, Optional<String> toolTip) {
        TextField textField = new TextField();

        if (readOnly) {
            textField.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        textField.setPrefWidth(110.0);
        textField.setEditable(!readOnly);
        if (toolTip.isPresent()) {
            textField.setTooltip(new Tooltip(toolTip.get()));
        }
        fieldBox.getChildren().add(textField);

        return textField;
    }

    private BulkDatabaseMiner getMiner() {
        return controller.getMiner();
    }
}
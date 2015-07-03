package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.converter.PercentNumberToStringConverter;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
                ));
    }

    private void addFieldControls(VBox defaultTab, DbStructureDto.Field field, DbDto.Topic currentTopic) {

        String fieldName = field.getName();
        DbStructureDto.FieldType fieldType = field.getFieldType();
        int fieldRank = field.getRank();

        SimpleStringProperty property = new SimpleStringProperty(DisplayConstants.VALUE_FIELD_DEFAULT);
        controller.getRawValuePropertyByFieldRank().put(fieldRank, property);

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

        addFieldLabel(fieldBox, fieldReadOnly, fieldName, potentialToolTip);

        addValueTextField(fieldBox, field, fieldReadOnly, potentialToolTip, property);

        switch (fieldType) {
            case PERCENT:
                addPercentValueControls(fieldBox, fieldReadOnly, property);
                break;
            case BITFIELD:
                addBitfieldValueControls(fieldBox, fieldReadOnly);
                break;
            case REFERENCE:
                addReferenceValueControls(fieldBox, fieldReadOnly, field);
                break;
            default:
                if (field.isAResourceField()) {
                    addResourceValueControls(fieldBox, fieldReadOnly, field, property, currentTopic);
                }
                break;
        }
    }

    private void addValueTextField(HBox fieldBox, DbStructureDto.Field field, boolean fieldReadOnly, Optional<String> potentialToolTip, SimpleStringProperty property) {
        boolean valueTextFieldReadOnly = DbStructureDto.FieldType.PERCENT == field.getFieldType() || fieldReadOnly;
        TextField valueTextField = new TextField();

        if (valueTextFieldReadOnly) {
            valueTextField.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        valueTextField.setPrefWidth(110.0);
        valueTextField.setEditable(!valueTextFieldReadOnly);
        potentialToolTip.ifPresent((text) -> valueTextField.setTooltip(new Tooltip(text)));
        fieldBox.getChildren().add(valueTextField);

        valueTextField.textProperty().bindBidirectional(property);
        valueTextField.focusedProperty().addListener(controller.handleTextFieldFocusChange(field.getRank(), property));
    }

    private void addPercentValueControls(HBox fieldBox, boolean fieldReadOnly, SimpleStringProperty rawValueProperty) {
        Slider slider = new Slider(0.0, 100.0, 0.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(3);
        slider.setBlockIncrement(1);
        slider.setPrefWidth(450);
        slider.setDisable(fieldReadOnly);

        Bindings.bindBidirectional(rawValueProperty, slider.valueProperty(), new PercentNumberToStringConverter());

        fieldBox.getChildren().add(slider);
        fieldBox.getChildren().add(new Separator(VERTICAL));
    }

    private void addReferenceValueControls(HBox fieldBox, boolean fieldReadOnly, DbStructureDto.Field field) {
        int fieldRank = field.getRank();
        SimpleStringProperty property = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_REFERENCE);
        controller.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        Label remoteValueLabel = addCustomLabel(fieldBox, fieldReadOnly, DisplayConstants.VALUE_UNKNOWN);
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        DbDto.Topic targetTopic = getMiner().getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
        addCustomLabel(fieldBox, fieldReadOnly, targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRank(fieldRank, controller.getCurrentProfileObject());
        if (potentialFieldSettings.isPresent() && potentialFieldSettings.get() != null) {
            String targetProfileName = potentialFieldSettings.get().getRemoteReferenceProfile();
            List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, controller.getLayoutObject()).getEntryLabelFieldRanks();
            SimpleStringProperty entryReferenceProperty = controller.getRawValuePropertyByFieldRank().get(fieldRank);

            if (!fieldReadOnly) {
                addContextualButton(
                        fieldBox,
                        DisplayConstants.LABEL_BUTTON_BROWSE,
                        DisplayConstants.TOOLTIP_BUTTON_BROWSE_ENTRIES,
                        controller.handleBrowseEntriesButtonMouseClick(targetTopic, labelFieldRanks, entryReferenceProperty, fieldRank)
                );
            }
            addContextualButton(
                    fieldBox,
                    DisplayConstants.LABEL_BUTTON_GOTO,
                    DisplayConstants.TOOLTIP_BUTTON_GOTO_LINKED_ENTRY,
                    controller.handleGotoReferenceButtonMouseClick(targetTopic, fieldRank, targetProfileName));
        }
    }

    private void addResourceValueControls(HBox fieldBox, boolean fieldReadOnly, DbStructureDto.Field field, SimpleStringProperty rawValueProperty, DbDto.Topic topic) {
        String fieldTargetRef = field.getTargetRef();
        if (fieldTargetRef != null) {
            topic = getMiner().getDatabaseTopicFromReference(fieldTargetRef).getTopic();
        }

        int fieldRank = field.getRank();
        SimpleStringProperty property = new SimpleStringProperty(DisplayConstants.VALUE_RESOURCE_DEFAULT);
        controller.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        addResourceValueLabel(fieldBox, fieldReadOnly, property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, fieldReadOnly, topic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        if (!fieldReadOnly) {
            addContextualButton(
                    fieldBox,
                    DisplayConstants.LABEL_BUTTON_BROWSE,
                    DisplayConstants.TOOLTIP_BUTTON_BROWSE_RESOURCES,
                    controller.handleBrowseResourcesButtonMouseClick(topic, rawValueProperty, fieldRank)
            );
        }
    }

    private void addBitfieldValueControls(HBox fieldBox, boolean fieldReadOnly) {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setPrefWidth(225);

        gridPane.add(new CheckBox("1"), 0, 0);
        gridPane.add(new CheckBox("2"), 1, 0);
        gridPane.add(new CheckBox("3"), 2, 0);
        gridPane.add(new CheckBox("4"), 0, 1);
        gridPane.add(new CheckBox("5"), 1, 1);
        gridPane.add(new CheckBox("6"), 2, 1);
        gridPane.add(new CheckBox("7"), 0, 2);
        gridPane.add(new CheckBox("8"), 1, 2);
        gridPane.add(new CheckBox("9"), 2, 2);
        gridPane.add(new CheckBox("10"), 0, 3);
        gridPane.add(new CheckBox("11"), 1, 3);
        gridPane.add(new CheckBox("12"), 2, 3);
        gridPane.add(new CheckBox("13"), 0, 4);
        gridPane.add(new CheckBox("14"), 1, 4);
        gridPane.add(new CheckBox("15"), 2, 4);
        gridPane.add(new CheckBox("16"), 0, 5);
        gridPane.add(new CheckBox("17"), 1, 5);
        gridPane.add(new CheckBox("18"), 2, 5);


        fieldBox.getChildren().add(gridPane);

    }

    private static void addResourceValueLabel(HBox fieldBox, boolean fieldReadOnly, SimpleStringProperty property) {
        Label resourceValueLabel = addLabel(fieldBox, fieldReadOnly, 450, null);

        resourceValueLabel.textProperty().bindBidirectional(property);
    }

    private BulkDatabaseMiner getMiner() {
        return controller.getMiner();
    }
}
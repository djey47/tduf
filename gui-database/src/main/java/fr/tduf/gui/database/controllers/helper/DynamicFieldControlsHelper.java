package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.converter.BitfieldToStringConverter;
import fr.tduf.gui.database.converter.PercentNumberToStringConverter;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.libunlimited.framework.base.Strings;
import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static javafx.geometry.Orientation.VERTICAL;

/**
 * Helper class to be used to generate Editor controls for regular fields at runtime.
 */
public class DynamicFieldControlsHelper extends AbstractDynamicControlsHelper {

    /**
     * @param controller    : main controller instance
     */
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

                .forEach(structureField -> addFieldControls(
                        structureField,
                        topic
                ));
    }

    private void addFieldControls(DbStructureDto.Field field, DbDto.Topic currentTopic) {
        int fieldRank = field.getRank();
        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(fieldRank, controller.getCurrentProfileObject().getName(), controller.getLayoutObject());
        if (!potentialFieldSettings.isPresent()) {
            return;
        }

        FieldSettingsDto fieldSettings = potentialFieldSettings.get();
        if (fieldSettings.isHidden()) {
            return;
        }

        SimpleStringProperty property = new SimpleStringProperty(DisplayConstants.VALUE_FIELD_DEFAULT);
        controller.getViewData().getRawValuesByFieldRank().put(fieldRank, property);

        String fieldName = field.getName();
        if (fieldSettings.getLabel() != null) {
            fieldName = fieldSettings.getLabel();
        }

        String toolTipText = String.format(DisplayConstants.TOOLTIP_FIELD_TEMPLATE, fieldRank, fieldName);
        if (fieldSettings.getToolTip() != null) {
            toolTipText += (":" + fieldSettings.getToolTip());
        }

        boolean fieldReadOnly = fieldSettings.isReadOnly();

        String groupName = fieldSettings.getGroup();

        HBox fieldBox = addFieldBox(Optional.ofNullable(groupName), 25.0);

        addFieldLabel(fieldBox, fieldReadOnly, fieldName, toolTipText);

        addValueTextField(fieldBox, field, fieldReadOnly, toolTipText, property);

        addCustomControls(fieldBox, field, fieldReadOnly, currentTopic, property);
    }

    private void addValueTextField(HBox fieldBox, DbStructureDto.Field field, boolean fieldReadOnly, String toolTip, SimpleStringProperty property) {
        boolean valueTextFieldReadOnly = DbStructureDto.FieldType.PERCENT == field.getFieldType() || fieldReadOnly;
        TextField valueTextField = new TextField();

        if (valueTextFieldReadOnly) {
            valueTextField.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        valueTextField.setPrefWidth(110.0);
        valueTextField.setEditable(!valueTextFieldReadOnly);
        valueTextField.setTooltip(new Tooltip(toolTip));
        fieldBox.getChildren().add(valueTextField);

        valueTextField.textProperty().bindBidirectional(property);
        if (!valueTextFieldReadOnly) {
            valueTextField.focusedProperty().addListener(controller.handleTextFieldFocusChange(field.getRank(), property));
        }
    }

    private void addCustomControls(HBox fieldBox, DbStructureDto.Field field, boolean fieldReadOnly, DbDto.Topic currentTopic, SimpleStringProperty property) {
        switch (field.getFieldType()) {
            case PERCENT:
                addPercentValueControls(fieldBox, field.getRank(), fieldReadOnly, property);
                break;
            case BITFIELD:
                addBitfieldValueControls(fieldBox, field.getRank(), fieldReadOnly, property, currentTopic);
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

    // Ignore warning (method ref)
    private void addPercentValueControls(HBox fieldBox, int fieldRank, boolean fieldReadOnly, SimpleStringProperty rawValueProperty) {
        Slider slider = new Slider(0.0, 100.0, 0.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(3);
        slider.setBlockIncrement(1);
        slider.setPrefWidth(450);
        slider.setDisable(fieldReadOnly);

        Bindings.bindBidirectional(rawValueProperty, slider.valueProperty(), new PercentNumberToStringConverter());
        if (!fieldReadOnly) {
            slider.valueChangingProperty().addListener(controller.handleSliderValueChange(fieldRank, rawValueProperty));
        }

        fieldBox.getChildren().add(slider);
        fieldBox.getChildren().add(new Separator(VERTICAL));
    }

    private void addReferenceValueControls(HBox fieldBox, boolean fieldReadOnly, DbStructureDto.Field field) {
        int fieldRank = field.getRank();
        SimpleStringProperty property = new SimpleStringProperty(DisplayConstants.LABEL_ITEM_REFERENCE);
        controller.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        final String valueUnknown = String.format(DisplayConstants.VALUE_UNKNOWN, "?");
        Label remoteValueLabel = addCustomLabel(fieldBox, fieldReadOnly, valueUnknown);
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
            SimpleStringProperty entryReferenceProperty = controller.getViewData().getRawValuesByFieldRank().get(fieldRank);

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
        DbDto.Topic effectiveTopic = fieldTargetRef == null ?
                topic : getMiner().getDatabaseTopicFromReference(fieldTargetRef).getTopic();

        int fieldRank = field.getRank();
        SimpleStringProperty property = new SimpleStringProperty(DisplayConstants.VALUE_RESOURCE_DEFAULT);
        controller.getResolvedValuePropertyByFieldRank().put(fieldRank, property);

        addResourceValueLabel(fieldBox, fieldReadOnly, property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, fieldReadOnly, effectiveTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        if (!fieldReadOnly) {
            addContextualButton(
                    fieldBox,
                    DisplayConstants.LABEL_BUTTON_BROWSE,
                    DisplayConstants.TOOLTIP_BUTTON_BROWSE_RESOURCES,
                    controller.handleBrowseResourcesButtonMouseClick(effectiveTopic, rawValueProperty, fieldRank)
            );
        }
    }

    // Ignore warning (method ref)
    private void addBitfieldValueControls(HBox fieldBox, int fieldRank, boolean fieldReadOnly, SimpleStringProperty rawValueProperty, DbDto.Topic currentTopic) {
        VBox vbox = new VBox();

        BitfieldHelper bitfieldHelper = new BitfieldHelper();
        bitfieldHelper.getBitfieldReferenceForTopic(currentTopic)
                .ifPresent(refs -> refs
                        .forEach(ref -> addBitValueCheckbox(vbox, fieldRank, ref, fieldReadOnly, rawValueProperty, currentTopic, bitfieldHelper)));

        fieldBox.getChildren().add(vbox);
    }

    // Ignore warning (method ref)
    private void addBitValueCheckbox(VBox vbox, int fieldRank, DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto ref, boolean fieldReadOnly, SimpleStringProperty rawValueProperty, DbDto.Topic currentTopic, BitfieldHelper bitfieldHelper) {
        int bitIndex = ref.getIndex();
        String displayedIndex = Strings.padStart(Integer.toString(bitIndex), 2, '0');
        String label = String.format("%s: %s", displayedIndex, ref.getLabel());
        CheckBox checkBox = new CheckBox(label);

        checkBox.setPadding(new Insets(0, 5, 0, 5));
        checkBox.setDisable(fieldReadOnly);

        if (StringUtils.isNotEmpty(ref.getComment())) {
            checkBox.setTooltip(new Tooltip(ref.getComment()));
        }

        Bindings.bindBidirectional(rawValueProperty, checkBox.selectedProperty(), new BitfieldToStringConverter(currentTopic, bitIndex, rawValueProperty, bitfieldHelper));
        if (!fieldReadOnly) {
            checkBox.selectedProperty().addListener(controller.handleBitfieldCheckboxSelectionChange(fieldRank, rawValueProperty));
        }

        vbox.getChildren().add(checkBox);
    }

    private static void addResourceValueLabel(HBox fieldBox, boolean fieldReadOnly, SimpleStringProperty property) {
        Label resourceValueLabel = addLabel(fieldBox, fieldReadOnly, 450, null);

        resourceValueLabel.textProperty().bindBidirectional(property);
    }
}

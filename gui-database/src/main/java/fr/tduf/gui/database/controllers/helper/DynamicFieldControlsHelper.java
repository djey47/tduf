package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.controllers.MainStageViewDataController;
import fr.tduf.gui.database.domain.ItemViewModel;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.listener.ErrorChangeListener;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.REFERENCE;
import static javafx.beans.binding.Bindings.not;
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

    void addCustomControls(HBox fieldBox, DbStructureDto.Field field, FieldSettingsDto fieldSettings, DbDto.Topic currentTopic, StringProperty rawValueProperty) {
        ItemViewModel itemProps = controller.getViewData().getItemPropsByFieldRank();
        BooleanProperty errorProperty = itemProps.errorPropertyAtFieldRank(field.getRank());
        StringProperty errorMessageProperty = itemProps.errorMessagePropertyAtFieldRank(field.getRank());

        String pluginName = fieldSettings.getPluginName();
        if (pluginName == null) {
            addSpecialControls(fieldBox, field, fieldSettings, currentTopic);
        } else {
            addPluginControls(pluginName, currentTopic, fieldBox, fieldSettings, rawValueProperty, errorMessageProperty, errorProperty, field.getTargetRef());
        }

        addErrorSign(fieldBox, errorProperty, errorMessageProperty);
    }

    private void addFieldControls(DbStructureDto.Field field, DbDto.Topic currentTopic) {
        int fieldRank = field.getRank();
        final MainStageViewDataController viewData = controller.getViewData();
        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(fieldRank, viewData.currentProfile().getValue().getName(), controller.getLayoutObject());
        if (!potentialFieldSettings.isPresent()) {
            return;
        }

        FieldSettingsDto fieldSettings = potentialFieldSettings.get();
        if (fieldSettings.isHidden()) {
            return;
        }

        final StringProperty property = viewData.getItemPropsByFieldRank().rawValuePropertyAtFieldRank(fieldRank);
        property.set(DisplayConstants.VALUE_FIELD_DEFAULT);

        String toolTipText = String.format(DisplayConstants.TOOLTIP_FIELD_TEMPLATE, fieldRank, field.getName());
        if (fieldSettings.getToolTip() != null) {
            toolTipText += (":" + fieldSettings.getToolTip());
        }

        boolean fieldReadOnly = fieldSettings.isReadOnly();

        String groupName = fieldSettings.getGroup();

        HBox fieldBox = addFieldBox(Optional.ofNullable(groupName), 25.0);

        String effectiveFieldName = fieldSettings.getLabel() == null ?
                field.getName() : fieldSettings.getLabel();
        addFieldLabel(fieldBox, fieldReadOnly, effectiveFieldName, toolTipText, fieldRank);

        addValueTextField(fieldBox, field, fieldReadOnly, toolTipText, property);

        addCustomControls(fieldBox, field, fieldSettings, currentTopic, property);
    }

    private void addValueTextField(HBox fieldBox, DbStructureDto.Field field, boolean fieldReadOnly, String toolTip, StringProperty property) {
        boolean valueTextFieldReadOnly = DbStructureDto.FieldType.PERCENT == field.getFieldType() || fieldReadOnly;
        TextField valueTextField = new TextField();

        int fieldRank = field.getRank();
        controller.getViewData()
                .getItemPropsByFieldRank()
                .errorPropertyAtFieldRank(fieldRank)
                .addListener(new ErrorChangeListener(valueTextField));

        if (valueTextFieldReadOnly) {
            valueTextField.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        valueTextField.setPrefWidth(110.0);
        valueTextField.setEditable(!valueTextFieldReadOnly);
        valueTextField.setTooltip(new Tooltip(toolTip));
        fieldBox.getChildren().add(valueTextField);

        valueTextField.textProperty().bindBidirectional(property);
        if (!valueTextFieldReadOnly) {
            valueTextField.focusedProperty().addListener(controller.handleTextFieldFocusChange(fieldRank, property));
        }
    }

    private void addSpecialControls(HBox fieldBox, DbStructureDto.Field field, FieldSettingsDto fieldSettings, DbDto.Topic currentTopic) {
        boolean fieldReadOnly = fieldSettings.isReadOnly();
        if (REFERENCE == field.getFieldType()) {
            addReferenceValueControls(fieldBox, fieldReadOnly, field);
        } else if (field.isAResourceField()) {
            addResourceValueControls(fieldBox, fieldReadOnly, field, currentTopic);
        }
    }

    private void addPluginControls(String pluginName, DbDto.Topic currentTopic, HBox fieldBox, FieldSettingsDto fieldSettings, StringProperty rawValueProperty, StringProperty errorMessageProperty, BooleanProperty errorProperty, String fieldTargetRef) {
        EditorContext editorContext = controller.getPluginHandler().getContext();
        editorContext.setCurrentTopic(currentTopic);
        editorContext.setRemoteTopic(getEffectiveTopic(currentTopic, fieldTargetRef));
        editorContext.setFieldRank(fieldSettings.getRank());
        editorContext.setFieldReadOnly(fieldSettings.isReadOnly());
        editorContext.setRawValueProperty(rawValueProperty);
        editorContext.setErrorMessageProperty(errorMessageProperty);
        editorContext.setErrorProperty(errorProperty);

        controller.getPluginHandler().renderPluginByName(pluginName, fieldBox);
    }

    private void addReferenceValueControls(HBox fieldBox, boolean fieldReadOnly, DbStructureDto.Field field) {
        int fieldRank = field.getRank();
        final MainStageViewDataController viewData = controller.getViewData();
        StringProperty property = viewData.getItemPropsByFieldRank().resolvedValuePropertyAtFieldRank(fieldRank);
        property.set(DisplayConstants.LABEL_ITEM_REFERENCE);

        final String valueUnknown = String.format(DisplayConstants.VALUE_UNKNOWN, "?");
        Label remoteValueLabel = addCustomLabel(fieldBox, fieldReadOnly, valueUnknown);
        remoteValueLabel.setPrefWidth(450);
        remoteValueLabel.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        DbDto.Topic targetTopic = getMiner().getDatabaseTopicFromReference(field.getTargetRef()).getTopic();
        addCustomLabel(fieldBox, fieldReadOnly, targetTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        ItemViewModel itemViewModel = viewData.getItemPropsByFieldRank();
        BooleanProperty errorProperty = itemViewModel.errorPropertyAtFieldRank(fieldRank);

        Optional<FieldSettingsDto> potentialFieldSettings = EditorLayoutHelper.getFieldSettingsByRank(fieldRank, viewData.currentProfile().getValue());
        if (potentialFieldSettings.isPresent()) {
            String targetProfileName = potentialFieldSettings.get().getRemoteReferenceProfile();
            List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByName(targetProfileName, controller.getLayoutObject()).getEntryLabelFieldRanks();
            StringProperty entryReferenceProperty = viewData.getItemPropsByFieldRank().rawValuePropertyAtFieldRank(fieldRank);

            if (!fieldReadOnly) {
                addContextualButton(
                        fieldBox,
                        DisplayConstants.LABEL_BUTTON_BROWSE,
                        DisplayConstants.TOOLTIP_BUTTON_BROWSE_ENTRIES,
                        controller.handleBrowseEntriesButtonMouseClick(targetTopic, labelFieldRanks, entryReferenceProperty, fieldRank));
            }
            addContextualButtonWithActivationCondition(
                    fieldBox,
                    DisplayConstants.LABEL_BUTTON_GOTO,
                    DisplayConstants.TOOLTIP_BUTTON_GOTO_LINKED_ENTRY,
                    controller.handleGotoReferenceButtonMouseClick(targetTopic, fieldRank, targetProfileName),
                    not(errorProperty));
        }

        fieldBox.getChildren().add(new Separator(VERTICAL));
    }

    private void addResourceValueControls(HBox fieldBox, boolean fieldReadOnly, DbStructureDto.Field field, DbDto.Topic topic) {
        String fieldTargetRef = field.getTargetRef();
        DbDto.Topic effectiveTopic = getEffectiveTopic(topic, fieldTargetRef);

        int fieldRank = field.getRank();

        ItemViewModel itemViewModel = controller.getViewData().getItemPropsByFieldRank();
        StringProperty property = itemViewModel
                .resolvedValuePropertyAtFieldRank(fieldRank);
        property.set(DisplayConstants.VALUE_RESOURCE_DEFAULT);

        addResourceValueLabel(fieldBox, fieldReadOnly, property);

        fieldBox.getChildren().add(new Separator(VERTICAL));

        addCustomLabel(fieldBox, fieldReadOnly, effectiveTopic.name());

        fieldBox.getChildren().add(new Separator(VERTICAL));

        if (!fieldReadOnly) {
            StringProperty rawValueProperty = itemViewModel.rawValuePropertyAtFieldRank(fieldRank);
            addContextualButton(
                    fieldBox,
                    DisplayConstants.LABEL_BUTTON_BROWSE,
                    DisplayConstants.TOOLTIP_BUTTON_BROWSE_RESOURCES,
                    controller.handleBrowseResourcesButtonMouseClick(effectiveTopic, rawValueProperty, fieldRank)
            );
        }

        fieldBox.getChildren().add(new Separator(VERTICAL));
    }

    private DbDto.Topic getEffectiveTopic(DbDto.Topic sourceTopic, String fieldTargetRef) {
        return fieldTargetRef == null ?
                sourceTopic : getMiner().getDatabaseTopicFromReference(fieldTargetRef).getTopic();
    }

    private static void addResourceValueLabel(HBox fieldBox, boolean fieldReadOnly, StringProperty property) {
        Label resourceValueLabel = addLabel(fieldBox, fieldReadOnly, 450, null);

        resourceValueLabel.textProperty().bindBidirectional(property);
    }
}

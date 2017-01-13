package fr.tduf.gui.database.plugins.bitfield;

import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.bitfield.converter.BitfieldToStringConverter;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.libunlimited.framework.base.Strings;
import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import static fr.tduf.gui.database.plugins.bitfield.common.DisplayConstants.LABEL_FORMAT_BITFIELD_CHECKBOX;

/**
 * Pretty prints bitfield values with easy changes.
 * Required items in context:
 * - currentTopic
 * - rawValueProperty
 * - fieldReadOnly
 * - fieldRank
 * - mainStageController
 */
public class BitfieldPlugin implements DatabasePlugin {
    private BitfieldHelper bitfieldHelper;

    @Override
    public void onInit(EditorContext context) {
        bitfieldHelper = new BitfieldHelper();
    }

    @Override
    public void onSave(EditorContext context) {
        // Nothing to do for this plugin
    }

    @Override
    public Node renderControls(EditorContext context) {
        VBox vbox = new VBox();

        bitfieldHelper.getBitfieldReferenceForTopic(context.getCurrentTopic())
                .ifPresent(refs -> refs
                        .forEach(ref -> addBitValueCheckbox(context, vbox, ref)));

        return vbox;
    }

    private void addBitValueCheckbox(EditorContext context, VBox vbox, DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto ref) {
        int bitIndex = ref.getIndex();
        String displayedIndex = Strings.padStart(Integer.toString(bitIndex), 2, '0');
        String label = String.format(LABEL_FORMAT_BITFIELD_CHECKBOX, displayedIndex, ref.getLabel());
        CheckBox checkBox = new CheckBox(label);

        checkBox.setPadding(new Insets(0, 5, 0, 5));
        boolean fieldReadOnly = context.isFieldReadOnly();
        checkBox.setDisable(fieldReadOnly);

        if (StringUtils.isNotEmpty(ref.getComment())) {
            checkBox.setTooltip(new Tooltip(ref.getComment()));
        }

        StringProperty rawValueProperty = context.getRawValueProperty();
        Bindings.bindBidirectional(rawValueProperty, checkBox.selectedProperty(), new BitfieldToStringConverter(context.getCurrentTopic(), bitIndex, rawValueProperty, bitfieldHelper));
        if (!fieldReadOnly) {
            checkBox.selectedProperty().addListener(handleBitfieldCheckboxSelectionChange(context.getFieldRank(), rawValueProperty, context.getCurrentTopic(), context.getChangeDataController()));
        }

        vbox.getChildren().add(checkBox);
    }

    private ChangeListener<Boolean> handleBitfieldCheckboxSelectionChange(int fieldRank, StringProperty rawValueProperty, DbDto.Topic topic, MainStageChangeDataController changeDataController) {
        return (observable, oldCheckedState, newCheckedState) -> {
            if (newCheckedState == oldCheckedState) {
                return;
            }
            changeDataController.updateContentItem(topic, fieldRank, rawValueProperty.get());
        };
    }
}

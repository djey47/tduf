package fr.tduf.gui.database.plugins.bitfield;

import fr.tduf.gui.database.converter.BitfieldToStringConverter;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import fr.tduf.libunlimited.framework.base.Strings;
import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

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
    public void onInit() {
        bitfieldHelper = new BitfieldHelper();
    }

    @Override
    public Node renderControls(PluginContext context) {
        VBox vbox = new VBox();

        bitfieldHelper.getBitfieldReferenceForTopic(context.getCurrentTopic())
                .ifPresent(refs -> refs
                        .forEach(ref -> addBitValueCheckbox(context, vbox, ref)));

        return vbox;
    }

    private void addBitValueCheckbox(PluginContext context, VBox vbox, DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto ref) {
        int bitIndex = ref.getIndex();
        String displayedIndex = Strings.padStart(Integer.toString(bitIndex), 2, '0');
        // TODO extract to constant
        String label = String.format("%s: %s", displayedIndex, ref.getLabel());
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
            checkBox.selectedProperty().addListener(context.getMainStageController().handleBitfieldCheckboxSelectionChange(context.getFieldRank(), rawValueProperty));
        }

        vbox.getChildren().add(checkBox);
    }
}

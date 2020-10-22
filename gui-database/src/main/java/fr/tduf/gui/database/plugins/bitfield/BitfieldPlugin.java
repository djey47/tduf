package fr.tduf.gui.database.plugins.bitfield;

import fr.tduf.gui.database.plugins.bitfield.converter.BitfieldToStringConverter;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libunlimited.framework.base.Strings;
import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fr.tduf.gui.database.common.FxConstants.CSS_CLASS_CHECKBOX;
import static fr.tduf.gui.database.plugins.bitfield.common.DisplayConstants.LABEL_FORMAT_BITFIELD_CHECKBOX;
import static fr.tduf.gui.database.plugins.bitfield.common.FxConstants.CSS_CLASS_BITFIELD_CHECKBOX;
import static fr.tduf.gui.database.plugins.bitfield.common.FxConstants.PATH_RESOURCE_CSS_BITFIELD;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_PLUGIN_BOX;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_PLUGIN_CHECKBOX;
import static java.util.Collections.singletonList;

/**
 * Pretty prints bitfield values with easy changes.
 * Required items in context:
 * - currentTopic
 * - rawValueProperty
 * - fieldReadOnly
 * - fieldRank
 * - changeDataController
 */
public class BitfieldPlugin extends AbstractDatabasePlugin {
    private BitfieldHelper bitfieldHelper;

    @Override
    public void onInit(String pluginName, EditorContext editorContext) throws IOException {
        super.onInit(pluginName, editorContext);

        bitfieldHelper = new BitfieldHelper();
    }

    @Override
    public void onSave() {}

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        VBox vbox = new VBox();
        vbox.getStyleClass().addAll(CSS_CLASS_PLUGIN_BOX);

        bitfieldHelper.getBitfieldReferenceForTopic(onTheFlyContext.getCurrentTopic())
                .ifPresent(refs -> refs
                        .forEach(ref -> addBitValueCheckbox(onTheFlyContext, vbox, ref)));

        return vbox;
    }

    @Override
    public Set<String> getCss() {
        String css = PluginHandler.fetchCss(PATH_RESOURCE_CSS_BITFIELD);
        return new HashSet<>(singletonList(css));
    }

    private void addBitValueCheckbox(OnTheFlyContext onTheFlyContext, VBox vbox, DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto ref) {
        int bitIndex = ref.getIndex();
        String displayedIndex = Strings.padStart(Integer.toString(bitIndex), 2, '0');
        String label = String.format(LABEL_FORMAT_BITFIELD_CHECKBOX, displayedIndex, ref.getLabel());

        CheckBox checkBox = new CheckBox(label);
        checkBox.getStyleClass().addAll(CSS_CLASS_CHECKBOX, CSS_CLASS_PLUGIN_CHECKBOX, CSS_CLASS_BITFIELD_CHECKBOX);
        boolean fieldReadOnly = onTheFlyContext.isFieldReadOnly();
        checkBox.setDisable(fieldReadOnly);

        if (StringUtils.isNotEmpty(ref.getComment())) {
            checkBox.setTooltip(new Tooltip(ref.getComment()));
        }

        StringProperty rawValueProperty = onTheFlyContext.getRawValueProperty();
        Bindings.bindBidirectional(rawValueProperty, checkBox.selectedProperty(), new BitfieldToStringConverter(onTheFlyContext.getCurrentTopic(), bitIndex, rawValueProperty, bitfieldHelper));
        if (!fieldReadOnly) {
            checkBox.selectedProperty().addListener(handleBitfieldCheckboxSelectionChange(onTheFlyContext));
        }

        vbox.getChildren().add(checkBox);
    }

    private ChangeListener<Boolean> handleBitfieldCheckboxSelectionChange(OnTheFlyContext onTheFlyContext) {
        return (observable, oldCheckedState, newCheckedState) -> {
            if (newCheckedState == oldCheckedState) {
                return;
            }
            getEditorContext().getChangeDataController().updateContentItem(onTheFlyContext.getCurrentTopic(), onTheFlyContext.getFieldRank(), onTheFlyContext.getRawValueProperty().get());
        };
    }
}

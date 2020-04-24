package fr.tduf.gui.database.plugins.percent;

import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.percent.converter.PercentNumberToStringConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fr.tduf.gui.database.plugins.percent.common.FxConstants.CSS_CLASS_PERCENT_SLIDER;
import static fr.tduf.gui.database.plugins.percent.common.FxConstants.PATH_RESOURCE_CSS_PERCENT;
import static java.util.Collections.singletonList;
import static javafx.geometry.Orientation.VERTICAL;

/**
 * Pretty prints percent values with easy changes (slider).
 * Required items in context:
 * - rawValueProperty
 * - fieldReadOnly
 * - fieldRank
 * - mainStageController
 */
public class PercentPlugin extends AbstractDatabasePlugin {
    @Override
    public void onInit(EditorContext editorContext) throws IOException {
        super.onInit(editorContext);
    }

    @Override
    public void onSave() {}

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        boolean fieldReadOnly = onTheFlyContext.isFieldReadOnly();

        HBox hBox = new HBox();
        Slider slider = new Slider(0.0, 100.0, 0.0);
        slider.getStyleClass().add(CSS_CLASS_PERCENT_SLIDER);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(3);
        slider.setBlockIncrement(1);
        slider.setDisable(fieldReadOnly);

        Bindings.bindBidirectional(onTheFlyContext.getRawValueProperty(), slider.valueProperty(), new PercentNumberToStringConverter());
        if (!fieldReadOnly) {
            slider.valueChangingProperty().addListener(
                    handleSliderValueChange(onTheFlyContext));
        }

        hBox.getChildren().add(slider);
        hBox.getChildren().add(new Separator(VERTICAL));

        return hBox;
    }

    @Override
    public Set<String> getCss() {
        String css = PluginHandler.fetchCss(PATH_RESOURCE_CSS_PERCENT);
        return new HashSet<>(singletonList(css));
    }

    private ChangeListener<Boolean> handleSliderValueChange(OnTheFlyContext onTheFlyContext) {
        return (observable, oldState, newState) -> {
            if (oldState == newState) {
                return;
            }
            getEditorContext().getChangeDataController().updateContentItem(onTheFlyContext.getCurrentTopic(), onTheFlyContext.getFieldRank(), onTheFlyContext.getRawValueProperty().get());
        };
    }
}

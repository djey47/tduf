package fr.tduf.gui.database.plugins.percent;

import fr.tduf.gui.database.plugins.percent.converter.PercentNumberToStringConverter;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

import static javafx.geometry.Orientation.VERTICAL;

/**
 * Pretty prints percent values with easy changes (slider).
 * Required items in context:
 * - rawValueProperty
 * - fieldReadOnly
 * - fieldRank
 * - mainStageController
 */
public class PercentPlugin implements DatabasePlugin {
    @Override
    public void onInit() { }

    @Override
    public Node renderControls(PluginContext context) {
        boolean fieldReadOnly = context.isFieldReadOnly();
        StringProperty rawValueProperty = context.getRawValueProperty();

        HBox hBox = new HBox();
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
            // TODO create an handler field in context and use it instead
            slider.valueChangingProperty().addListener(context.getMainStageController().handleSliderValueChange(context.getFieldRank(), rawValueProperty));
        }

        hBox.getChildren().add(slider);
        hBox.getChildren().add(new Separator(VERTICAL));

        return hBox;
    }
}

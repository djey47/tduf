package fr.tduf.gui.common.helper.javafx;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

import static java.util.Objects.requireNonNull;

/**
 * Set of helper methods to handle JavaFX controls.
 */
public class ControlHelper {
    /**
     * Defines a default tooltip with specified text contents.
     * @param control       : control to add tooltip to
     * @param tooltipText   : text to be displayed in tooltip.
     */
    public static void setTooltipText(Control control, String tooltipText) {
        requireNonNull(control, "A JavaFX control is required.");

        Tooltip tooltip = new Tooltip();
        tooltip.setText(tooltipText);
        control.setTooltip(tooltip);
    }
}
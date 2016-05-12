package fr.tduf.gui.common.javafx.scene.control;

import javafx.scene.control.CheckBox;

/**
 * CheckBox whose value can be changed only programmatically
 */
public class ReadOnlyCheckBox extends CheckBox {
    /**
     * Creates a read-only check box with an empty string for its label.
     */
    public ReadOnlyCheckBox() {
        super();
        setReadOnly();
    }

    /**
     * Creates a read-only check box with the specified text as its label.
     *
     * @param text A text string for its label.
     */
    public ReadOnlyCheckBox(String text) {
        super(text);
        setReadOnly();
    }

    private void setReadOnly() {
        setDisable(true);
        setStyle("-fx-opacity: 1");
    }
}

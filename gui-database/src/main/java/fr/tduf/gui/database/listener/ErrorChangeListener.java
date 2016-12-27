package fr.tduf.gui.database.listener;

import fr.tduf.gui.database.common.FxConstants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

/**
 * Listens changes on error boolean property for scene node.
 */
public class ErrorChangeListener implements ChangeListener<Boolean> {

    private final Node node;

    public ErrorChangeListener(Node node) {
        this.node = node;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (oldValue == newValue) {
            return;
        }

        if (newValue) {
            node.getStyleClass().add(FxConstants.CSS_CLASS_ERROR);
        } else {
            node.getStyleClass().remove(FxConstants.CSS_CLASS_ERROR);
        }
    }
}

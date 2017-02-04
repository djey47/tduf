package fr.tduf.gui.database.plugins.iks;

import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * Piece of information required by IKs plugin.
 */
public class IKsContext implements PluginContext {
    private StringProperty errorMessageProperty = null;
    private BooleanProperty errorProperty = null;

    @Override
    public void reset() {
        errorMessageProperty = null;
        errorProperty = null;
    }

    StringProperty getErrorMessageProperty() {
        return errorMessageProperty;
    }

    void setErrorMessageProperty(StringProperty errorMessageProperty) {
        this.errorMessageProperty = errorMessageProperty;
    }

    BooleanProperty getErrorProperty() {
        return errorProperty;
    }

    void setErrorProperty(BooleanProperty errorProperty) {
        this.errorProperty = errorProperty;
    }
}

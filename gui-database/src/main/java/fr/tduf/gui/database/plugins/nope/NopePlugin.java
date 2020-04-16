package fr.tduf.gui.database.plugins.nope;

import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.Set;

/**
 * Test plugin, doing nothing.
 */
public class NopePlugin extends AbstractDatabasePlugin {
    @Override
    public void onInit(EditorContext context) {}

    @Override
    public void onSave(EditorContext context) {}

    @Override
    public Node renderControls(EditorContext context) {
        return new VBox();
    }

    @Override
    public Set<String> getCss() {
        return null;
    }
}

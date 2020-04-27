package fr.tduf.gui.database.plugins.nope;

import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.Set;

/**
 * Test plugin, doing nothing.
 */
public class NopePlugin extends AbstractDatabasePlugin {
    @Override
    public void onInit(String pluginName, EditorContext context) {}

    @Override
    public void onSave() {}

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        return new VBox();
    }

    @Override
    public Set<String> getCss() {
        return null;
    }
}

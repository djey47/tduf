package fr.tduf.gui.database.plugins.nope;

import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Test plugin, doing nothing.
 */
public class NopePlugin implements DatabasePlugin {
    @Override
    public void onInit(PluginContext context) {

    }

    @Override
    public Node renderControls(PluginContext context) {
        return new VBox();
    }
}

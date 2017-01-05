package fr.tduf.gui.database.plugins.nope;

import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Test plugin, doing nothing.
 */
public class NopePlugin implements DatabasePlugin {
    @Override
    public void onInit() {

    }

    @Override
    public Node renderControls() {
        return new VBox();
    }
}

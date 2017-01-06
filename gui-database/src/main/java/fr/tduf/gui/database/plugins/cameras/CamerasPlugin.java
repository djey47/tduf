package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Advanced cameras edition plugin
 */
public class CamerasPlugin implements DatabasePlugin {
    @Override
    public void onInit() {

    }

    @Override
    public Node renderControls(PluginContext context) {
        HBox hBox = new HBox();

        hBox.getChildren().add(new Label("This is cameras plugin component dummy"));

        return hBox;
    }
}

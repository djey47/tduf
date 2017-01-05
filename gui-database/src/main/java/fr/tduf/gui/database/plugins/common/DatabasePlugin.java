package fr.tduf.gui.database.plugins.common;

import javafx.scene.Node;

/**
 * To be implemented by all database editor plugins.
 */
public interface DatabasePlugin {
    /**
     * Performs initialization tasks. Will be invoked once database loaded.
     */
    void onInit();

    /**
     * @return rendered FX controls
     */
    Node renderControls();
}

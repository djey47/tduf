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
     * Creates FX controls at specified join point.
     * @param parentNode    : join point
     */
    void renderControls(Node parentNode);
}

package fr.tduf.gui.database.plugins.common;

import javafx.scene.Node;

import java.io.IOException;

/**
 * To be implemented by all database editor plugins.
 */
public interface DatabasePlugin {
    /**
     * Performs initialization tasks. Will be invoked once database loaded.
     * @param context : all required information about Database Editor
     */
    void onInit(EditorContext context) throws IOException;

    /**
     * Performs saving tasks or processing drawn by database saving
     * @param context : all required information about Database Editor
     */
    void onSave(EditorContext context) throws IOException;

    /**
     * @param context : all required information about Database Editor
     * @return rendered FX controls
     */
    Node renderControls(EditorContext context);
}

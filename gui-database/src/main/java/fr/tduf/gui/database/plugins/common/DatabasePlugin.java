package fr.tduf.gui.database.plugins.common;

import javafx.scene.Node;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

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

    /**
     * @return necessary stylesheet(s). Null or empty are allowed.
     */
    Set<String> getCss();

    /**
     * Marks an initialization error onto this plugin
     * @param initException: exception thrown at initialization
     */
    void setInitError(Exception initException);

    /**
     * @return exception thrown at initialization, if any
     */
    Optional<Exception> getInitError();
}

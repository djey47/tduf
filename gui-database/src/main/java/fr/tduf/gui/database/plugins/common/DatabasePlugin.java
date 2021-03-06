package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
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
     * @param pluginName : name of plugin
     * @param context : all required information about Database Editor
     */
    void onInit(String pluginName, EditorContext context) throws IOException;

    /**
     * Performs saving tasks or processing drawn by database saving
     */
    void onSave() throws IOException;

    /**
     * @param onTheFlyContext : all required information about current context
     * @return rendered FX controls
     */
    Node renderControls(OnTheFlyContext onTheFlyContext);

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
     * Marks a save error onto this plugin
     * @param saveException: exception thrown at initialization
     */
    void setSaveError(Exception saveException);

    /**
     * @return exception thrown at initialization, if any
     */
    Optional<Exception> getInitError();

    /**
     * @return exception thrown at saving, if any
     */
    Optional<Exception> getSaveError();

    /**
     * @return application context
     */
    EditorContext getEditorContext();

    /**
     * @return plugin name
     */
    String getName();
}

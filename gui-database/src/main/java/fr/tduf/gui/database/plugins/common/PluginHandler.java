package fr.tduf.gui.database.plugins.common;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageController;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import static java.util.Objects.requireNonNull;

/**
 * Ensures support for database editor plugins.
 */
public class PluginHandler {
    private static final String THIS_CLASS_NAME = PluginHandler.class.getSimpleName();

    private final PluginContext context = new PluginContext();

    /**
     * Creates plugin handler for specified database editor controller
     */
    public PluginHandler(MainStageController mainStageController) {
        context.setMainStageController(requireNonNull(mainStageController, "Main stage controller instance is required."));
    }

    /**
     * Calls all init methods from all plugins in index
     */
    public void initializeAllPlugins() {
       PluginIndex.allAsStream().forEach(this::initializePlugin);
    }

    /**
     * Renders plugin controls and attach to provided parent node.
     * @param pluginName    : must match a valid name in PluginIndex
     * @param parentPane    : required
     */
    public void renderPluginByName(String pluginName, Pane parentPane) {
        requireNonNull(parentPane, "A parent node to attach rendered component to is required");

        try {
            PluginIndex resolvedPlugin = PluginIndex.valueOf(pluginName);

            Node renderedNode = resolvedPlugin.getPluginInstance().renderControls(context);
            parentPane.getChildren().add(renderedNode);
        } catch(Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occured while rendering plugin: " + pluginName, e);
        }
    }

    private void initializePlugin(PluginIndex pluginIndex) {
        Log.debug(THIS_CLASS_NAME, "Now initializing plugin: " + pluginIndex);

        try {
            pluginIndex.getPluginInstance().onInit();
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occured while initializing plugin: " + pluginIndex, e);
        }
    }

    public PluginContext getContext() {
        return context;
    }
}

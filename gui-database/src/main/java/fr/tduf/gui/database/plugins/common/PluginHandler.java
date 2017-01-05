package fr.tduf.gui.database.plugins.common;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Ensures support for database editor plugins.
 */
// TODO public methods can't crash application: catch and log errors
public class PluginHandler {
    private static final String THIS_CLASS_NAME = PluginHandler.class.getSimpleName();

    private PluginContext context = new PluginContext();

    /**
     * Creates plugin handler for specified database editor controller
     */
    public PluginHandler(MainStageController mainStageController) {
        context.setMainStageController(mainStageController);
    }

    /**
     * Calls all init methods from all plugins in index
     * @param databaseObjects   : loaded database topic objects
     */
    public void initializeAllPlugins(List<DbDto> databaseObjects) {
       PluginIndex.allAsStream().forEach(this::initializePlugin);
    }

    /**
     * Renders plugin controls and attach to provided parent node.
     * @param pluginName    : must match a valid name in PluginIndex
     * @param parentPane    : required
     */
    public void renderPluginByName(String pluginName, Pane parentPane) {
        requireNonNull(parentPane, "A parent node to attach rendered component to is reuired");

        PluginIndex resolvedPlugin = PluginIndex.valueOf(pluginName);

        Node renderedNode = resolvedPlugin.getPluginInstance().renderControls(context);
        parentPane.getChildren().add(renderedNode);
    }

    private void initializePlugin(PluginIndex pluginIndex) {
        Log.debug(THIS_CLASS_NAME, "Now initializing plugin: " + pluginIndex);

        pluginIndex.getPluginInstance().onInit();
    }

    public PluginContext getContext() {
        return context;
    }
}

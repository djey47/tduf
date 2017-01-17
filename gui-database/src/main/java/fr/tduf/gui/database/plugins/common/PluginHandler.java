package fr.tduf.gui.database.plugins.common;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Ensures support for database editor plugins.
 */
public class PluginHandler {
    private static final String THIS_CLASS_NAME = PluginHandler.class.getSimpleName();

    private final EditorContext context = new EditorContext();

    /**
     * Creates plugin handler for specified database editor controller
     */
    public PluginHandler(Parent root, MainStageChangeDataController changeDataController) {
        context.setChangeDataController(requireNonNull(changeDataController, "Change data controller instance is required."));

        PluginIndex.allAsStream().forEach(p -> addPluginCss(p, root));
    }

    /**
     * Calls all init methods from all plugins in index
     */
    public void initializeAllPlugins() {
       PluginIndex.allAsStream().forEach(this::initializePlugin);
    }

    /**
     * Calls all triggerOnSaveForPlugin methods from all plugins in index
     */
    public void triggerOnSaveForAllPLugins() {
        PluginIndex.allAsStream().forEach(this::triggerOnSaveForPlugin);
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
            pluginIndex.getPluginInstance().onInit(context);
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occured while initializing plugin: " + pluginIndex, e);
        }
    }

    private void triggerOnSaveForPlugin(PluginIndex pluginIndex) {
        Log.debug(THIS_CLASS_NAME, "Now triggering onSave for plugin: " + pluginIndex);

        try {
            pluginIndex.getPluginInstance().onSave(context);
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occured while triggering onSave for plugin: " + pluginIndex, e);
        }
    }

    private void addPluginCss(PluginIndex pluginIndex, Parent root) {
        Log.debug(THIS_CLASS_NAME, "Now adding CSS for plugin: " + pluginIndex);

        try {
            Set<String> allCss = pluginIndex.getPluginInstance().getCss();
            if (allCss != null) {
                root.getStylesheets().addAll(allCss);
            }
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occured while invoking getCss for plugin: " + pluginIndex, e);
        }
    }

    public EditorContext getContext() {
        return context;
    }
}

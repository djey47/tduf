package fr.tduf.gui.database.plugins.common;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.ImageConstants;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static fr.tduf.gui.database.plugins.common.FxConstants.*;
import static java.util.Objects.requireNonNull;

/**
 * Ensures support for database editor plugins.
 */
public class PluginHandler {
    private static final Class<PluginHandler> thisClass = PluginHandler.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private final EditorContext editorContext = new EditorContext();

    /**
     * Creates plugin handler for specified database editor controller
     */
    public PluginHandler(Parent root, MainStageChangeDataController changeDataController) {
        editorContext.setChangeDataController(requireNonNull(changeDataController, "Change data controller instance is required."));

        Scene scene = requireNonNull(root, "Root FX component instance is required.").getScene();
        if (scene != null) {
            editorContext.setMainWindow(scene.getWindow());
        }

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
     * @param onTheFlyContext : required
     */
    public void renderPluginByName(String pluginName, OnTheFlyContext onTheFlyContext) {
        requireNonNull(onTheFlyContext, "\"On The fly\" context is required");

        try {
            PluginIndex resolvedPlugin = PluginIndex.valueOf(pluginName);
            DatabasePlugin pluginInstance = resolvedPlugin.getPluginInstance();
            renderPluginInstance(pluginInstance, onTheFlyContext);

        } catch(Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occurred while rendering plugin: " + pluginName, e);
        }
    }

    /**
     * Utility method to extract CSS from given resource
     * @param cssResource : resource path
     * @return CSS contents to be provided to FX engine
     */
    public static String fetchCss(String cssResource) {
        return thisClass.getResource(cssResource).toExternalForm();
    }

    void initializePluginInstance(DatabasePlugin pluginInstance, String pluginName) {
        try {
            pluginInstance.onInit(pluginName, editorContext);
            pluginInstance.setInitError(null);
        } catch (IOException ioe) {
            Log.error(THIS_CLASS_NAME, "Error occurred while initializing plugin: " + pluginName, ioe);
            pluginInstance.setInitError(ioe);
        }
    }

    void renderPluginInstance(DatabasePlugin pluginInstance, OnTheFlyContext onTheFlyContext) {
        // Error handling
        Optional<Exception> initError = pluginInstance.getInitError();
        final Node renderedNode = initError
                .map(e -> renderErrorPlaceholder(e, pluginInstance.getName()))
                .orElseGet(() -> pluginInstance.renderControls(onTheFlyContext));
        onTheFlyContext.getParentPane().getChildren().add(renderedNode);
    }

    void triggerOnSaveForPluginInstance(DatabasePlugin pluginInstance) {
        try {
            pluginInstance.onSave();
            pluginInstance.setSaveError(null);
        } catch (IOException ioe) {
            Log.error(THIS_CLASS_NAME, "Error occurred while triggering onSave for plugin: " + pluginInstance, ioe);
            pluginInstance.setSaveError(ioe);
        }
    }

    private void initializePlugin(PluginIndex pluginIndex) {
        Log.debug(THIS_CLASS_NAME, "Now initializing plugin: " + pluginIndex);

        initializePluginInstance(pluginIndex.getPluginInstance(), pluginIndex.name());
    }

    private void triggerOnSaveForPlugin(PluginIndex pluginIndex) {
        Log.debug(THIS_CLASS_NAME, "Now triggering onSave for plugin: " + pluginIndex);

        triggerOnSaveForPluginInstance(pluginIndex.getPluginInstance());
    }

    private void addPluginCss(PluginIndex pluginIndex, Parent root) {
        Log.debug(THIS_CLASS_NAME, "Now adding CSS for plugin: " + pluginIndex);

        try {
            Set<String> allCss = pluginIndex.getPluginInstance().getCss();
            if (allCss != null) {
                root.getStylesheets().addAll(allCss);
            }
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, "Error occurred while invoking getCss for plugin: " + pluginIndex, e);
        }
    }

    private Node renderErrorPlaceholder(Exception parentException, String pluginName) {
        HBox placeholderNode = new HBox();
        placeholderNode.getStyleClass().addAll(CSS_CLASS_PLUGIN_BOX, CSS_CLASS_PLUGIN_ERROR_PLACEHOLDER);

        VBox mainColumnBox = new VBox();
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox titleBox = new HBox();
        Image errorSignImage = new Image(ImageConstants.RESOURCE_RED_WARN, 24.0, 24.0, true, true);
        ImageView errorImageView = new ImageView(errorSignImage);
        String errorTitle = String.format(DisplayConstants.TITLE_PLUGIN_INIT_ERROR, pluginName);
        Label errorTitleLabel = new Label(errorTitle);
        errorTitleLabel.getStyleClass().add(CSS_CLASS_PLUGIN_ERROR_TITLE_LABEL);
        titleBox.getChildren().addAll(errorImageView, errorTitleLabel);

        String errorMessage = parentException.getMessage();
        Label errorMessageLabel = new Label(errorMessage);
        errorMessageLabel.getStyleClass().add(CSS_CLASS_PLUGIN_ERROR_MESSAGE_LABEL);
        mainColumnChildren.addAll(titleBox, errorMessageLabel);

        ObservableList<Node> mainRowChildren = placeholderNode.getChildren();
        mainRowChildren.add(mainColumnBox);

        return placeholderNode;
    }

    public EditorContext getEditorContext() {
        return editorContext;
    }
}

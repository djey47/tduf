package fr.tduf.gui.database.plugins.contentRef;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.ImageConstants;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.HashSet;
import java.util.Set;

import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_PLUGIN_BOX;
import static fr.tduf.gui.database.plugins.contentRef.common.DisplayConstants.TOOLTIP_WARNING;
import static fr.tduf.gui.database.plugins.contentRef.common.FxConstants.CSS_CLASS_CONTENT_REF_PLUGIN_BOX;
import static fr.tduf.gui.database.plugins.contentRef.common.FxConstants.PATH_RESOURCE_CSS_CONTENT_REF;
import static java.util.Collections.singletonList;

/**
 * Simple plugin to decorate content REF fields
 */
public class ContentRefPlugin extends AbstractDatabasePlugin {
    private Class<ContentRefPlugin> thisClass = ContentRefPlugin.class;

    private String THIS_CLASS_NAME = thisClass.getSimpleName();

    /**
     * Required contextual information: none
     * @param context : all required information about Database Editor
     */
    @Override
    public void onInit(EditorContext context) {
        Log.info(THIS_CLASS_NAME, "Ready!");
    }

    /**
     * Required contextual information: none
     * @param context : all required information about Database Editor
     */
    @Override
    public void onSave(EditorContext context) {}

    /**
     * Required contextual information: none
     * @param context : all required information about Database Editor
     */
    @Override
    public Node renderControls(EditorContext context) {
        HBox hBox = new HBox();
        hBox.getStyleClass().addAll(CSS_CLASS_PLUGIN_BOX, CSS_CLASS_CONTENT_REF_PLUGIN_BOX);

        Image warnSignImage = new Image(ImageConstants.RESOURCE_WARN, 24.0, 24.0, true, true);
        ImageView imageView = new ImageView(warnSignImage);

        Tooltip tooltip = new Tooltip(TOOLTIP_WARNING);
        Tooltip.install(imageView, tooltip);

        hBox.getChildren().add(imageView);

        return hBox;
    }

    @Override
    public Set<String> getCss() {
        return new HashSet<>(singletonList(thisClass.getResource(PATH_RESOURCE_CSS_CONTENT_REF).toExternalForm()));
    }
}

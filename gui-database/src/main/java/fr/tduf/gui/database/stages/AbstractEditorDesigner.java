package fr.tduf.gui.database.stages;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.libunlimited.framework.base.Files;
import javafx.scene.Parent;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static fr.tduf.gui.database.common.FxConstants.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Parent of all database editor frame designers
 */
public abstract class AbstractEditorDesigner {
    private static final Class<AbstractEditorDesigner> thisClass = AbstractEditorDesigner.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    /**
     * Adds all common CSS to specified root node, taking into account custom theme
     * @param root : root node
     */
    protected static void initCommonCss(Parent root) {
        String styledCommonCss = thisClass.getResource(PATH_RESOURCE_CSS_COMMON).toExternalForm();
        String styledToolBarCss = thisClass.getResource(PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        String themeCss = DatabaseEditor.getInstance().getApplicationConfiguration().getEditorCustomThemeCss()
                .flatMap(path -> {
                    String themeFileURI = Files.PROTOCOL_FILE + path.toString();
                    try {
                        File themeFile = new File(new URI(themeFileURI));
                        if (!themeFile.exists()) {
                            return empty();
                        }
                    } catch (URISyntaxException use) {
                        return empty();
                    }
                    return of(themeFileURI);
                })
                .orElseGet(() -> {
                    Log.warn(THIS_CLASS_NAME, "Editor theme not found, using default colors");
                    return thisClass.getResource(PATH_RESOURCE_CSS_COLORS).toExternalForm();
                });

        root.getStylesheets().addAll(styledCommonCss, styledToolBarCss, themeCss);
    }
}

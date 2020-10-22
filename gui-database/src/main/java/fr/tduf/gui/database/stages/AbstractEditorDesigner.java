package fr.tduf.gui.database.stages;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.DatabaseEditor;
import javafx.scene.Parent;

import java.io.File;

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
                    Log.debug(THIS_CLASS_NAME, "custom theme path=" + path);
                    File themeFile = path.toFile();
                    if (!themeFile.exists()) {
                        Log.debug(THIS_CLASS_NAME, "non existing themeFile:" + themeFile);
                        return empty();
                    }
                    return of(themeFile.toURI().toString());
                })
                .orElseGet(() -> {
                    Log.warn(THIS_CLASS_NAME, "Editor theme not found, using default colors");
                    return thisClass.getResource(PATH_RESOURCE_CSS_COLORS).toExternalForm();
                });

        root.getStylesheets().addAll(styledCommonCss, styledToolBarCss, themeCss);
    }
}

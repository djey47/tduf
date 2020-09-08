package fr.tduf.gui.database.stages;

import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.libunlimited.framework.base.Files;
import javafx.scene.Parent;

import static fr.tduf.gui.database.common.FxConstants.*;

/**
 * Parent of all database editor frame designers
 */
public abstract class AbstractEditorDesigner {
    private static final Class<AbstractEditorDesigner> thisClass = AbstractEditorDesigner.class;

    protected static void initCommonCss(Parent root) {
        String styledCommonCss = thisClass.getResource(PATH_RESOURCE_CSS_COMMON).toExternalForm();
        String styledToolBarCss = thisClass.getResource(PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        String themeCss = DatabaseEditor.getInstance().getApplicationConfiguration().getEditorCustomThemeCss()
                .map(path -> Files.PROTOCOL_FILE + path.toString())
                .orElse(thisClass.getResource(PATH_RESOURCE_CSS_COLORS).toExternalForm());

        root.getStylesheets().addAll(styledCommonCss, styledToolBarCss, themeCss);
    }
}

package fr.tduf.gui.database.stages;

import javafx.scene.Parent;

import static fr.tduf.gui.database.common.FxConstants.*;

/**
 * Parent of all database editor frame designers
 */
public abstract class AbstractEditorDesigner {
    private static final Class<AbstractEditorDesigner> thisClass = AbstractEditorDesigner.class;

    protected static void initCommonCss(Parent root) {
        String styledCommonCss = thisClass.getResource(PATH_RESOURCE_CSS_COMMON).toExternalForm();
        String styledColorsCss = thisClass.getResource(PATH_RESOURCE_CSS_COLORS).toExternalForm();
        String styledToolBarCss = thisClass.getResource(PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();

        root.getStylesheets().addAll(styledCommonCss, styledColorsCss, styledToolBarCss);
    }
}

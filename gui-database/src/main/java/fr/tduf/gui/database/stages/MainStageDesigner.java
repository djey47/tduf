package fr.tduf.gui.database.stages;

import fr.tduf.gui.common.stages.StageHelper;
import fr.tduf.gui.database.common.FxConstants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static fr.tduf.gui.database.common.DisplayConstants.TITLE_APPLICATION;
import static fr.tduf.gui.database.common.FxConstants.PATH_RESOURCE_CSS_PANES;
import static fr.tduf.gui.database.common.FxConstants.PATH_RESOURCE_CSS_TABCONTENTS;

/**
 * Loads graphical interface for main window.
 */
public class MainStageDesigner extends AbstractEditorDesigner {

    private static final Class<MainStageDesigner> thisClass = MainStageDesigner.class;

    /**
     * Loads main scene from FXML resource.
     * @param primaryStage  : reference to primary stage, returned by FX engine.
     */
    public static void init(Stage primaryStage) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_MAIN_STAGE_DESIGNER));
        Parent mainRoot = mainLoader.load();

        initMainWindow(primaryStage, mainRoot);
    }

    private static void initMainWindow(Stage primaryStage, Parent mainRoot) {
        initCommonCss(mainRoot);

        String styledTabContentsCss = thisClass.getResource(PATH_RESOURCE_CSS_TABCONTENTS).toExternalForm();
        String styledPanesCss = thisClass.getResource(PATH_RESOURCE_CSS_PANES).toExternalForm();
        String styledPluginsCommonCss = thisClass.getResource(fr.tduf.gui.database.plugins.common.FxConstants.PATH_RESOURCE_CSS_PLUGINS_COMMON).toExternalForm();
        mainRoot.getStylesheets().addAll(styledTabContentsCss, styledPanesCss, styledPluginsCommonCss);

        primaryStage.setScene(new Scene(mainRoot, 1280, 720));
        primaryStage.setTitle(TITLE_APPLICATION);

        StageHelper.setStandardIcon(primaryStage);
    }
}

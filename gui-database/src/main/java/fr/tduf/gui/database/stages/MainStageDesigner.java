package fr.tduf.gui.database.stages;

import fr.tduf.gui.common.stages.StageHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for main window.
 */
public class MainStageDesigner {

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
        String styledToolBarCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        String styledTabContentsCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TABCONTENTS).toExternalForm();
        String styledPanesCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_PANES).toExternalForm();

        // TODO see to add plugin css at PluginHandler creation (provided by plugin interface)
        String camerasPluginCss = thisClass.getResource(fr.tduf.gui.database.plugins.cameras.common.FxConstants.PATH_RESOURCE_CSS_CAMERAS).toExternalForm();

        mainRoot.getStylesheets().addAll(styledToolBarCss, styledTabContentsCss, styledPanesCss, camerasPluginCss);

        primaryStage.setScene(new Scene(mainRoot, 1280, 720));
        primaryStage.setTitle(DisplayConstants.TITLE_APPLICATION);

        StageHelper.setStandardIcon(primaryStage);
    }
}

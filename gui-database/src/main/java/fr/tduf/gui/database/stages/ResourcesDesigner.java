package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for resources window.
 */
public class ResourcesDesigner {

    private static final Class<ResourcesDesigner> thisClass = ResourcesDesigner.class;

    /**
     * Loads main scene from FXML resource.
     * @param resourcesStage  : reference to resources stage.
     */
    public static void init(Stage resourcesStage) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_RES_STAGE_DESIGNER));
        Parent mainRoot = mainLoader.load();

        initWindow(resourcesStage, mainRoot);
    }

    private static void initWindow(Stage resourcesStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        Scene scene = new Scene(mainRoot, 640, 768);
        scene.setUserData(resourcesStage.getUserData());         // Hack to give main controller reference
        resourcesStage.setScene(scene);

        resourcesStage.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
        resourcesStage.setResizable(false);
    }
}
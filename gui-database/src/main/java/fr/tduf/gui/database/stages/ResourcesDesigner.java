package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.ResourcesStageController;
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
     * Loads scene from FXML resource.
     * @param resourcesStage  : reference to resources stage.
     */
    public static ResourcesStageController init(Stage resourcesStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_RES_STAGE_DESIGNER));
        Parent root = loader.load();

        initWindow(resourcesStage, root);

        return loader.getController();
    }

    private static void initWindow(Stage resourcesStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        resourcesStage.setScene(new Scene(mainRoot, 650, 720));
        resourcesStage.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
    }
}
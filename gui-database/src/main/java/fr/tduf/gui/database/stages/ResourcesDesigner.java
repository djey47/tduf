package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.main.MainStageController;
import fr.tduf.gui.database.controllers.ResourcesStageController;
import javafx.application.Platform;
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

    private ResourcesDesigner() {}

    /**
     * Loads scene from FXML resource.
     * @param mainStageController   :
     */
    public static ResourcesStageController init(MainStageController mainStageController) throws IOException {
        Stage resourcesStage = new Stage();
        Platform.runLater(() -> resourcesStage.initOwner(mainStageController.getWindow())); // runLater() ensures main stage will be initialized first.

        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_RES_STAGE_DESIGNER));
        Parent root = loader.load();

        initWindow(resourcesStage, root);

        final ResourcesStageController controller = loader.getController();
        controller.setMainStageController(mainStageController);

        return controller;
    }

    private static void initWindow(Stage resourcesStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        resourcesStage.setScene(new Scene(mainRoot, 1024, 720));
        resourcesStage.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESOURCES);
    }
}

package fr.tduf.gui.database.plugins.materials.stages;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.controllers.main.MainStageController;
import fr.tduf.gui.database.plugins.materials.common.FxConstants;
import fr.tduf.gui.database.plugins.materials.controllers.MaterialAdvancedInfoStageController;
import fr.tduf.gui.database.stages.AbstractEditorDesigner;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set;

import static fr.tduf.gui.database.plugins.common.FxConstants.PATH_RESOURCE_CSS_PLUGINS_COMMON;
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.TITLE_SUB_ADVANCED_INFO;

/**
 * Loads graphical interface for material info window.
 */
public class MaterialAdvancedInfoDesigner extends AbstractEditorDesigner {
    private static final Class<MaterialAdvancedInfoDesigner> thisClass = MaterialAdvancedInfoDesigner.class;

    private MaterialAdvancedInfoDesigner() {}

    /**
     * Loads scene from FXML resource.
     * @param mainStageController   : main stage controller instance
     * @param pluginCss             : all stylesheets to be loaded via plugin def
     */
    public static MaterialAdvancedInfoStageController init(MainStageController mainStageController, Set<String> pluginCss) throws IOException {
        Stage resourcesStage = new Stage();
        Platform.runLater(() -> resourcesStage.initOwner(mainStageController.getWindow())); // runLater() ensures main stage will be initialized first.

        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_ADVANCED_INFO_STAGE_DESIGNER));
        Parent root = loader.load();

        initWindow(resourcesStage, root, pluginCss);

        final MaterialAdvancedInfoStageController controller = loader.getController();
        controller.setMainStageController(mainStageController);

        return controller;
    }

    private static void initWindow(Stage resourcesStage, Parent mainRoot, Set<String> pluginCss) {
        initCommonCss(mainRoot);
        String pluginsCommonCss = thisClass.getResource(PATH_RESOURCE_CSS_PLUGINS_COMMON).toExternalForm();
        mainRoot.getStylesheets().add(pluginsCommonCss);
        mainRoot.getStylesheets().addAll(pluginCss);

        resourcesStage.setScene(new Scene(mainRoot, 1024, 560));
        resourcesStage.setTitle(DisplayConstants.TITLE_APPLICATION + TITLE_SUB_ADVANCED_INFO);
    }
}

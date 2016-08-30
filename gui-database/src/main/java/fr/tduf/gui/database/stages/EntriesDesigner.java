package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.EntriesStageController;
import fr.tduf.gui.database.controllers.MainStageController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for content entries.
 */
public class EntriesDesigner {
    private static final Class<EntriesDesigner> thisClass = EntriesDesigner.class;

    private EntriesDesigner() {}

    /**
     * Loads scene from FXML resource.
     * @param mainStageController  : reference to main stage controller.
     */
    public static EntriesStageController init(MainStageController mainStageController) throws IOException {
        Stage entriesStage = new Stage();
        Platform.runLater(() -> entriesStage.initOwner(mainStageController.getWindow()));

        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_ENTRIES_RES_STAGE_DESIGNER));
        Parent root = loader.load();

        initWindow(entriesStage, root);

        final EntriesStageController entriesStageController = loader.getController();
        entriesStageController.setMainStageController(mainStageController);
        return entriesStageController;
    }

    private static void initWindow(Stage entriesStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        entriesStage.setScene(new Scene(mainRoot, 650, 720));
        entriesStage.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_ENTRIES);
        entriesStage.initModality(Modality.APPLICATION_MODAL);
    }
}

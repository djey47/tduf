package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.FieldsBrowserStageController;
import fr.tduf.gui.database.controllers.main.MainStageController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for content fields browsing.
 */
public class FieldsBrowserDesigner {
    private static final Class<FieldsBrowserDesigner> thisClass = FieldsBrowserDesigner.class;

    private FieldsBrowserDesigner() {}

    /**
     * Loads scene from FXML resource.
     * @param mainStageController  : reference to fields browser stage.
     */
    public static FieldsBrowserStageController init(MainStageController mainStageController) throws IOException {
        Stage fieldsStage = new Stage();
        Platform.runLater(() -> fieldsStage.initOwner(mainStageController.getWindow()));

        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_FIELDS_BROWSER_STAGE_DESIGNER));
        Parent root = loader.load();

        initWindow(fieldsStage, root);

        final FieldsBrowserStageController fieldsBrowserStageController = loader.getController();
        fieldsBrowserStageController.setMainStageController(mainStageController);
        return fieldsBrowserStageController;
    }

    private static void initWindow(Stage entriesStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_TOOLBARS).toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        entriesStage.setScene(new Scene(mainRoot, 650, 720));
        entriesStage.setTitle(DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_FIELDS);
        entriesStage.initModality(Modality.APPLICATION_MODAL);
    }
}

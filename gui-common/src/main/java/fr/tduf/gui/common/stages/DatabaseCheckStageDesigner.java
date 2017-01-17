package fr.tduf.gui.common.stages;

import fr.tduf.gui.common.FxConstants;
import fr.tduf.gui.common.controllers.DatabaseCheckStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for database check results.
 */
public class DatabaseCheckStageDesigner {

    private static final Class<DatabaseCheckStageDesigner> thisClass = DatabaseCheckStageDesigner.class;

    /**
     * Loads scene from FXML resource.
     * @param stage             : reference to stage, returned by FX engine.
     * @param applicationTitle  : title bar label
     */
    public static DatabaseCheckStageController init(Stage stage, String applicationTitle) throws IOException {
        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_DB_CHECK_STAGE_DESIGNER));
        Parent root = loader.load();

        String styledPanesCss = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_CHECK).toExternalForm();

        stage.setScene(new Scene(root, 700, 700));
        stage.setTitle(applicationTitle);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getScene().getStylesheets().add(styledPanesCss);


        return loader.getController();
    }
}

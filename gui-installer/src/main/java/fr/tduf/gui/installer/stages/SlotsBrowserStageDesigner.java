package fr.tduf.gui.installer.stages;

import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FxConstants;
import fr.tduf.gui.installer.controllers.SlotsBrowserStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for vehicle slots browser.
 */
public class SlotsBrowserStageDesigner {

    private static final Class<SlotsBrowserStageDesigner> thisClass = SlotsBrowserStageDesigner.class;

    /**
     * Loads scene from FXML resource.
     * @param stage  : reference to stage, returned by FX engine.
     */
    public static SlotsBrowserStageController init(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_BROWSE_SLOTS_STAGE_DESIGNER));
        Parent root = loader.load();

        stage.setScene(new Scene(root, 775, 700));
        stage.setTitle(DisplayConstants.TITLE_APPLICATION);
        stage.initModality(Modality.APPLICATION_MODAL);

        return loader.getController();
    }
}

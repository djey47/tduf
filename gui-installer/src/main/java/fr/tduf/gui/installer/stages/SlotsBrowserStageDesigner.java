package fr.tduf.gui.installer.stages;

import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FxConstants;
import fr.tduf.gui.installer.controllers.SlotsBrowserStageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for vehicle slots browser.
 */
public class SlotsBrowserStageDesigner {

    private static final Class<SlotsBrowserStageDesigner> thisClass = SlotsBrowserStageDesigner.class;

    /**
     * Loads scene from FXML resource.
     * @param primaryStage  : reference to primary stage, returned by FX engine.
     */
    public static SlotsBrowserStageController init(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(thisClass.getResource(FxConstants.PATH_RESOURCE_BROWSE_SLOTS_STAGE_DESIGNER));
        Parent root = loader.load();

        initMainWindow(primaryStage, root);

        return loader.getController();
    }

    private static void initMainWindow(Stage primaryStage, Parent mainRoot) {
        primaryStage.setScene(new Scene(mainRoot, 750, 750));
        primaryStage.setTitle(DisplayConstants.TITLE_APPLICATION);
    }
}

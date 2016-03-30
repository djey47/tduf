package fr.tduf.gui.installer.stages;

import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FxConstants;
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
        String currentStylesheet = thisClass.getResource(FxConstants.PATH_RESOURCE_CSS_MAIN).toExternalForm();

        primaryStage.setScene(new Scene(mainRoot, 750, 750));
        primaryStage.setTitle(DisplayConstants.TITLE_APPLICATION);
        primaryStage.getScene().getStylesheets().add(currentStylesheet);
    }
}
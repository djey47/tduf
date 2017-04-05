package fr.tduf.gui.launcher.stages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static fr.tduf.gui.launcher.common.DisplayConstants.TITLE_APPLICATION;
import static fr.tduf.gui.launcher.common.FxConstants.PATH_RESOURCE_MAIN_STAGE_DESIGNER;

/**
 * Loads graphical interface for main window.
 */
public class MainStageDesigner {

    private static final Class<MainStageDesigner> thisClass = MainStageDesigner.class;

    private MainStageDesigner() {}

    /**
     * Loads main scene from FXML resource.
     * @param primaryStage  : reference to primary stage, returned by FX engine.
     */
    public static void init(Stage primaryStage) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader(thisClass.getResource(PATH_RESOURCE_MAIN_STAGE_DESIGNER));
        Parent mainRoot = mainLoader.load();

        initMainWindow(primaryStage, mainRoot);
    }

    private static void initMainWindow(Stage primaryStage, Parent mainRoot) {
        primaryStage.setScene(new Scene(mainRoot, 1280, 360));
        primaryStage.setTitle(TITLE_APPLICATION);
    }
}

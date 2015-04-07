package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.controllers.MainStageController;
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
        FXMLLoader mainLoader = new FXMLLoader(thisClass.getResource("/designer/MainDesigner.fxml"));
        Parent mainRoot = mainLoader.load();
        MainStageController mainController = mainLoader.<MainStageController>getController();
        mainController.setMainStage(primaryStage);

        initMainWindow(primaryStage, mainRoot);
    }

    private static void initMainWindow(Stage primaryStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource("/css/ToolBars.css").toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        primaryStage.setScene(new Scene(mainRoot, 1280, 768));
        primaryStage.setTitle("TDUF Database Editor");
        primaryStage.setResizable(false);
    }
}
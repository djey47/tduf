package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.common.DisplayConstants;
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

        initMainWindow(primaryStage, mainRoot);
    }

    private static void initMainWindow(Stage primaryStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource("/css/ToolBars.css").toExternalForm();
        String styledTabContentsCss = thisClass.getResource("/css/TabContents.css").toExternalForm();
        mainRoot.getStylesheets().addAll(styledToolBarCss, styledTabContentsCss);

        primaryStage.setScene(new Scene(mainRoot, 1280, 768));
        primaryStage.setTitle(DisplayConstants.TITLE_APPLICATION);
        primaryStage.setResizable(false);
    }
}
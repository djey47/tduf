package fr.tduf.gui.database.stages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads graphical interface for main window.
 */
public class MainStageDesigner {

    private static final Class<MainStageDesigner> thisClass = MainStageDesigner.class;

    /**
     *
     * @param primaryStage
     */
    public static void init(Stage primaryStage) throws IOException {

        String styledToolBarCss = thisClass.getResource("/css/ToolBars.css").toExternalForm();
        Parent parent = FXMLLoader.load(thisClass.getResource("/designer/MainDesigner.fxml"));
        parent.getStylesheets().add(styledToolBarCss);

        primaryStage.setScene(new Scene(parent, 1280, 768));
        primaryStage.setTitle("TDUF Database Editor");
        primaryStage.setResizable(false);

        TitledPane settingsPane = (TitledPane) primaryStage.getScene().lookup("#settingsPane");
        settingsPane.setExpanded(false);
    }
}
package fr.tduf.gui.database;

import fr.tduf.gui.database.stages.MainStageDesigner;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

/**
 * Application entry point.
 */
public class DatabaseEditor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainStageDesigner.init(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

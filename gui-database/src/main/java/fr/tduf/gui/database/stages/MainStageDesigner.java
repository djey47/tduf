package fr.tduf.gui.database.stages;

import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;

import static java.util.Arrays.asList;

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
        FXMLLoader mainLoader = new FXMLLoader(thisClass.getResource("/designer/MainDesigner.fxml"));
        Parent mainRoot = mainLoader.load();
        MainStageController mainController = mainLoader.<MainStageController>getController();
        mainController.setMainStage(primaryStage);

        initMainWindow(primaryStage, mainRoot);

        initSettingsPane(mainController);

        // DEBUG
        mainController.getDatabaseLocationTextField().setText("/media/DevStore/GIT/tduf/cli/integ-tests/db-json/");
    }

    private static void initMainWindow(Stage primaryStage, Parent mainRoot) {
        String styledToolBarCss = thisClass.getResource("/css/ToolBars.css").toExternalForm();
        mainRoot.getStylesheets().add(styledToolBarCss);

        primaryStage.setScene(new Scene(mainRoot, 1280, 768));
        primaryStage.setTitle("TDUF Database Editor");
        primaryStage.setResizable(false);
    }

    private static void initSettingsPane(MainStageController controller) {
        fillLocales(controller);

        controller.getSettingsPane().setExpanded(false);
    }

    private static void fillLocales(MainStageController controller) {
        ChoiceBox localesChoiceBox = controller.getLocalesChoiceBox();

        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> localesChoiceBox.getItems().add(locale));

        localesChoiceBox.setValue(DbResourceDto.Locale.UNITED_STATES);
    }
}
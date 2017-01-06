package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Advanced cameras edition plugin
 */
public class CamerasPlugin implements DatabasePlugin {
    @Override
    public void onInit() {

    }

    @Override
    public Node renderControls(PluginContext context) {
        HBox hBox = new HBox();

        VBox mainColumnBox = new VBox();

        ComboBox<Integer> cameraSelectorComboBox = new ComboBox<>();
        mainColumnBox.getChildren().add(cameraSelectorComboBox);

        HBox viewSelectorBox = new HBox();
        ComboBox<Integer> viewSelectorComboBox = new ComboBox<>();
        viewSelectorBox.getChildren().add(new Label("Available views:"));
        viewSelectorBox.getChildren().add(viewSelectorComboBox);
        mainColumnBox.getChildren().add(viewSelectorBox);

        TableView<String> setPropertyTableView = new TableView<>();
        mainColumnBox.getChildren().add(setPropertyTableView);



        hBox.getChildren().add(mainColumnBox);

        return hBox;
    }
}

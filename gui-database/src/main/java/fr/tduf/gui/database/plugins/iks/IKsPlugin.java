package fr.tduf.gui.database.plugins.iks;

import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fr.tduf.gui.database.plugins.iks.common.DisplayConstants.LABEL_AVAILABLE_IKS;
import static fr.tduf.gui.database.plugins.iks.common.FxConstants.*;
import static java.util.Collections.singletonList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.layout.Priority.ALWAYS;

public class IKsPlugin implements DatabasePlugin {
    private static final Class<IKsPlugin> thisClass = IKsPlugin.class;

    @Override
    public void onInit(EditorContext context) throws IOException {}

    @Override
    public void onSave(EditorContext context) throws IOException {}

    @Override
    public Node renderControls(EditorContext context) {
        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        VBox mainColumnBox = createMainColumn(context);

        ObservableList<Node> mainRowChildren = hBox.getChildren();
        mainRowChildren.add(mainColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));

        return hBox;
    }

    @Override
    public Set<String> getCss() {
        return new HashSet<>(singletonList(thisClass.getResource(PATH_RESOURCE_CSS_IKS).toExternalForm()));
    }

    private VBox createMainColumn(EditorContext context) {
        ObservableList<String> ikItems = FXCollections.observableArrayList();

        // TODO use metadata class 'IKInfo' instead of String
        ComboBox<String> ikSelectorComboBox = new ComboBox<>(ikItems);
        ikSelectorComboBox.getStyleClass().add(CSS_CLASS_IK_SELECTOR_COMBOBOX);
//        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter());

        VBox mainColumnBox = new VBox();
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox ikSelectorBox = createIkSelectorBox(context, ikItems, ikSelectorComboBox);

        mainColumnChildren.add(ikSelectorBox);
        return mainColumnBox;
    }

    private HBox createIkSelectorBox(EditorContext context, ObservableList<String> ikItems, ComboBox<String> ikSelectorComboBox) {
        HBox camSelectorBox = new HBox();
        camSelectorBox.getStyleClass().add(CSS_CLASS_IK_SELECTOR_BOX);

//        ikSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
//                getCameraSelectorChangeListener(context.getFieldRank(), context.getCurrentTopic(), context.getChangeDataController(), viewSelectorComboBox.valueProperty(), viewSelectorComboBox.itemsProperty().get()));
//        Bindings.bindBidirectional(
//                context.getRawValueProperty(), cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraItems));

        Label availableIKsLabel = new Label(LABEL_AVAILABLE_IKS);
        availableIKsLabel.setLabelFor(ikSelectorComboBox);
        availableIKsLabel.getStyleClass().add(CSS_CLASS_AVAILABLE_IKS_LABEL);

        Region camRegion = new Region();
        HBox.setHgrow(camRegion, ALWAYS);

        camSelectorBox.getChildren().add(availableIKsLabel);
        camSelectorBox.getChildren().add(camRegion);
        camSelectorBox.getChildren().add(ikSelectorComboBox);

        return camSelectorBox;
    }
}

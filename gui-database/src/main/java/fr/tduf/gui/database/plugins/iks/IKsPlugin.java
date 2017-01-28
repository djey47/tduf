package fr.tduf.gui.database.plugins.iks;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.gui.database.plugins.iks.converter.IKReferenceToItemConverter;
import fr.tduf.gui.database.plugins.iks.converter.IKReferenceToRawValueConverter;
import fr.tduf.libunlimited.high.files.db.common.helper.CameraAndIKHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_ITEM_LABEL;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_PLUGIN_BOX;
import static fr.tduf.gui.database.plugins.iks.common.DisplayConstants.LABEL_AVAILABLE_IKS;
import static fr.tduf.gui.database.plugins.iks.common.FxConstants.*;
import static fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto.TopicMetadataDto.FIELD_RANK_IK;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.layout.Priority.ALWAYS;

public class IKsPlugin implements DatabasePlugin {
    private static final Class<IKsPlugin> thisClass = IKsPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private CameraAndIKHelper ikRefHelper;

    /**
     * Required contextual information: none
     */
    @Override
    public void onInit(EditorContext context) throws IOException {
        ikRefHelper = new CameraAndIKHelper();
        Log.info(THIS_CLASS_NAME, "IK reference loaded");
    }

    /**
     * Required contextual information: none
     */
    @Override
    public void onSave(EditorContext context) throws IOException {}

    /**
     * Required contextual information:
     * - rawValueProperty
     * @param context : all required information about Database Editor
     */
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
        Map<Integer, String> reference = ikRefHelper.getIKReference();

        ComboBox<Map.Entry<Integer, String>> ikSelectorComboBox = new ComboBox<>(
                observableArrayList(reference.entrySet()).sorted(comparing(Map.Entry::getValue)));
        ikSelectorComboBox.getStyleClass().add(CSS_CLASS_IK_SELECTOR_COMBOBOX);
        ikSelectorComboBox.setConverter(new IKReferenceToItemConverter());

        VBox mainColumnBox = new VBox();
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox ikSelectorBox = createIkSelectorBox(context, reference, ikSelectorComboBox);

        mainColumnChildren.add(ikSelectorBox);
        return mainColumnBox;
    }

    private HBox createIkSelectorBox(EditorContext context, Map<Integer, String> reference, ComboBox<Map.Entry<Integer, String>> ikSelectorComboBox) {
        HBox camSelectorBox = new HBox();
        camSelectorBox.getStyleClass().add(CSS_CLASS_IK_SELECTOR_BOX);

        ikSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getIKSelectorChangeListener(context.getChangeDataController()));
        Bindings.bindBidirectional(
                context.getRawValueProperty(), ikSelectorComboBox.valueProperty(), new IKReferenceToRawValueConverter(reference));

        Label availableIKsLabel = new Label(LABEL_AVAILABLE_IKS);
        availableIKsLabel.setLabelFor(ikSelectorComboBox);
        availableIKsLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        Region camRegion = new Region();
        HBox.setHgrow(camRegion, ALWAYS);

        camSelectorBox.getChildren().add(availableIKsLabel);
        camSelectorBox.getChildren().add(camRegion);
        camSelectorBox.getChildren().add(ikSelectorComboBox);

        return camSelectorBox;
    }

    private ChangeListener<Map.Entry<Integer, String>> getIKSelectorChangeListener(MainStageChangeDataController changeDataController) {
        return (ObservableValue<? extends Map.Entry<Integer, String>> observable, Map.Entry<Integer, String> oldValue, Map.Entry<Integer, String> newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            if (newValue != null) {
                changeDataController.updateContentItem(CAR_PHYSICS_DATA, FIELD_RANK_IK, Integer.toString(newValue.getKey()));
            }
        };
    }
}

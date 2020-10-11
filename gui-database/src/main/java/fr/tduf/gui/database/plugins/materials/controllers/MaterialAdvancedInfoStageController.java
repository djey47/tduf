package fr.tduf.gui.database.plugins.materials.controllers;

import fr.tduf.gui.database.controllers.AbstractEditorController;
import fr.tduf.gui.database.plugins.materials.converter.LayerFlagsToLabelConverter;
import fr.tduf.gui.database.plugins.materials.converter.LayerTextureToLabelConverter;
import fr.tduf.gui.database.plugins.materials.converter.LayerToItemConverter;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToAdvancedTitleConverter;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Layer;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.LayerGroup;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

import java.util.function.Function;

/**
 * Controller to display advanced material info via dedicated dialog.
 */
public class MaterialAdvancedInfoStageController extends AbstractEditorController {

    @FXML
    private Label titleLabel;

    @FXML
    private Accordion infoAccordion;

    @FXML
    private TitledPane shaderInfoPane;

    @FXML
    private ChoiceBox<Layer> layersChoiceBox;

    @FXML
    private Label layerFlagsLabel;

    @FXML
    private Label layerTextureNameLabel;

    private final Property<Layer> currentLayerProperty = new SimpleObjectProperty<>();

    private final Property<Material> materialProperty = new SimpleObjectProperty<>();

    private Function<String, String> fullNameProvider;

    @Override
    public void init() {
        initTopBar();

        initLayersPane();

        initStatusBar();
    }

    /**
     * Displays dialog
     * @param materialProperty  : material used to provide info
     * @param fullNameProvider  : function allowing to resolve material full name from normalized one
     */
    public void showDialog(Property<Material> materialProperty, Function<String, String> fullNameProvider) {
        this.materialProperty.setValue(materialProperty.getValue());
        this.fullNameProvider = fullNameProvider;

        refreshTopBar();

        refreshLayersPane();

        showWindow();
    }

    private void initTopBar() {
    }

    private void initLayersPane() {
        layersChoiceBox.setConverter(new LayerToItemConverter());
        currentLayerProperty.bind(layersChoiceBox.getSelectionModel().selectedItemProperty());

        layerFlagsLabel.textProperty().bindBidirectional(currentLayerProperty, new LayerFlagsToLabelConverter());
        layerTextureNameLabel.textProperty().bindBidirectional(currentLayerProperty, new LayerTextureToLabelConverter());
    }

    private void initStatusBar() {
    }

    private void refreshTopBar() {
        titleLabel.textProperty().unbindBidirectional(materialProperty);
        titleLabel.textProperty().bindBidirectional(materialProperty, new MaterialToAdvancedTitleConverter(fullNameProvider));
    }

    private void refreshLayersPane() {
        fillLayers();

        infoAccordion.setExpandedPane(shaderInfoPane);
    }

    private void fillLayers() {
        final ObservableList<Layer> items = layersChoiceBox.getItems();
        items.clear();
        LayerGroup layerGroup = materialProperty.getValue().getLayerGroup();
        items.addAll(layerGroup.getLayers().subList(0, layerGroup.getLayerCount() - 1));

        if (!items.isEmpty()) {
            layersChoiceBox.getSelectionModel().clearAndSelect(0);
        }
    }
}

package fr.tduf.gui.database.plugins.materials.controllers;

import fr.tduf.gui.database.controllers.AbstractEditorController;
import fr.tduf.gui.database.plugins.materials.converter.*;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Layer;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.LayerGroup;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialSubSetting;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.function.Function;

import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.*;

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
    private Label reflectionLayerScaleLabel;

    @FXML
    private Label layerFlagsLabel;

    @FXML
    private Label layerTextureNameLabel;

    @FXML
    private TableView<MaterialSubSetting> subSettingsTableView;

    private final Property<Layer> currentLayerProperty = new SimpleObjectProperty<>();

    private final Property<Material> materialProperty = new SimpleObjectProperty<>();

    private Function<String, String> fullNameProvider;

    @Override
    public void init() {
        initTopBar();

        initShaderPane();

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

        refreshShaderPane();

        refreshLayersPane();

        showWindow();
    }

    private void initTopBar() {
    }

    private void initShaderPane() {
        reflectionLayerScaleLabel.textProperty().bindBidirectional(materialProperty, new MaterialToReflectionLayerScaleLabelConverter());

        TableColumn<MaterialSubSetting, Long> idColumn = new TableColumn<>(HEADER_SUBTABLE_ID);
        idColumn.setCellValueFactory((cellData) -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        idColumn.setSortType(TableColumn.SortType.ASCENDING);

        TableColumn<MaterialSubSetting, Long> value1Column = createSubSettingValueColumn(HEADER_SUBTABLE_VALUE1, (cellData) -> new SimpleObjectProperty<>(cellData.getValue().getValue1()));
        TableColumn<MaterialSubSetting, Long> value2Column = createSubSettingValueColumn(HEADER_SUBTABLE_VALUE2, (cellData) -> new SimpleObjectProperty<>(cellData.getValue().getValue2()));
        TableColumn<MaterialSubSetting, Long> value3Column = createSubSettingValueColumn(HEADER_SUBTABLE_VALUE3, (cellData) -> new SimpleObjectProperty<>(cellData.getValue().getValue3()));

        //noinspection unchecked
        subSettingsTableView.getColumns().addAll(
                idColumn,
                value1Column,
                value2Column,
                value3Column
        );

        subSettingsTableView.getSortOrder().add(idColumn);
    }

    private static TableColumn<MaterialSubSetting, Long> createSubSettingValueColumn(String header, Callback<TableColumn.CellDataFeatures<MaterialSubSetting, Long>, ObservableValue<Long>> observableValueCallback) {
        TableColumn<MaterialSubSetting, Long> value1Column = new TableColumn<>(header);
        value1Column.setCellValueFactory(observableValueCallback);
        return value1Column;
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

    private void refreshShaderPane() {
        fillSubSettings();

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

    private void fillSubSettings() {
        ObservableList<MaterialSubSetting> settings = FXCollections.observableArrayList(materialProperty.getValue().getProperties().getShaderParameters().getSubSettings());
        SortedList<MaterialSubSetting> settingsSorted = new SortedList<>(settings);
        settingsSorted.comparatorProperty().bind(subSettingsTableView.comparatorProperty());
        subSettingsTableView.setItems(settingsSorted);
    }
}

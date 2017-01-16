package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToItemConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToRawValueConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraViewToItemConverter;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo.CameraView.fromProps;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.TYPE;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Advanced cameras edition plugin
 */
// TODO externalize strings
public class CamerasPlugin implements DatabasePlugin {
    private static final String THIS_CLASS_NAME = CamerasPlugin.class.getSimpleName();

    /**
     * Required contextual information:
     * - databaseLocation
     * - camerasContext->allCameras
     * - camerasContext->camerasParser
     * @param context : all required information about Database Editor
     * @throws IOException when camars file can't be parsed for some reason
     */
    @Override
    public void onInit(EditorContext context) throws IOException {
        CamerasContext camerasContext = context.getCamerasContext();
        Property<CamerasParser> camerasParserProperty = camerasContext.getCamerasParserProperty();

        camerasContext.reset();

        String databaseLocation = context.getDatabaseLocation();
        Path cameraFile = resolveCameraFilePath(databaseLocation);
        if (!Files.exists(cameraFile)) {
            Log.warn(THIS_CLASS_NAME, "No cameras.bin file was found in database directory: " + databaseLocation);
            return;
        }

        camerasContext.setBinaryFileLocation(cameraFile.toString());
        Log.debug(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        CamerasParser camerasParser = CamerasHelper.loadAndParseFile(cameraFile.toString());
        camerasContext.setPluginLoaded(true);
        // Set to field?
        camerasParserProperty.setValue(camerasParser);
    }

    /**
     * Required contextual information:
     * - camerasContext->pluginLoaded
     * - camerasContext->binaryFileLocation
     * @param context : all required information about Database Editor
     */
    @Override
    public void onSave(EditorContext context) throws IOException {
        CamerasContext camerasContext = context.getCamerasContext();
        if (!camerasContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no saving will be performed");
            return;
        }

        String cameraFile = camerasContext.getBinaryFileLocation();
        Log.info(THIS_CLASS_NAME, "Saving camera info to " + cameraFile);
        CamerasHelper.saveFile(camerasContext.getCamerasParserProperty().getValue(), cameraFile);
    }

    /**
     * Required contextual information:
     * - rawValueProperty
     * - fieldRank
     * - currentTopic
     * - camerasContext->allCameras
     * - camerasContext->viewTypeProperty
     * - camerasContext->camerasParser
     * @param context : all required information about Database Editor
     */
    @Override
    public Node renderControls(EditorContext context) {
        HBox hBox = new HBox();
        ObservableList<Node> mainRowChildren = hBox.getChildren();
        // TODO extract to CSS
        hBox.setPadding(new Insets(5.0));

        CamerasContext camerasContext = context.getCamerasContext();
        if (!camerasContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no rendering will be performed");
            return hBox;
        }

        List<CameraInfo> allCamerasSorted = CamerasHelper.fetchAllInformation(camerasContext.getCamerasParserProperty().getValue()).stream()
                .sorted(comparingLong(CameraInfo::getCameraIdentifier))
                .collect(toList());

        // TODO create in sub methods
        ObservableList<CameraInfo> cameraItems = FXCollections.observableArrayList(allCamerasSorted);
        ObservableList<Map.Entry<ViewProps, ?>> allViewProps = FXCollections.observableArrayList();
        ObservableList<CameraInfo.CameraView> cameraViews = FXCollections.observableArrayList();

        VBox mainColumnBox = createMainColumn(context, cameraViews, cameraItems, camerasContext, allViewProps);
        VBox buttonColumnBox = createButtonColumn();

        mainRowChildren.add(mainColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));
        mainRowChildren.add(buttonColumnBox);

        return hBox;
    }

    private VBox createMainColumn(EditorContext context, ObservableList<CameraInfo.CameraView> cameraViews, ObservableList<CameraInfo> cameraItems, CamerasContext camerasContext, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        Property<CamerasParser> camerasParserProperty = camerasContext.getCamerasParserProperty();

        ComboBox<CameraInfo> cameraSelectorComboBox = new ComboBox<>(cameraItems);
        ComboBox<CameraInfo.CameraView> viewSelectorComboBox = new ComboBox<>(cameraViews);

        VBox mainColumnBox = new VBox();
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        // TODO extract to constants or CSS
        int mainColumWidth = 625;
        int comboWidth = 455;

        HBox camSelectorBox = createCamSelectorBox(context, cameraItems, camerasParserProperty, mainColumWidth, comboWidth, cameraSelectorComboBox, viewSelectorComboBox);
        HBox viewSelectorBox = createViewSelectorBox(mainColumWidth, comboWidth, viewSelectorComboBox, cameraSelectorComboBox.valueProperty(), allViewProps, camerasParserProperty);
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = createPropertiesTableView(context, allViewProps, viewSelectorComboBox.valueProperty(), camerasParserProperty, mainColumWidth);

        mainColumnChildren.add(camSelectorBox);
        mainColumnChildren.add(viewSelectorBox);
        mainColumnChildren.add(setPropertyTableView);
        return mainColumnBox;
    }

    private VBox createButtonColumn() {
        return new VBox();
//        buttonColumnBox.getChildren().add(new Button("+"));
//        buttonColumnBox.getChildren().add(new Button("-"));
//        buttonColumnBox.getChildren().add(new Button("C"));
//        buttonColumnBox.getChildren().add(new Button("P"));
    }

    private TableView<Map.Entry<ViewProps, ?>> createPropertiesTableView(EditorContext context, ObservableList<Map.Entry<ViewProps, ?>> viewProps, Property<CameraInfo.CameraView> currentViewProperty, Property<CamerasParser> camerasParserProperty, int mainColumWidth) {
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = new TableView<>(viewProps);
        setPropertyTableView.setMinSize(mainColumWidth,200);
        setPropertyTableView.setEditable(true);
        TableColumn<Map.Entry<ViewProps, ?>, String> settingColumn = new TableColumn<>("Setting");
        settingColumn.setMinWidth(175);
        settingColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getKey().name()));
        setPropertyTableView.getColumns().add(settingColumn);
        TableColumn<Map.Entry<ViewProps, ?>, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setMinWidth(100);
        valueColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getValue().toString()));
        valueColumn.setCellFactory(forTableColumn());
        valueColumn.setOnEditCommit(getCellEditEventHandler(camerasParserProperty, context.getRawValueProperty(), currentViewProperty));
        setPropertyTableView.getColumns().add(valueColumn);
        return setPropertyTableView;
    }

    private HBox createCamSelectorBox(EditorContext context, ObservableList<CameraInfo> cameraItems, Property<CamerasParser> camerasParserProperty, int mainColumWidth, int comboWidth, ComboBox<CameraInfo> cameraSelectorComboBox, ComboBox<CameraInfo.CameraView> viewSelectorComboBox) {
        HBox camSelectorBox = new HBox();
        camSelectorBox.setPrefWidth(mainColumWidth);
        cameraSelectorComboBox.setPrefWidth(comboWidth);
        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter());
        StringProperty rawValueProperty = context.getRawValueProperty();
        Bindings.bindBidirectional(
                rawValueProperty, cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraItems));
        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getCameraSelectorChangeListener(context.getFieldRank(), context.getCurrentTopic(), context.getChangeDataController(), camerasParserProperty, viewSelectorComboBox.valueProperty(), viewSelectorComboBox.itemsProperty().get()));
        Region camRegion = new Region();
        HBox.setHgrow(camRegion, ALWAYS);
        // TODO bold label
        camSelectorBox.getChildren().add(new Label("Available cameras:"));
        camSelectorBox.getChildren().add(camRegion);
        camSelectorBox.getChildren().add(cameraSelectorComboBox);
        return camSelectorBox;
    }

    private HBox createViewSelectorBox(int mainColumWidth, int comboWidth, ComboBox<CameraInfo.CameraView> viewSelectorComboBox, Property<CameraInfo> currentCameraSetProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps, Property<CamerasParser> camerasParserProperty) {
        HBox viewSelectorBox = new HBox();
        viewSelectorBox.setPrefWidth(mainColumWidth);
        viewSelectorComboBox.setPrefWidth(comboWidth);
        viewSelectorComboBox.setConverter(new CameraViewToItemConverter());
        viewSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getViewSelectorChangeListener(camerasParserProperty, currentCameraSetProperty, allViewProps));

        // TODO bold label
        viewSelectorBox.getChildren().add(new Label("Available views:"));
        Region viewRegion = new Region();
        HBox.setHgrow(viewRegion, ALWAYS);
        viewSelectorBox.getChildren().add(viewRegion);
        viewSelectorBox.getChildren().add(viewSelectorComboBox);
        return viewSelectorBox;
    }

    private ChangeListener<CameraInfo> getCameraSelectorChangeListener(int fieldRank, DbDto.Topic topic, MainStageChangeDataController changeDataController, Property<CamerasParser> camerasParserProperty, Property<CameraInfo.CameraView> currentCameraViewProperty, ObservableList<CameraInfo.CameraView> allCameraViews) {
        return (ObservableValue<? extends CameraInfo> observable, CameraInfo oldValue, CameraInfo newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allCameraViews.clear();

            if (newValue != null) {
                allCameraViews.addAll(
                        getSortedViews(camerasParserProperty, newValue));

                if (!allCameraViews.isEmpty()) {
                    currentCameraViewProperty.setValue(allCameraViews.get(0));
                }

                changeDataController.updateContentItem(topic, fieldRank, Long.toString(newValue.getCameraIdentifier()));
            }
        };
    }

    private ChangeListener<CameraInfo.CameraView> getViewSelectorChangeListener(Property<CamerasParser> currentParserProperty, Property<CameraInfo> currentCameraSetProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allViewProps.clear();

            if (newValue != null) {
                allViewProps.addAll(
                        getEditableViewProperties(currentCameraSetProperty.getValue().getCameraIdentifier(), newValue.getType(), currentParserProperty.getValue()));
            }
        };
    }

    private List<CameraInfo.CameraView> getSortedViews(Property<CamerasParser> camerasParserProperty, CameraInfo newValue) {
        return CamerasHelper.fetchInformation(newValue.getCameraIdentifier(), camerasParserProperty.getValue()).getViews().stream()
                .sorted(comparing(CameraInfo.CameraView::getType))
                .collect(toList());
    }

    private List<Map.Entry<ViewProps, ?>> getEditableViewProperties(long cameraIdentifier, ViewKind viewKind, CamerasParser camerasParser) {
        final Set<ViewProps> nonEditableProps = new HashSet<>(singletonList(TYPE));

        // TODO extract view lookup to helper
        return CamerasHelper.fetchInformation(cameraIdentifier, camerasParser).getViews().stream()
                .filter(cv -> viewKind == cv.getType())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No view available: " + viewKind))
                .getSettings().entrySet().stream()
                .filter(propsEntry -> !nonEditableProps.contains(propsEntry.getKey()))
                .sorted(comparing(Map.Entry::getKey))
                .collect(toList());
    }

    private EventHandler<TableColumn.CellEditEvent<Map.Entry<ViewProps, ?>, String>> getCellEditEventHandler(Property<CamerasParser> camerasParserProperty, Property<String> rawValueProperty, Property<CameraInfo.CameraView> currentViewProperty) {
        return cellEditEvent -> {
            String newValue = cellEditEvent.getNewValue();
            Map.Entry<ViewProps, ?> editedRowValue = cellEditEvent.getRowValue();
            Log.debug(THIS_CLASS_NAME, "Edited prop: " + editedRowValue + ", old=" + cellEditEvent.getOldValue() + ", new=" + newValue);

            //noinspection unchecked
            Map.Entry<ViewProps, Object> editedEntry = (Map.Entry<ViewProps, Object>) editedRowValue;
            editedEntry.setValue(validateCellInput(newValue));

            ViewKind currentViewKind = currentViewProperty.getValue().getType();
            long cameraIdentifier = Long.valueOf(rawValueProperty.getValue());
            updateViewPropertiesInParser(cameraIdentifier, currentViewKind, editedEntry, camerasParserProperty.getValue());
        };
    }

    private CameraInfo updateViewPropertiesInParser(long cameraIdentifier, ViewKind currentViewKind, Map.Entry<ViewProps, Object> editedProp, CamerasParser camerasParser) {
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(TYPE, currentViewKind);
        viewProps.put(editedProp.getKey(), editedProp.getValue());

        CameraInfo updatedConfiguration = CameraInfo.builder()
                .forIdentifier(cameraIdentifier)
                .addView(fromProps(viewProps))
                .build();

        Log.debug(THIS_CLASS_NAME, "Will update camera: " + cameraIdentifier);

        return CamerasHelper.updateViews(updatedConfiguration, camerasParser);
    }

    private int validateCellInput(String value) {
        try {
            return Integer.valueOf(value);
        } catch(NumberFormatException nfe) {
            Log.error(THIS_CLASS_NAME, "Unsupported value was entered: " + value);
            throw nfe;
        }
    }

    private Path resolveCameraFilePath(String databaseLocation) {
        return Paths.get(databaseLocation, "cameras.bin");
    }
}

package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToItemConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToRawValueConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraViewToItemConverter;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
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
    public void onInit(PluginContext context) throws IOException {
        CamerasContext camerasContext = context.getCamerasContext();
        List<CameraInfo> allCameras = camerasContext.getAllCameras();
        allCameras.clear();

        String databaseLocation = context.getDatabaseLocation();
        Path cameraFile = resolveCameraFilePath(databaseLocation);
        if (!Files.exists(cameraFile)) {
            Log.warn(THIS_CLASS_NAME, "No cameras.bin file was found in database directory: " + databaseLocation);
            camerasContext.setPluginLoaded(false);
            return;
        }

        camerasContext.setBinaryFileLocation(cameraFile.toString());
        Log.debug(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        CamerasParser camerasParser = CamerasHelper.loadAndParseFile(cameraFile.toString());
        camerasContext.setPluginLoaded(true);
        camerasContext.setCamerasParser(camerasParser);

        allCameras.addAll(CamerasHelper.fetchAllInformation(camerasParser));
        Log.debug(THIS_CLASS_NAME, "Loaded sets count: " + allCameras.size());
    }

    /**
     * Required contextual information:
     * - camerasContext->pluginLoaded
     * - camerasContext->binaryFileLocation
     * @param context : all required information about Database Editor
     */
    @Override
    public void onSave(PluginContext context) throws IOException {
        CamerasContext camerasContext = context.getCamerasContext();
        if (!camerasContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no saving will be performed");
            return;
        }

        String cameraFile = camerasContext.getBinaryFileLocation();
        Log.info(THIS_CLASS_NAME, "Saving camera info to " + cameraFile);
        CamerasHelper.saveFile(camerasContext.getCamerasParser(), cameraFile);
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
    public Node renderControls(PluginContext context) {
        // TODO extract methods

        HBox hBox = new HBox();
        CamerasContext camerasContext = context.getCamerasContext();
        if (!camerasContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no rendering will be performed");
            return hBox;
        }

        ObservableList<Map.Entry<ViewProps, ?>> viewProps = FXCollections.observableArrayList();
        ObservableList<CameraInfo.CameraView> cameraViews = FXCollections.observableArrayList();
        ObservableList<CameraInfo> cameraItems = FXCollections.observableArrayList(camerasContext.getAllCameras().stream()
                .sorted(comparingLong(CameraInfo::getCameraIdentifier))
                .collect(toList()));

        Property<ViewKind> currentViewTypeProperty = camerasContext.getViewTypeProperty();
        CamerasParser camerasParser = camerasContext.getCamerasParser();

        hBox.setPadding(new Insets(5.0));

        VBox mainColumnBox = new VBox();
        int mainColumWidth = 625;
        int comboWidth = 455;

        HBox viewSelectorBox = new HBox();
        viewSelectorBox.setPrefWidth(mainColumWidth);
        ComboBox<CameraInfo.CameraView> viewSelectorComboBox = new ComboBox<>(cameraViews);
        viewSelectorComboBox.setPrefWidth(comboWidth);
        viewSelectorComboBox.setConverter(new CameraViewToItemConverter());
        viewSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getViewSelectorChangeListener(currentViewTypeProperty, viewProps));
        viewSelectorBox.getChildren().add(new Label("Available views:"));
        Region viewRegion = new Region();
        HBox.setHgrow(viewRegion, ALWAYS);
        viewSelectorBox.getChildren().add(viewRegion);
        viewSelectorBox.getChildren().add(viewSelectorComboBox);

        HBox camSelectorBox = new HBox();
        camSelectorBox.setPrefWidth(mainColumWidth);
        ComboBox<CameraInfo> cameraSelectorComboBox = new ComboBox<>(cameraItems);
        cameraSelectorComboBox.setPrefWidth(comboWidth);
        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter());
        StringProperty rawValueProperty = context.getRawValueProperty();
        Bindings.bindBidirectional(
                rawValueProperty, cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraItems));
        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getCameraSelectorChangeListener(context.getFieldRank(), rawValueProperty, context.getCurrentTopic(), cameraViews, viewSelectorComboBox, context.getMainStageController().getChangeData(), camerasParser));
        Region camRegion = new Region();
        HBox.setHgrow(camRegion, ALWAYS);
        camSelectorBox.getChildren().add(new Label("Available cameras:"));
        camSelectorBox.getChildren().add(camRegion);
        camSelectorBox.getChildren().add(cameraSelectorComboBox);

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
        valueColumn.setOnEditCommit(getCellEditEventHandler(camerasParser, context.getRawValueProperty(), currentViewTypeProperty, cameraViews));
        setPropertyTableView.getColumns().add(valueColumn);

        mainColumnBox.getChildren().add(camSelectorBox);
        mainColumnBox.getChildren().add(viewSelectorBox);
        mainColumnBox.getChildren().add(setPropertyTableView);

        VBox buttonColumnBox = new VBox();
//        buttonColumnBox.getChildren().add(new Button("+"));
//        buttonColumnBox.getChildren().add(new Button("-"));
//        buttonColumnBox.getChildren().add(new Button("C"));
//        buttonColumnBox.getChildren().add(new Button("P"));

        hBox.getChildren().add(mainColumnBox);
        hBox.getChildren().add(new Separator(VERTICAL));
        hBox.getChildren().add(buttonColumnBox);

        return hBox;
    }

    private ChangeListener<CameraInfo> getCameraSelectorChangeListener(int fieldRank, StringProperty rawValueProperty, DbDto.Topic topic, ObservableList<CameraInfo.CameraView> cameraViews, ComboBox<CameraInfo.CameraView> viewSelectorComboBox, MainStageChangeDataController changeDataController, CamerasParser camerasParser) {
        return (ObservableValue<? extends CameraInfo> observable, CameraInfo oldValue, CameraInfo newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            cameraViews.clear();

            String cameraIdAsString = rawValueProperty.get();
            if (newValue == null) {
                Log.warn(THIS_CLASS_NAME, "No camera for identifier: " + cameraIdAsString);
                return;
            }

            cameraViews.addAll(
                    CamerasHelper.fetchInformation(Long.valueOf(cameraIdAsString), camerasParser).getViews().stream()
                            .sorted(comparing(CameraInfo.CameraView::getType))
                            .collect(toList())
            );

            if (!cameraViews.isEmpty()) {
                viewSelectorComboBox.valueProperty().setValue(cameraViews.get(0));
            }

            changeDataController.updateContentItem(topic, fieldRank, cameraIdAsString);
        };
    }

    private ChangeListener<CameraInfo.CameraView> getViewSelectorChangeListener(Property<ViewKind> currentViewTypeProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allViewProps.clear();

            if (newValue == null) {
                currentViewTypeProperty.setValue(null);
                return;
            }
            allViewProps.addAll(getEditableProps(newValue));
            currentViewTypeProperty.setValue(newValue.getType());
        };
    }

    private List<Map.Entry<ViewProps, ?>> getEditableProps(CameraInfo.CameraView newValue) {
        final Set<ViewProps> nonEditableProps = new HashSet<>(singletonList(TYPE));

        return newValue.getSettings().entrySet().stream()
                .filter(propsEntry -> !nonEditableProps.contains(propsEntry.getKey()))
                .sorted(comparing(Map.Entry::getKey))
                .collect(toList());
    }

    private EventHandler<TableColumn.CellEditEvent<Map.Entry<ViewProps, ?>, String>> getCellEditEventHandler(CamerasParser camerasParser, StringProperty rawValueProperty, Property<ViewKind> currentViewTypeProperty, ObservableList<CameraInfo.CameraView> cameraViews) {
        return cellEditEvent -> {
            // TODO simplify

            //noinspection unchecked
            Map.Entry<ViewProps, Object> editedEntry = (Map.Entry<ViewProps, Object>) cellEditEvent.getRowValue();
            String newValue = cellEditEvent.getNewValue();
            editedEntry.setValue(validateCellInput(newValue));

            Log.debug(THIS_CLASS_NAME, "Edited prop: " + editedEntry.getKey() + ", old=" + cellEditEvent.getOldValue() + ", new=" + newValue);

            EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
            ViewKind currentViewKind = currentViewTypeProperty.getValue();
            viewProps.put(TYPE, currentViewKind);
            viewProps.put(editedEntry.getKey(), editedEntry.getValue());

            long cameraIdentifier = Long.valueOf(rawValueProperty.get());
            CameraInfo updatedConfiguration = CameraInfo.builder()
                    .forIdentifier(cameraIdentifier)
                    .addView(fromProps(viewProps))
                    .build();

            Log.debug(THIS_CLASS_NAME, "Will update camera: " + cameraIdentifier);

            // FIXME does not seem to work...
            CameraInfo updatedCameraInfo = CamerasHelper.updateViews(updatedConfiguration, camerasParser);

            int replacedIndex = cameraViews.stream()
                    .filter(cv -> currentViewKind == cv.getType())
                    .findAny()
                    .map(cameraViews::indexOf)
                    .orElseThrow(() -> new IllegalStateException("Replaced view not found for camera id: " + cameraIdentifier + " : " + currentViewKind));
            CameraInfo.CameraView updatedView = updatedCameraInfo.getViews().stream()
                    .filter(cv -> currentViewKind == cv.getType())
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("View not found for camera id: " + cameraIdentifier + " : " + currentViewKind));
            cameraViews.set(replacedIndex, updatedView);
        };
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

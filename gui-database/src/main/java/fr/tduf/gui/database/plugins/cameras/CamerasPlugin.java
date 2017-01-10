package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToItemConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToRawValueConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraViewToItemConverter;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.TYPE;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Advanced cameras edition plugin
 */
public class CamerasPlugin implements DatabasePlugin {
private static final String THIS_CLASS_NAME = CamerasPlugin.class.getSimpleName();
    /**
     * Required contextual information:
     * - databaseLocation
     * - camerasContext->allCameras
     * @param context : all required information about Database Editor
     * @throws IOException when camars file can't be parsed for some reason
     */
    @Override
    public void onInit(PluginContext context) throws IOException {
        CamerasContext camerasContext = context.getCamerasContext();
        List<CameraInfo> allCameras = camerasContext.getAllCameras();
        allCameras.clear();

        String databaseLocation = context.getDatabaseLocation();
        Path cameraFile = Paths.get(databaseLocation, "cameras.bin");
        if (!Files.exists(cameraFile)) {
            Log.warn(THIS_CLASS_NAME, "No cameras.bin file was found in database directory: " + databaseLocation);
            camerasContext.setPluginLoaded(false);
            return;
        }

        camerasContext.setBinaryFileLocation(cameraFile.toString());
        Log.debug(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        CamerasParser camerasParser = CamerasHelper.loadAndParseFile(cameraFile.toString());
        camerasContext.setPluginLoaded(true);

        //noinspection ResultOfMethodCallIgnored
        camerasParser.getCameraViews().entrySet().stream()
                .map(Map.Entry::getKey)
                .map(cameraId -> CamerasHelper.fetchInformation(cameraId, camerasParser))
                .collect(toCollection(() -> allCameras));

        Log.debug(THIS_CLASS_NAME, "Loaded sets count: " + allCameras.size());
    }

    /**
     * Required contextual information:
     * - rawValueProperty
     * - camerasContext->allCameras
     * @param context : all required information about Database Editor
     */
    @Override
    public Node renderControls(PluginContext context) {
        HBox hBox = new HBox();
        if (!context.getCamerasContext().isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "No cameras were loaded");
            return hBox;
        }

        ObservableList<Map.Entry<ViewProps, ?>> viewProps = FXCollections.observableArrayList();
        ObservableList<CameraInfo.CameraView> cameraViews = FXCollections.observableArrayList();
        ObservableList<CameraInfo> cameraItems = FXCollections.observableArrayList(context.getCamerasContext().getAllCameras().stream()
                .sorted(comparingLong(CameraInfo::getCameraIdentifier))
                .collect(toList()));

        hBox.setPadding(new Insets(5.0));

        VBox mainColumnBox = new VBox();
        int columWidth = 445;
        int comboWidth = 275;

        HBox camSelectorBox = new HBox();
        camSelectorBox.setPrefWidth(columWidth);
        ComboBox<CameraInfo> cameraSelectorComboBox = new ComboBox<>(cameraItems);
        cameraSelectorComboBox.setPrefWidth(comboWidth);
        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter());
        StringProperty rawValueProperty = context.getRawValueProperty();
        Bindings.bindBidirectional(
                rawValueProperty, cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraItems));
        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getCameraSelectorChangeListener(context.getFieldRank(), rawValueProperty, context.getCurrentTopic(), cameraViews, context.getMainStageController().getChangeData()));
        Region camRegion = new Region();
        HBox.setHgrow(camRegion, ALWAYS);
        camSelectorBox.getChildren().add(new Label("Available cameras:"));
        camSelectorBox.getChildren().add(camRegion);
        camSelectorBox.getChildren().add(cameraSelectorComboBox);

        HBox viewSelectorBox = new HBox();
        viewSelectorBox.setPrefWidth(columWidth);
        ComboBox<CameraInfo.CameraView> viewSelectorComboBox = new ComboBox<>(cameraViews);
        viewSelectorComboBox.setPrefWidth(comboWidth);
        viewSelectorComboBox.setConverter(new CameraViewToItemConverter());
        viewSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getViewSelectorChangeListener(viewProps));
        viewSelectorBox.getChildren().add(new Label("Available views:"));
        Region viewRegion = new Region();
        HBox.setHgrow(viewRegion, ALWAYS);
        viewSelectorBox.getChildren().add(viewRegion);
        viewSelectorBox.getChildren().add(viewSelectorComboBox);

        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = new TableView<>(viewProps);
        setPropertyTableView.setMinSize(columWidth,200);
        TableColumn<Map.Entry<ViewProps, ?>, Object> settingColumn = new TableColumn<>("Setting");
        settingColumn.setMinWidth(175);
        settingColumn.setCellValueFactory((cellData) -> (ObservableValue) new SimpleStringProperty(cellData.getValue().getKey().name()));
        setPropertyTableView.getColumns().add(settingColumn);
        TableColumn<Map.Entry<ViewProps, ?>, Object> valueColumn = new TableColumn<>("Value");
        valueColumn.setMinWidth(100);
        valueColumn.setCellValueFactory((cellData) -> (ObservableValue) new SimpleStringProperty(cellData.getValue().getValue().toString()));
        setPropertyTableView.getColumns().add(valueColumn);

        mainColumnBox.getChildren().add(camSelectorBox);
        mainColumnBox.getChildren().add(viewSelectorBox);
        mainColumnBox.getChildren().add(setPropertyTableView);

        VBox buttonColumnBox = new VBox();
        buttonColumnBox.getChildren().add(new Button("Apply"));
        buttonColumnBox.getChildren().add(new Button("Copy"));
        buttonColumnBox.getChildren().add(new Button("Paste"));

        hBox.getChildren().add(mainColumnBox);
        hBox.getChildren().add(new Separator(Orientation.VERTICAL));
        hBox.getChildren().add(buttonColumnBox);

        return hBox;
    }

    private ChangeListener<CameraInfo> getCameraSelectorChangeListener(int fieldRank, StringProperty rawValueProperty, DbDto.Topic topic, ObservableList<CameraInfo.CameraView> cameraViews, MainStageChangeDataController changeDataController) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            changeDataController.updateContentItem(topic, fieldRank, rawValueProperty.get());

            cameraViews.clear();
            cameraViews.addAll(newValue.getViews().stream()
                    .sorted(Comparator.comparingInt(v -> v.getType().getInternalId()))
                    .collect(toList()));
        };
    }

    private ChangeListener<CameraInfo.CameraView> getViewSelectorChangeListener(ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allViewProps.clear();

            if (newValue == null) {
                return;
            }
            allViewProps.addAll(getEditableProps(newValue));
        };
    }

    private List<Map.Entry<ViewProps, ?>> getEditableProps(CameraInfo.CameraView newValue) {
        final Set<ViewProps> nonEditableProps = new HashSet<>(singletonList(TYPE));

        return newValue.getSettings().entrySet().stream()
                .filter(propsEntry -> !nonEditableProps.contains(propsEntry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(toList());
    }
}

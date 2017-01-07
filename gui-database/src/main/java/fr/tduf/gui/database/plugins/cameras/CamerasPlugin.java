package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_FORMAT_CAMERA_ITEM;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

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

        // TODO what to do when JSON directory used ? ask for specific file?
        String cameraFile = Paths.get(context.getDatabaseLocation(), "cameras.bin").toString();
        context.getCamerasContext().setBinaryFileLocation(cameraFile);

        // TODO load via service
        Log.debug(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        CamerasParser camerasParser = CamerasHelper.loadAndParseFile(cameraFile);

        List<CameraInfo> allCameras = context.getCamerasContext().getAllCameras();
        allCameras.clear();

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

        VBox mainColumnBox = new VBox();

        HBox camSelectorBox = new HBox();
        ObservableList<CameraInfo> cameraItems = FXCollections.observableArrayList(context.getCamerasContext().getAllCameras().stream()
                .sorted(comparingLong(CameraInfo::getCameraIdentifier))
                .collect(toList()));
        ComboBox<CameraInfo> cameraSelectorComboBox = new ComboBox<>(cameraItems);
        cameraSelectorComboBox.setConverter(getCameraInfoToItemConverter());
        Property<String> rawValueProperty = context.getRawValueProperty();
        Bindings.bindBidirectional(
                rawValueProperty, cameraSelectorComboBox.valueProperty(), getCameraInfoToRawValueConverter(cameraItems));
        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(getCameraSelectorChangeListener());
        camSelectorBox.getChildren().add(new Label("Available cameras:"));
        camSelectorBox.getChildren().add(cameraSelectorComboBox);
        mainColumnBox.getChildren().add(camSelectorBox);

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

    // TODO extract converter to own class
    private StringConverter<CameraInfo> getCameraInfoToItemConverter() {
        return new StringConverter<CameraInfo>() {
            @Override
            public String toString(CameraInfo cameraInfo) {
                // TODO add metadata
                return String.format(LABEL_FORMAT_CAMERA_ITEM, cameraInfo.getCameraIdentifier(), cameraInfo.getViews().size());
            }

            @Override
            public CameraInfo fromString(String cameraIdentifierAsString) {
                return null;
            }
        };
    }

    // TODO extract converter to own class
    private StringConverter<CameraInfo> getCameraInfoToRawValueConverter(ObservableList<CameraInfo> allCameras) {
        return new StringConverter<CameraInfo>() {
            @Override
            public String toString(CameraInfo cameraInfo) {
                if (cameraInfo == null) {
                    return DisplayConstants.LABEL_CAMERA_RAW_VALUE_DEFAULT;
                }
                return Long.toString(cameraInfo.getCameraIdentifier());
            }

            @Override
            public CameraInfo fromString(String cameraIdentifierAsString) {
                // TODO handle illegal format
                long cameraId = Long.valueOf(cameraIdentifierAsString);
                return allCameras.stream()
                        .filter(camera -> camera.getCameraIdentifier() == cameraId)
                        .findAny()
                        .orElse(null);
            }
        };
    }

    private ChangeListener<CameraInfo> getCameraSelectorChangeListener() {
        return (observable, oldValue, newValue) -> {
            // TODO update views
        };
    }
}

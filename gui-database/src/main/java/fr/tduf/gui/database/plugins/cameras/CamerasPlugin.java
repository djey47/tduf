package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginContext;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toCollection;

/**
 * Advanced cameras edition plugin
 */
public class CamerasPlugin implements DatabasePlugin {
private static final String THIS_CLASS_NAME = CamerasPlugin.class.getSimpleName();
    /**
     * Required conetxtual information:
     * - databaseLocation
     * @param context : all required information about Database Editor
     * @throws IOException
     */
    @Override
    public void onInit(PluginContext context) throws IOException {

        // TODO what to do when JSON directory used ? ask for specific file?
        String cameraFile = Paths.get(context.getDatabaseLocation(), "cameras.bin").toString();
        context.getCamerasContext().setBinaryFileLocation(cameraFile);

        // TODO load via service
        Log.info(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        CamerasParser camerasParser = CamerasHelper.loadAndParseFile(cameraFile);

        List<CameraInfo> allCameras = context.getCamerasContext().getAllCameras();
        allCameras.clear();

        camerasParser.getCameraViews().entrySet().stream()
                .map(Map.Entry::getKey)
                .map(cameraId -> CamerasHelper.fetchInformation(cameraId, camerasParser))
                .collect(toCollection(() -> allCameras));

        Log.info(THIS_CLASS_NAME, "Loaded sets count: " + allCameras.size());
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

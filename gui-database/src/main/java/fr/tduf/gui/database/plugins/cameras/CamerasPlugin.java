package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.gui.database.plugins.cameras.common.FxConstants;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToItemConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToRawValueConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraViewToItemConverter;
import fr.tduf.gui.database.plugins.cameras.helper.CamerasDialogsHelper;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.libunlimited.high.files.db.common.helper.CameraAndIKHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.cameras.common.FxConstants.*;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_ITEM_LABEL;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_PLUGIN_BOX;
import static fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto.TopicMetadataDto.FIELD_RANK_CAMERA;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo.CameraView.fromProps;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.TYPE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
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
public class CamerasPlugin implements DatabasePlugin {
    private static final Class<CamerasPlugin> thisClass = CamerasPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private CameraAndIKHelper cameraRefHelper;
    private CamerasDialogsHelper dialogsHelper;

    private final Property<CamerasParser> camerasParserProperty = new SimpleObjectProperty<>();

    /**
     * Required contextual information:
     * - databaseLocation
     * - camerasContext->allCameras
     * @param context : all required information about Database Editor
     * @throws IOException when cameras file can't be parsed for some reason
     */
    @Override
    public void onInit(EditorContext context) throws IOException {
        CamerasContext camerasContext = context.getCamerasContext();
        camerasContext.reset();

        camerasParserProperty.setValue(null);

        String databaseLocation = context.getDatabaseLocation();
        Path cameraFile = resolveCameraFilePath(databaseLocation);
        if (!Files.exists(cameraFile)) {
            Log.warn(THIS_CLASS_NAME, "No cameras.bin file was found in database directory: " + databaseLocation);
            return;
        }

        camerasContext.setBinaryFileLocation(cameraFile.toString());
        Log.info(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        camerasContext.setPluginLoaded(true);

        CamerasParser camerasParser = CamerasHelper.loadAndParseFile(cameraFile.toString());
        camerasParserProperty.setValue(camerasParser);

        cameraRefHelper = new CameraAndIKHelper();
        Log.info(THIS_CLASS_NAME, "Camera reference loaded");

        dialogsHelper = new CamerasDialogsHelper();
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
        CamerasHelper.saveFile(camerasParserProperty.getValue(), cameraFile);
    }

    /**
     * Required contextual information:
     * - rawValueProperty
     * - camerasContext->allCameras
     * - camerasContext->viewTypeProperty
     * @param context : all required information about Database Editor
     */
    @Override
    public Node renderControls(EditorContext context) {
        CamerasContext camerasContext = context.getCamerasContext();
        // TODO see to set them automatically (add get set to interface?)
        camerasContext.setErrorProperty(context.getErrorProperty());
        camerasContext.setErrorMessageProperty(context.getErrorMessageProperty());

        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        if (!context.getCamerasContext().isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no rendering will be performed");
            return hBox;
        }

        // TODO convert to field
        ObservableList<CameraInfo> cameraItems = FXCollections.observableArrayList(getSortedCameraSets());
        // TODO sort with sorted method of observable list
        ComboBox<CameraInfo> cameraSelectorComboBox = new ComboBox<>(cameraItems);
        VBox mainColumnBox = createMainColumn(context, cameraItems, cameraSelectorComboBox);

        StringProperty rawValueProperty = context.getRawValueProperty();
        VBox buttonColumnBox = createButtonColumn(
                handleAddSetButtonAction(rawValueProperty, cameraSelectorComboBox),
                handleImportSetButtonAction());

        ObservableList<Node> mainRowChildren = hBox.getChildren();
        mainRowChildren.add(mainColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));
        mainRowChildren.add(buttonColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));

        return hBox;
    }

    @Override
    public Set<String> getCss() {
        return new HashSet<>(singletonList(thisClass.getResource(PATH_RESOURCE_CSS_CAMERAS).toExternalForm()));
    }

    private VBox createMainColumn(EditorContext context, ObservableList<CameraInfo> cameraItems, ComboBox<CameraInfo> cameraSelectorComboBox) {
        ObservableList<Map.Entry<ViewProps, ?>> allViewProps = FXCollections.observableArrayList();

        cameraSelectorComboBox.getStyleClass().add(CSS_CLASS_CAM_SELECTOR_COMBOBOX);
        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter(cameraRefHelper));

        ComboBox<CameraInfo.CameraView> viewSelectorComboBox = new ComboBox<>(FXCollections.observableArrayList());
        viewSelectorComboBox.getStyleClass().add(CSS_CLASS_VIEW_SELECTOR_COMBOBOX);
        viewSelectorComboBox.setConverter(new CameraViewToItemConverter());

        VBox mainColumnBox = new VBox();
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox camSelectorBox = createCamSelectorBox(context, cameraItems, cameraSelectorComboBox, viewSelectorComboBox);
        HBox viewSelectorBox = createViewSelectorBox(viewSelectorComboBox, cameraSelectorComboBox.valueProperty(), allViewProps);
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = createPropertiesTableView(context, allViewProps, viewSelectorComboBox.valueProperty());

        mainColumnChildren.add(camSelectorBox);
        mainColumnChildren.add(viewSelectorBox);
        mainColumnChildren.add(setPropertyTableView);
        return mainColumnBox;
    }

    private VBox createButtonColumn(EventHandler<ActionEvent> onAddSetAction, EventHandler<ActionEvent> onImportSetAction) {
        // TODO set to common stylesheet
        VBox buttonColumnBox = new VBox(5.0);

        Button addSetButton = new Button(LABEL_ADD_BUTTON);
        // TODO use stylesheet
        addSetButton.setPrefWidth(80.0);
        ControlHelper.setTooltipText(addSetButton, TOOLTIP_ADD_BUTTON);
        addSetButton.setOnAction(onAddSetAction);

        Button importSetButton = new Button(LABEL_IMPORT_SET_BUTTON);
        // TODO use stylesheet
        importSetButton.setPrefWidth(80.0);
        ControlHelper.setTooltipText(importSetButton, DisplayConstants.TOOLTIP_IMPORT_SET_BUTTON);
        importSetButton.setOnAction(onImportSetAction);

        ObservableList<Node> children = buttonColumnBox.getChildren();
        children.add(addSetButton);
        children.add(importSetButton);

        return buttonColumnBox;
    }

    private TableView<Map.Entry<ViewProps, ?>> createPropertiesTableView(EditorContext context, ObservableList<Map.Entry<ViewProps, ?>> viewProps, Property<CameraInfo.CameraView> currentViewProperty) {
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = new TableView<>(viewProps);
        setPropertyTableView.getStyleClass().add(CSS_CLASS_SET_PROPERTY_TABLEVIEW);
        setPropertyTableView.setEditable(true);

        TableColumn<Map.Entry<ViewProps, ?>, String> settingColumn = new TableColumn<>(HEADER_PROPTABLE_SETTING);
        settingColumn.getStyleClass().add(CSS_CLASS_SETTING_TABLECOLUMN);
        settingColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getKey().name()));

        TableColumn<Map.Entry<ViewProps, ?>, String> descriptionColumn = new TableColumn<>(HEADER_PROPTABLE_DESCRIPTION);
        descriptionColumn.getStyleClass().add(CSS_CLASS_DESCRIPTION_TABLECOLUMN);
        descriptionColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getKey().getDescription()));

        TableColumn<Map.Entry<ViewProps, ?>, String> valueColumn = new TableColumn<>(HEADER_PROPTABLE_VALUE);
        valueColumn.getStyleClass().add(CSS_CLASS_VALUE_TABLECOLUMN);
        valueColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getValue().toString()));
        valueColumn.setCellFactory(forTableColumn());
        valueColumn.setOnEditCommit(getCellEditEventHandler(context.getRawValueProperty(), currentViewProperty));

        setPropertyTableView.getColumns().add(settingColumn);
        setPropertyTableView.getColumns().add(descriptionColumn);
        setPropertyTableView.getColumns().add(valueColumn);

        return setPropertyTableView;
    }

    private HBox createCamSelectorBox(EditorContext context, ObservableList<CameraInfo> cameraItems, ComboBox<CameraInfo> cameraSelectorComboBox, ComboBox<CameraInfo.CameraView> viewSelectorComboBox) {
        HBox camSelectorBox = new HBox();
        camSelectorBox.getStyleClass().add(CSS_CLASS_CAM_SELECTOR_BOX);

        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getCameraSelectorChangeListener(context, viewSelectorComboBox.valueProperty(), viewSelectorComboBox.itemsProperty().get()));
        Bindings.bindBidirectional(
                context.getRawValueProperty(), cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraItems));

        Label availableCamerasLabel = new Label(LABEL_AVAILABLE_CAMERAS);
        availableCamerasLabel.setLabelFor(cameraSelectorComboBox);
        availableCamerasLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        Region camRegion = new Region();
        HBox.setHgrow(camRegion, ALWAYS);

        camSelectorBox.getChildren().add(availableCamerasLabel);
        camSelectorBox.getChildren().add(camRegion);
        camSelectorBox.getChildren().add(cameraSelectorComboBox);

        return camSelectorBox;
    }

    private HBox createViewSelectorBox(ComboBox<CameraInfo.CameraView> viewSelectorComboBox, Property<CameraInfo> currentCameraSetProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        HBox viewSelectorBox = new HBox();
        viewSelectorBox.getStyleClass().add(FxConstants.CSS_CLASS_VIEW_SELECTOR_BOX);

        viewSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getViewSelectorChangeListener(currentCameraSetProperty, allViewProps));

        Label availableViewsLabel = new Label(LABEL_AVAILABLE_VIEWS);
        availableViewsLabel.setLabelFor(viewSelectorComboBox);
        availableViewsLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        Region viewRegion = new Region();
        HBox.setHgrow(viewRegion, ALWAYS);

        viewSelectorBox.getChildren().add(availableViewsLabel);
        viewSelectorBox.getChildren().add(viewRegion);
        viewSelectorBox.getChildren().add(viewSelectorComboBox);

        return viewSelectorBox;
    }

    private ChangeListener<CameraInfo> getCameraSelectorChangeListener(EditorContext context, Property<CameraInfo.CameraView> currentCameraViewProperty, ObservableList<CameraInfo.CameraView> allCameraViews) {
        return (ObservableValue<? extends CameraInfo> observable, CameraInfo oldValue, CameraInfo newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allCameraViews.clear();

            CamerasContext camerasContext = context.getCamerasContext();
            if (newValue == null) {
                camerasContext.getErrorProperty().setValue(true);
                camerasContext.getErrorMessageProperty().setValue(LABEL_ERROR_TOOLTIP);
            } else {
                camerasContext.getErrorProperty().setValue(false);
                camerasContext.getErrorMessageProperty().setValue("");

                allCameraViews.addAll(getSortedViews(newValue));

                if (!allCameraViews.isEmpty()) {
                    currentCameraViewProperty.setValue(allCameraViews.get(0));
                }

                context.getChangeDataController().updateContentItem(CAR_PHYSICS_DATA, FIELD_RANK_CAMERA, Long.toString(newValue.getCameraIdentifier()));
            }
        };
    }

    private ChangeListener<CameraInfo.CameraView> getViewSelectorChangeListener(Property<CameraInfo> currentCameraSetProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allViewProps.clear();

            if (newValue != null) {
                allViewProps.addAll(
                        getSortedAndEditableViewProperties(currentCameraSetProperty.getValue().getCameraIdentifier(), newValue.getType()));
            }
        };
    }

    private List<CameraInfo> getSortedCameraSets() {
        return CamerasHelper.fetchAllInformation(camerasParserProperty.getValue()).stream()
                .sorted(comparingLong(CameraInfo::getCameraIdentifier))
                .collect(toList());
    }

    private List<CameraInfo.CameraView> getSortedViews(CameraInfo cameraInfo) {
        return CamerasHelper.fetchInformation(cameraInfo.getCameraIdentifier(), camerasParserProperty.getValue()).getViews().stream()
                .sorted(comparing(CameraInfo.CameraView::getType))
                .collect(toList());
    }

    private List<Map.Entry<ViewProps, ?>> getSortedAndEditableViewProperties(long cameraIdentifier, ViewKind viewKind) {
        final Set<ViewProps> nonEditableProps = new HashSet<>(singletonList(TYPE));

        return CamerasHelper.fetchViewProperties(cameraIdentifier, viewKind, camerasParserProperty.getValue()).entrySet().stream()
                .filter(propsEntry -> !nonEditableProps.contains(propsEntry.getKey()))
                .sorted(comparing(Map.Entry::getKey))
                .collect(toList());
    }

    private EventHandler<TableColumn.CellEditEvent<Map.Entry<ViewProps, ?>, String>> getCellEditEventHandler(Property<String> rawValueProperty, Property<CameraInfo.CameraView> currentViewProperty) {
        return cellEditEvent -> {
            String newValue = cellEditEvent.getNewValue();
            Map.Entry<ViewProps, ?> editedRowValue = cellEditEvent.getRowValue();
            Log.debug(THIS_CLASS_NAME, "Edited prop: " + editedRowValue + ", old=" + cellEditEvent.getOldValue() + ", new=" + newValue);

            //noinspection unchecked
            Map.Entry<ViewProps, Object> editedEntry = (Map.Entry<ViewProps, Object>) editedRowValue;
            editedEntry.setValue(validateCellInput(newValue));

            ViewKind currentViewKind = currentViewProperty.getValue().getType();
            long cameraIdentifier = Long.valueOf(rawValueProperty.getValue());
            updateViewPropertiesInParser(cameraIdentifier, currentViewKind, editedEntry);
        };
    }

    private CameraInfo updateViewPropertiesInParser(long cameraIdentifier, ViewKind currentViewKind, Map.Entry<ViewProps, Object> editedProp) {
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(TYPE, currentViewKind);
        viewProps.put(editedProp.getKey(), editedProp.getValue());

        CameraInfo updatedConfiguration = CameraInfo.builder()
                .forIdentifier(cameraIdentifier)
                .addView(fromProps(viewProps))
                .build();

        Log.debug(THIS_CLASS_NAME, "Will update camera: " + cameraIdentifier);

        return CamerasHelper.updateViews(updatedConfiguration, camerasParserProperty.getValue());
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
        return Paths.get(databaseLocation, CamerasHelper.FILE_CAMERAS_BIN);
    }

    private EventHandler<ActionEvent> handleAddSetButtonAction(StringProperty rawValueProperty, ComboBox<CameraInfo> cameraSelectorComboBox) {
        return event -> {
            Optional<String> input = CommonDialogsHelper.showInputValueDialog(TITLE_ADD_SET, MESSAGE_ADD_SET_IDENTIFIER, null);
            if (!input.isPresent()) {
                return;
            }

            long newCameraSetIdentifier = input.map(Long::valueOf)
                    .orElseThrow(() -> new IllegalStateException("Should not happen"));
            long cameraSetIdentifier = Long.valueOf(rawValueProperty.getValue());

            CamerasParser camerasParser = camerasParserProperty.getValue();
            CamerasHelper.duplicateCameraSet(cameraSetIdentifier, newCameraSetIdentifier, camerasParser);

            CameraInfo newCameraInfo = CamerasHelper.fetchInformation(newCameraSetIdentifier, camerasParser);
            ObservableList<CameraInfo> cameraItems = cameraSelectorComboBox.getItems();
            if (!cameraItems.contains(newCameraInfo)) {
                cameraItems.add(newCameraInfo);
            }

            cameraSelectorComboBox.getSelectionModel().select(newCameraInfo);
        };
    }

    private EventHandler<ActionEvent> handleImportSetButtonAction() {
        return event -> dialogsHelper.askForCameraPatchLocation(null)
                .map(File::new)
                .ifPresent(this::importSetFromPatchFile);
    }

    private void importSetFromPatchFile(File file) {

    }
}

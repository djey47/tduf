package fr.tduf.gui.database.plugins.cameras;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.database.plugins.cameras.common.DisplayConstants;
import fr.tduf.gui.database.plugins.cameras.common.FxConstants;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToItemConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraInfoToRawValueConverter;
import fr.tduf.gui.database.plugins.cameras.converter.CameraViewToItemConverter;
import fr.tduf.gui.database.plugins.cameras.helper.CamerasDialogsHelper;
import fr.tduf.gui.database.plugins.cameras.helper.CamerasImExHelper;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.libunlimited.high.files.db.common.helper.CameraAndIKHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.gui.database.common.DisplayConstants.MESSAGE_SEE_LOGS;
import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.cameras.common.FxConstants.*;
import static fr.tduf.gui.database.plugins.common.FxConstants.*;
import static fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto.TopicMetadataDto.FIELD_RANK_CAMERA;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo.CameraView.fromProps;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.TYPE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
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
    private CamerasImExHelper imExHelper;

    private final Property<CameraInfoEnhanced> cameraInfoEnhancedProperty = new SimpleObjectProperty<>();
    private ObservableList<CameraInfo> cameraInfos;
    private ObservableList<CameraInfo.CameraView> cameraViews;

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

        cameraInfoEnhancedProperty.setValue(null);

        String databaseLocation = context.getDatabaseLocation();
        Path cameraFile = resolveCameraFilePath(databaseLocation);
        if (!Files.exists(cameraFile)) {
            Log.warn(THIS_CLASS_NAME, "No cameras.bin file was found in database directory: " + databaseLocation);
            return;
        }

        camerasContext.setBinaryFileLocation(cameraFile.toString());
        Log.info(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        camerasContext.setPluginLoaded(true);

        CameraInfoEnhanced cameraInfoEnhanced = CamerasHelper.loadAndParseCamerasDatabase(cameraFile.toString());
        cameraInfoEnhancedProperty.setValue(cameraInfoEnhanced);

        cameraRefHelper = new CameraAndIKHelper();
        Log.info(THIS_CLASS_NAME, "Camera reference loaded");

        dialogsHelper = new CamerasDialogsHelper();
        imExHelper = new CamerasImExHelper();
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
        CamerasHelper.saveCamerasDatabase(cameraInfoEnhancedProperty.getValue(), cameraFile);
    }

    /**
     * Required contextual information:
     * - rawValueProperty
     * - mainWindow
     * - errorProperty
     * - errorMessageProperty
     * - camerasContext->allCameras
     * - camerasContext->viewTypeProperty
     * @param context : all required information about Database Editor
     */
    @Override
    public Node renderControls(EditorContext context) {
        CamerasContext camerasContext = context.getCamerasContext();
        camerasContext.setErrorProperty(context.getErrorProperty());
        camerasContext.setErrorMessageProperty(context.getErrorMessageProperty());

        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        if (!context.getCamerasContext().isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no rendering will be performed");
            return hBox;
        }

        cameraInfos = FXCollections.observableArrayList(CamerasHelper.fetchAllInformation(cameraInfoEnhancedProperty.getValue()));
        cameraViews = FXCollections.observableArrayList();
        ComboBox<CameraInfo> cameraSelectorComboBox = new ComboBox<>(cameraInfos.sorted(comparingLong(CameraInfo::getCameraIdentifier)));
        ComboBox<CameraInfo.CameraView> viewSelectorComboBox = new ComboBox<>(cameraViews.sorted(comparing(CameraInfo.CameraView::getType)));
        VBox mainColumnBox = createMainColumn(context, cameraSelectorComboBox, viewSelectorComboBox);

        StringProperty rawValueProperty = context.getRawValueProperty();
        Window mainWindow = context.getMainWindow();
        VBox buttonColumnBox = createButtonColumn(
                handleAddSetButtonAction(rawValueProperty, cameraSelectorComboBox.getSelectionModel(), mainWindow),
                handleImportSetButtonAction(rawValueProperty, mainWindow),
                handleExportCurrentViewAction(rawValueProperty, viewSelectorComboBox.getSelectionModel(), mainWindow),
                handleExportAllViewsAction(rawValueProperty, mainWindow));

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

    private VBox createMainColumn(EditorContext context, ComboBox<CameraInfo> cameraSelectorComboBox, ComboBox<CameraInfo.CameraView> viewSelectorComboBox) {
        ObservableList<Map.Entry<ViewProps, ?>> allViewProps = FXCollections.observableArrayList();

        cameraSelectorComboBox.getStyleClass().add(CSS_CLASS_CAM_SELECTOR_COMBOBOX);
        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter(cameraRefHelper));

        viewSelectorComboBox.getStyleClass().add(CSS_CLASS_VIEW_SELECTOR_COMBOBOX);
        viewSelectorComboBox.setConverter(new CameraViewToItemConverter());

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(FxConstants.CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox camSelectorBox = createCamSelectorBox(context, cameraSelectorComboBox, viewSelectorComboBox.valueProperty());
        HBox viewSelectorBox = createViewSelectorBox(viewSelectorComboBox, cameraSelectorComboBox.valueProperty(), allViewProps);
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = createPropertiesTableView(context, allViewProps, viewSelectorComboBox.valueProperty());

        mainColumnChildren.add(camSelectorBox);
        mainColumnChildren.add(viewSelectorBox);
        mainColumnChildren.add(setPropertyTableView);
        return mainColumnBox;
    }

    private VBox createButtonColumn(EventHandler<ActionEvent> onAddSetAction, EventHandler<ActionEvent> onImportSetAction, EventHandler<ActionEvent> onExportCurrentViewAction, EventHandler<ActionEvent> onExportAllViewsAction) {
        VBox buttonColumnBox = new VBox();
        buttonColumnBox.getStyleClass().add(fr.tduf.gui.database.common.FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX);

        Button addSetButton = new Button(LABEL_ADD_BUTTON);
        addSetButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(addSetButton, TOOLTIP_ADD_BUTTON);
        addSetButton.setOnAction(onAddSetAction);

        Button importSetButton = new Button(LABEL_IMPORT_SET_BUTTON);
        importSetButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(importSetButton, TOOLTIP_IMPORT_SET_BUTTON);
        importSetButton.setOnAction(onImportSetAction);

        MenuButton exportSetMenuButton = new MenuButton(LABEL_EXPORT_SET_BUTTON);
        exportSetMenuButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(exportSetMenuButton, DisplayConstants.TOOLTIP_EXPORT_SET_BUTTON);
        MenuItem exportCurrentViewMenuItem = new MenuItem(LABEL_EXPORT_CURRENT_BUTTON);
        exportCurrentViewMenuItem.setOnAction(onExportCurrentViewAction);
        MenuItem exportAllViewsMenuItem = new MenuItem(LABEL_EXPORT_ALL_BUTTON);
        exportAllViewsMenuItem.setOnAction(onExportAllViewsAction);

        exportSetMenuButton.getItems().add(exportCurrentViewMenuItem);
        exportSetMenuButton.getItems().add(exportAllViewsMenuItem);

        ObservableList<Node> children = buttonColumnBox.getChildren();
        children.add(addSetButton);
        children.add(importSetButton);
        children.add(exportSetMenuButton);

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

    private HBox createCamSelectorBox(EditorContext context, ComboBox<CameraInfo> cameraSelectorComboBox, ObjectProperty<CameraInfo.CameraView> currentCameraViewProperty) {
        HBox camSelectorBox = new HBox();
        camSelectorBox.getStyleClass().add(CSS_CLASS_CAM_SELECTOR_BOX);

        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getCameraSelectorChangeListener(context, currentCameraViewProperty));
        Bindings.bindBidirectional(
                context.getRawValueProperty(), cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraInfos));

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

    private ChangeListener<CameraInfo> getCameraSelectorChangeListener(EditorContext context, Property<CameraInfo.CameraView> currentCameraViewProperty) {
        return (ObservableValue<? extends CameraInfo> observable, CameraInfo oldValue, CameraInfo newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            cameraViews.clear();

            CamerasContext camerasContext = context.getCamerasContext();
            if (newValue == null) {
                camerasContext.getErrorProperty().setValue(true);
                camerasContext.getErrorMessageProperty().setValue(LABEL_ERROR_TOOLTIP);
            } else {
                camerasContext.getErrorProperty().setValue(false);
                camerasContext.getErrorMessageProperty().setValue("");

                cameraViews.addAll(CamerasHelper.fetchInformation(Long.valueOf(newValue.getCameraIdentifier()).intValue(), cameraInfoEnhancedProperty.getValue()).getViews());

                if (!cameraViews.isEmpty()) {
                    currentCameraViewProperty.setValue(cameraViews.get(0));
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

    private List<Map.Entry<ViewProps, ?>> getSortedAndEditableViewProperties(long cameraIdentifier, ViewKind viewKind) {
        final Set<ViewProps> nonEditableProps = new HashSet<>(singletonList(TYPE));

        return CamerasHelper.fetchViewProperties(Long.valueOf(cameraIdentifier).intValue(), viewKind, cameraInfoEnhancedProperty.getValue()).entrySet().stream()
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

        return CamerasHelper.updateViews(updatedConfiguration, cameraInfoEnhancedProperty.getValue());
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

    private EventHandler<ActionEvent> handleAddSetButtonAction(StringProperty rawValueProperty, SingleSelectionModel<CameraInfo> cameraSelectorSelectionModel, Window mainWindow) {
        return event -> {
            Optional<String> input = CommonDialogsHelper.showInputValueDialog(TITLE_ADD_SET, MESSAGE_ADD_SET_IDENTIFIER, mainWindow);
            if (!input.isPresent()) {
                return;
            }

            int newCameraSetIdentifier = input.map(Integer::valueOf)
                    .orElseThrow(() -> new IllegalStateException("Should not happen"));
            int cameraSetIdentifier = Integer.valueOf(rawValueProperty.getValue());

            CameraInfoEnhanced cameraInfoEnhanced = cameraInfoEnhancedProperty.getValue();
            CamerasHelper.duplicateCameraSet(cameraSetIdentifier, newCameraSetIdentifier, cameraInfoEnhanced);

            CameraInfo newCameraInfo = CamerasHelper.fetchInformation(newCameraSetIdentifier, cameraInfoEnhanced);
            if (!cameraInfos.contains(newCameraInfo)) {
                cameraInfos.add(newCameraInfo);
            }

            cameraSelectorSelectionModel.select(newCameraInfo);
        };
    }

    private EventHandler<ActionEvent> handleImportSetButtonAction(StringProperty rawValueProperty, Window mainWindow) {
        return event -> dialogsHelper.askForCameraPatchLocation(mainWindow)
                .map(File::new)
                .ifPresent(file -> importSetFromPatchFile(file, Long.valueOf(rawValueProperty.get()), mainWindow));
    }

    private EventHandler<ActionEvent> handleExportAllViewsAction(StringProperty rawValueProperty, Window mainWindow) {
        return event -> dialogsHelper.askForCameraPatchSaveLocation(mainWindow)
                .map(File::new)
                .ifPresent(file -> exportSetToPatchFile(file, Long.valueOf(rawValueProperty.get()), null, mainWindow));
    }

    private EventHandler<ActionEvent> handleExportCurrentViewAction(StringProperty rawValueProperty, SingleSelectionModel<CameraInfo.CameraView> viewSelectorSelectionModel, Window mainWindow) {
        return event -> dialogsHelper.askForCameraPatchSaveLocation(mainWindow)
                .map(File::new)
                .ifPresent(file -> exportSetToPatchFile(file, Long.valueOf(rawValueProperty.get()), viewSelectorSelectionModel.getSelectedItem().getType(), mainWindow));
    }

    private void importSetFromPatchFile(File file, long targetSetIdentifier, Window mainWindow) {
        SimpleDialogOptions dialogOptions;
        try {
            String writtenPropertiesPath = imExHelper.importPatch(file, cameraInfoEnhancedProperty.getValue(), targetSetIdentifier)
                    // Extract to common display constant
                    .map(propertiesPath -> String.format("Written properties file:%s%s", System.lineSeparator(), propertiesPath))
                    .orElse("");

            dialogOptions = SimpleDialogOptions.builder()
                    .withContext(INFORMATION)
                    .withTitle(DisplayConstants.TITLE_IMPORT)
                    .withMessage(DisplayConstants.MESSAGE_DATA_IMPORTED)
                    .withDescription(writtenPropertiesPath)
                    .build();
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, e);

            dialogOptions = SimpleDialogOptions.builder()
                    .withContext(ERROR)
                    .withTitle(DisplayConstants.TITLE_IMPORT)
                    .withMessage(DisplayConstants.MESSAGE_UNABLE_IMPORT_PATCH)
                    .withDescription(MESSAGE_SEE_LOGS)
                    .build();
        }

        CommonDialogsHelper.showDialog(dialogOptions, mainWindow);
    }

    private void exportSetToPatchFile(File patchFile, long setIdentifier, ViewKind type, Window mainWindow) {
        SimpleDialogOptions dialogOptions;
        try {
            imExHelper.exportToPatch(patchFile, cameraInfoEnhancedProperty.getValue(), setIdentifier, type);

            dialogOptions = SimpleDialogOptions.builder()
                    .withContext(INFORMATION)
                    .withTitle(DisplayConstants.TITLE_EXPORT)
                    .withMessage(DisplayConstants.MESSAGE_DATA_EXPORTED)
                    .withDescription(patchFile.getPath())
                    .build();
        } catch (Exception e) {
            Log.error(THIS_CLASS_NAME, e);

            dialogOptions = SimpleDialogOptions.builder()
                    .withContext(ERROR)
                    .withTitle(DisplayConstants.TITLE_EXPORT)
                    .withMessage(DisplayConstants.MESSAGE_UNABLE_EXPORT_PATCH)
                    .withDescription(MESSAGE_SEE_LOGS)
                    .build();
        }

        CommonDialogsHelper.showDialog(dialogOptions, mainWindow);
    }
}

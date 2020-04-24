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
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.helper.CamerasImExHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.CameraAndIKHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
import fr.tduf.libunlimited.low.files.bin.cameras.dto.SetConfigurationDto;
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
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Collections.singletonList;
import static java.util.Comparator.*;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Advanced cameras edition plugin
 *
 */
public class CamerasPlugin extends AbstractDatabasePlugin {

    private static final Class<CamerasPlugin> thisClass = CamerasPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private CameraAndIKHelper cameraRefHelper;
    private CamerasDialogsHelper dialogsHelper;
    private CamerasImExHelper imExHelper;

    private final Property<CamerasDatabase> cameraInfoEnhancedProperty = new SimpleObjectProperty<>();
    private ObservableList<CameraSetInfo> cameraSetInfos;
    private ObservableList<CameraView> cameraViews;

    /**
     * Required contextual information:
     * - databaseLocation
     * - camerasContext->allCameras
     * @param editorContext : all required information about Database Editor
     * @throws IOException when cameras file can't be parsed for some reason
     */
    @Override
    public void onInit(EditorContext editorContext) throws IOException {
        super.onInit(editorContext);

        CamerasContext camerasContext = editorContext.getCamerasContext();
        camerasContext.reset();

        cameraInfoEnhancedProperty.setValue(null);

        String databaseLocation = editorContext.getDatabaseLocation();
        Path cameraFile = resolveCameraFilePath(databaseLocation);
        if (!Files.exists(cameraFile)) {
            String warningMessage = String.format(FORMAT_MESSAGE_WARN_NO_CAMBIN, databaseLocation);
            Log.warn(THIS_CLASS_NAME, warningMessage);

            throw new IOException(warningMessage);
        }

        camerasContext.setBinaryFileLocation(cameraFile.toString());
        Log.info(THIS_CLASS_NAME, "Loading camera info from " + cameraFile);
        camerasContext.setPluginLoaded(true);

        CamerasDatabase camerasDatabase = CamerasHelper.loadAndParseCamerasDatabase(cameraFile.toString());
        cameraInfoEnhancedProperty.setValue(camerasDatabase);

        cameraRefHelper = new CameraAndIKHelper();
        Log.info(THIS_CLASS_NAME, "Camera reference loaded");

        dialogsHelper = new CamerasDialogsHelper();
        imExHelper = new CamerasImExHelper();
    }

    /**
     * Required contextual information:
     * - camerasContext->pluginLoaded
     * - camerasContext->binaryFileLocation
     */
    @Override
    public void onSave() throws IOException {
        CamerasContext camerasContext = getEditorContext().getCamerasContext();
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
     * @param onTheFlyContext : all required information about Database Editor
     */
    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        EditorContext editorContext = getEditorContext();
        CamerasContext camerasContext = editorContext.getCamerasContext();
        camerasContext.setErrorProperty(onTheFlyContext.getErrorProperty());
        camerasContext.setErrorMessageProperty(onTheFlyContext.getErrorMessageProperty());

        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        if (!camerasContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Cameras plugin not loaded, no rendering will be performed");
            return hBox;
        }

        cameraSetInfos = FXCollections.observableArrayList(CamerasHelper.fetchAllInformation(cameraInfoEnhancedProperty.getValue()));
        cameraViews = FXCollections.observableArrayList();
        ComboBox<CameraSetInfo> cameraSelectorComboBox = new ComboBox<>(cameraSetInfos.sorted(comparingLong(CameraSetInfo::getCameraIdentifier)));
        ComboBox<CameraView> viewSelectorComboBox = new ComboBox<>(cameraViews.sorted(comparing(CameraView::getKind)));
        VBox mainColumnBox = createMainColumn(onTheFlyContext, cameraSelectorComboBox, viewSelectorComboBox);

        StringProperty rawValueProperty = onTheFlyContext.getRawValueProperty();
        Window mainWindow = editorContext.getMainWindow();
        VBox buttonColumnBox = createButtonColumn(
                handleAddSetButtonAction(rawValueProperty, cameraSelectorComboBox.getSelectionModel(), mainWindow),
                handleDeleteSetButtonAction(rawValueProperty, cameraSelectorComboBox.getSelectionModel()),
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

    /**
     * Initial view is either the last view kind selected, otherwise the first view
     */
    static Optional<CameraView> determineInitialCameraView(List<CameraView> sortedViews, CameraView previousCameraViewSelected) {
        CameraView initialView = ofNullable(previousCameraViewSelected)
                .flatMap(previousView -> sortedViews.stream()
                        .filter(view -> view.getKind().getInternalId() == previousView.getKind().getInternalId())
                        .findFirst())
                .orElse(sortedViews.stream()
                        .findFirst()
                        .orElse(null));
        return ofNullable(initialView);
    }

    private VBox createMainColumn(OnTheFlyContext onTheFlyContext, ComboBox<CameraSetInfo> cameraSelectorComboBox, ComboBox<CameraView> viewSelectorComboBox) {
        ObservableList<Map.Entry<ViewProps, ?>> allViewProps = FXCollections.observableArrayList();

        cameraSelectorComboBox.getStyleClass().add(CSS_CLASS_CAM_SELECTOR_COMBOBOX);
        cameraSelectorComboBox.setConverter(new CameraInfoToItemConverter(cameraRefHelper));

        viewSelectorComboBox.getStyleClass().add(CSS_CLASS_VIEW_SELECTOR_COMBOBOX);
        viewSelectorComboBox.setConverter(new CameraViewToItemConverter());

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(FxConstants.CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox camSelectorBox = createCamSelectorBox(onTheFlyContext, cameraSelectorComboBox, viewSelectorComboBox.valueProperty());
        HBox viewSelectorBox = createViewSelectorBox(viewSelectorComboBox, cameraSelectorComboBox.valueProperty(), allViewProps);
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = createPropertiesTableView(onTheFlyContext, allViewProps, viewSelectorComboBox.valueProperty());

        mainColumnChildren.add(camSelectorBox);
        mainColumnChildren.add(viewSelectorBox);
        mainColumnChildren.add(setPropertyTableView);
        return mainColumnBox;
    }

    private VBox createButtonColumn(EventHandler<ActionEvent> onAddSetAction, EventHandler<ActionEvent> onDeleteSetAction, EventHandler<ActionEvent> onImportSetAction, EventHandler<ActionEvent> onExportCurrentViewAction, EventHandler<ActionEvent> onExportAllViewsAction) {
        VBox buttonColumnBox = new VBox();
        buttonColumnBox.getStyleClass().add(fr.tduf.gui.database.common.FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX);

        Button addSetButton = new Button(LABEL_ADD_BUTTON);
        addSetButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(addSetButton, TOOLTIP_ADD_BUTTON);
        addSetButton.setOnAction(onAddSetAction);

        Button delSetButton = new Button(LABEL_DEL_BUTTON);
        delSetButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(delSetButton, TOOLTIP_DEL_BUTTON);
        delSetButton.setOnAction(onDeleteSetAction);

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
        children.add(delSetButton);
        children.add(importSetButton);
        children.add(exportSetMenuButton);

        return buttonColumnBox;
    }

    private TableView<Map.Entry<ViewProps, ?>> createPropertiesTableView(OnTheFlyContext context, ObservableList<Map.Entry<ViewProps, ?>> viewProps, ObjectProperty<CameraView> currentViewProperty) {
        TableView<Map.Entry<ViewProps, ?>> setPropertyTableView = new TableView<>(viewProps);
        setPropertyTableView.getStyleClass().addAll(CSS_CLASS_TABLEVIEW, CSS_CLASS_SET_PROPERTY_TABLEVIEW);
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

        //noinspection unchecked
        setPropertyTableView.getColumns().addAll(settingColumn, descriptionColumn, valueColumn);

        return setPropertyTableView;
    }

    private HBox createCamSelectorBox(OnTheFlyContext onTheFlyContext, ComboBox<CameraSetInfo> cameraSelectorComboBox, ObjectProperty<CameraView> currentCameraViewProperty) {
        HBox camSelectorBox = new HBox();
        camSelectorBox.getStyleClass().add(CSS_CLASS_CAM_SELECTOR_BOX);

        cameraSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getCameraSelectorChangeListener(currentCameraViewProperty));
        Bindings.bindBidirectional(
                onTheFlyContext.getRawValueProperty(), cameraSelectorComboBox.valueProperty(), new CameraInfoToRawValueConverter(cameraSetInfos));

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

    private HBox createViewSelectorBox(ComboBox<CameraView> viewSelectorComboBox, Property<CameraSetInfo> currentCameraSetProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
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

    private ChangeListener<CameraSetInfo> getCameraSelectorChangeListener(ObjectProperty<CameraView> currentCameraViewProperty) {
        return (ObservableValue<? extends CameraSetInfo> observable, CameraSetInfo oldValue, CameraSetInfo newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            final CameraView previousCameraViewSelected = currentCameraViewProperty.getValue();

            cameraViews.clear();

            EditorContext editorContext = getEditorContext();
            CamerasContext camerasContext = editorContext.getCamerasContext();
            if (newValue == null) {
                camerasContext.getErrorProperty().setValue(true);
                camerasContext.getErrorMessageProperty().setValue(LABEL_ERROR_TOOLTIP);
            } else {
                camerasContext.getErrorProperty().setValue(false);
                camerasContext.getErrorMessageProperty().setValue("");

                final List<CameraView> sortedViews = CamerasHelper.fetchInformation(newValue.getCameraIdentifier(), cameraInfoEnhancedProperty.getValue()).getViews().stream()
                        .sorted(comparingInt(view -> view.getKind().getInternalId()))
                        .collect(toList());

                cameraViews.addAll(sortedViews);
                determineInitialCameraView(sortedViews, previousCameraViewSelected)
                        .ifPresent(currentCameraViewProperty::setValue);

                getEditorContext().getChangeDataController().updateContentItem(CAR_PHYSICS_DATA, FIELD_RANK_CAMERA, Integer.toString(newValue.getCameraIdentifier()));
            }
        };
    }

    private ChangeListener<CameraView> getViewSelectorChangeListener(Property<CameraSetInfo> currentCameraSetProperty, ObservableList<Map.Entry<ViewProps, ?>> allViewProps) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            allViewProps.clear();

            if (newValue != null) {
                allViewProps.addAll(
                        getSortedAndEditableViewProperties(currentCameraSetProperty.getValue().getCameraIdentifier(), newValue.getKind()));
            }
        };
    }

    private List<Map.Entry<ViewProps, ?>> getSortedAndEditableViewProperties(int cameraIdentifier, ViewKind viewKind) {
        return CamerasHelper.fetchViewProperties(Long.valueOf(cameraIdentifier).intValue(), viewKind, cameraInfoEnhancedProperty.getValue()).entrySet().stream()
                .sorted(comparingByKey())
                .collect(toList());
    }

    private EventHandler<TableColumn.CellEditEvent<Map.Entry<ViewProps, ?>, String>> getCellEditEventHandler(Property<String> rawValueProperty, ObjectProperty<CameraView> currentViewProperty) {
        return cellEditEvent -> {
            String newValue = cellEditEvent.getNewValue();
            Map.Entry<ViewProps, ?> editedRowValue = cellEditEvent.getRowValue();
            Log.debug(THIS_CLASS_NAME, "Edited prop: " + editedRowValue + ", old=" + cellEditEvent.getOldValue() + ", new=" + newValue);

            //noinspection unchecked
            Map.Entry<ViewProps, Object> editedEntry = (Map.Entry<ViewProps, Object>) editedRowValue;
            editedEntry.setValue(validateCellInput(newValue));

            ViewKind currentViewKind = currentViewProperty.getValue().getKind();
            int cameraIdentifier = Integer.parseInt(rawValueProperty.getValue());
            updateViewProperties(cameraIdentifier, currentViewKind, editedEntry);
        };
    }

    private void updateViewProperties(int cameraIdentifier, ViewKind currentViewKind, Map.Entry<ViewProps, Object> editedProp) {
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(editedProp.getKey(), editedProp.getValue());

        SetConfigurationDto updatedConfiguration = SetConfigurationDto.builder()
                .forIdentifier(cameraIdentifier)
                .addView(CameraView.fromProps(viewProps, currentViewKind, cameraIdentifier))
                .build();

        Log.debug(THIS_CLASS_NAME, "Will update camera: " + cameraIdentifier);

        CamerasHelper.updateViews(updatedConfiguration, cameraInfoEnhancedProperty.getValue());
    }

    private int validateCellInput(String value) {
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException nfe) {
            Log.error(THIS_CLASS_NAME, "Unsupported value was entered: " + value);
            throw nfe;
        }
    }

    private Path resolveCameraFilePath(String databaseLocation) {
        return Paths.get(databaseLocation, CamerasHelper.FILE_CAMERAS_BIN);
    }

    private EventHandler<ActionEvent> handleAddSetButtonAction(StringProperty rawValueProperty, SingleSelectionModel<CameraSetInfo> cameraSelectorSelectionModel, Window mainWindow) {
        return event -> {
            Optional<String> input = CommonDialogsHelper.showInputValueDialog(TITLE_ADD_SET, MESSAGE_ADD_SET_IDENTIFIER, mainWindow);
            if (!input.isPresent()) {
                return;
            }

            int newCameraSetIdentifier = input.map(Integer::valueOf)
                    .orElseThrow(() -> new IllegalStateException("Should not happen"));
            int cameraSetIdentifier = Integer.parseInt(rawValueProperty.getValue());

            CamerasDatabase camerasDatabase = cameraInfoEnhancedProperty.getValue();
            CamerasHelper.duplicateCameraSet(cameraSetIdentifier, newCameraSetIdentifier, camerasDatabase);

            CameraSetInfo newCameraInfo = CamerasHelper.fetchInformation(newCameraSetIdentifier, camerasDatabase);
            cameraSetInfos.add(newCameraInfo);

            cameraSelectorSelectionModel.select(newCameraInfo);
        };
    }

    private EventHandler<ActionEvent> handleDeleteSetButtonAction(StringProperty rawValueProperty, SingleSelectionModel<CameraSetInfo> cameraSelectorSelectionModel) {
        return event -> {
            int cameraSetIdentifier = Integer.parseInt(rawValueProperty.getValue());
            CamerasDatabase camerasDatabase = cameraInfoEnhancedProperty.getValue();

            CamerasHelper.deleteCameraSet(cameraSetIdentifier, camerasDatabase);

            CameraSetInfo currentCameraInfo = cameraSelectorSelectionModel.getSelectedItem();
            cameraSetInfos.remove(currentCameraInfo);

            cameraSelectorSelectionModel.selectFirst();
        };
    }

    private EventHandler<ActionEvent> handleImportSetButtonAction(StringProperty rawValueProperty, Window mainWindow) {
        return event -> dialogsHelper.askForCameraPatchLocation(mainWindow)
                .map(File::new)
                .ifPresent(file -> importSetFromPatchFile(file, Long.parseLong(rawValueProperty.get()), mainWindow));
    }

    private EventHandler<ActionEvent> handleExportAllViewsAction(StringProperty rawValueProperty, Window mainWindow) {
        return event -> dialogsHelper.askForCameraPatchSaveLocation(mainWindow)
                .map(File::new)
                .ifPresent(file -> exportSetToPatchFile(file, Long.parseLong(rawValueProperty.get()), null, mainWindow));
    }

    private EventHandler<ActionEvent> handleExportCurrentViewAction(StringProperty rawValueProperty, SingleSelectionModel<CameraView> viewSelectorSelectionModel, Window mainWindow) {
        return event -> dialogsHelper.askForCameraPatchSaveLocation(mainWindow)
                .map(File::new)
                .ifPresent(file -> exportSetToPatchFile(file, Long.parseLong(rawValueProperty.get()), viewSelectorSelectionModel.getSelectedItem().getKind(), mainWindow));
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

    /**
     * Visible for testing
     */
    protected void setEditorContext(EditorContext editorContext) {
        super.setEditorContext(editorContext);
    }
}

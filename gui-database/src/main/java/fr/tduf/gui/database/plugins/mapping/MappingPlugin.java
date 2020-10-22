package fr.tduf.gui.database.plugins.mapping;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.common.javafx.helper.DesktopHelper;
import fr.tduf.gui.database.plugins.cameras.common.FxConstants;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginComponentBuilders;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.common.contexts.PluginContext;
import fr.tduf.gui.database.plugins.mapping.converter.BooleanStatusToDisplayConverter;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static fr.tduf.gui.database.common.DisplayConstants.*;
import static fr.tduf.gui.database.common.FxConstants.CSS_CLASS_TABLEVIEW;
import static fr.tduf.gui.database.plugins.common.FxConstants.*;
import static fr.tduf.gui.database.plugins.mapping.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.mapping.common.FxConstants.*;
import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.*;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javafx.geometry.Orientation.VERTICAL;

/**
 * File mapping status plugin
 */
public class MappingPlugin extends AbstractDatabasePlugin {
    private static final Class<MappingPlugin> thisClass = MappingPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private static final Set<DbDto.Topic> HANDLED_TOPICS = new HashSet<>(asList(
            CAR_PHYSICS_DATA, CAR_PACKS, CAR_SHOPS, CLOTHES, HOUSES, RIMS, TUTORIALS
    ));

    private final PluginContext mappingContext = new PluginContext();

    @SuppressWarnings("FieldMayBeFinal")
    private Property<BankMap> bankMapProperty = new SimpleObjectProperty<>();

    /**
     * Required contextual information:
     *
     * @param pluginName : name of plugin to initialize
     * @param editorContext : all required information about Database Editor
     * @throws IOException when cameras file can't be parsed for some reason
     */
    @Override
    public void onInit(String pluginName, EditorContext editorContext) throws IOException {
        super.onInit(pluginName, editorContext);

        mappingContext.reset();
        
        bankMapProperty.setValue(null);

        String gameLocation = editorContext.getGameLocation();
        Path mappingFile = resolveMappingFilePath(gameLocation);
        if (!Files.exists(mappingFile)) {
            String warningMessage = String.format(FORMAT_MESSAGE_WARN_NO_MAPPING, gameLocation);
            Log.warn(THIS_CLASS_NAME, warningMessage);

            throw new IOException(warningMessage);
        }

        Log.info(THIS_CLASS_NAME, "Loading mapping info from " + mappingFile);

        String mapFileName = mappingFile.toString();
        BankMap bankMap = MapParser.load(mapFileName).parse();
        bankMapProperty.setValue(bankMap);

        Log.info(THIS_CLASS_NAME, String.format("Mapping info loaded, %d entries available", bankMap.getEntries().size()));
        mappingContext.setPluginLoaded(true);
        mappingContext.setBinaryFileLocation(mapFileName);
    }

    @Override
    public void onSave() throws IOException {
        if (!mappingContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Mapping plugin not loaded, no saving will be performed");
            return;
        }

        String mapFile = mappingContext.getBinaryFileLocation();
        Log.info(THIS_CLASS_NAME, "Saving mapping info to " + mapFile);
        MapHelper.saveBankMap(bankMapProperty.getValue(), mapFile);
    }

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        OnTheFlyMappingContext onTheFlyMappingContext = (OnTheFlyMappingContext) onTheFlyContext;

        mappingContext.setErrorProperty(onTheFlyMappingContext.getErrorProperty());
        mappingContext.setErrorMessageProperty(onTheFlyMappingContext.getErrorMessageProperty());

        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        if (!mappingContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Mapping plugin not loaded, no rendering will be performed");
            return hBox;
        }
        
        TableView<MappingEntry> filesTableView = createFilesTableView(onTheFlyMappingContext);

        VBox mainColumnBox = createMainColumn(filesTableView, onTheFlyMappingContext);
        VBox buttonColumnBox = createButtonColumn(filesTableView.getSelectionModel(), onTheFlyMappingContext);
        
        ObservableList<Node> mainRowChildren = hBox.getChildren();
        mainRowChildren.add(mainColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));
        mainRowChildren.add(buttonColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));       
        
        return hBox;
    }

    @Override
    public Set<String> getCss() {
        return new HashSet<>(singletonList(thisClass.getResource(PATH_RESOURCE_CSS_MAPPING).toExternalForm()));
    }

    Path resolveMappingFilePath(String gameLocation) {
        return Paths.get(gameLocation, FileConstants.DIRECTORY_EURO, FileConstants.DIRECTORY_BANKS, MapHelper.MAPPING_FILE_NAME);
    }

    Path resolveBankFilePath(String gameLocation, String filePath) {
        return Paths.get(gameLocation, FileConstants.DIRECTORY_EURO, FileConstants.DIRECTORY_BANKS, filePath);
    }

    MappingEntry createMappingEntry(String resourceValue, MappedFileKind kind, OnTheFlyMappingContext onTheFlyMappingContext) {
        String fileName = String.format(kind.getFileNameFormat(), resourceValue);
        int lastPartIndex = fileName.lastIndexOf("_");
        int dotIndex = fileName.lastIndexOf(".");
        Path parentPath = kind.getParentPath();
        Path filePath;

        switch(kind) {
            case FRONT_RIMS_3D:
            case REAR_RIMS_3D:
                filePath = parentPath.resolve(resolveRimDirectoryName(onTheFlyMappingContext)).resolve(fileName);
                break;
            case SHOP_EXT_3D:
            case HOUSE_EXT_3D:
            case REALTOR_EXT_3D:
                filePath = parentPath.resolve(fileName.substring(0, lastPartIndex).toLowerCase() + fileName.substring(dotIndex));
                break;
            case SHOP_INT_3D:
            case REALTOR_INT_3D:
                filePath = parentPath.resolve(PREFIX_SPOT_INTERIOR_BANK + fileName.substring(1, lastPartIndex).toLowerCase() + fileName.substring(dotIndex));
                break;            
            case HOUSE_LOUNGE_3D:
                filePath = parentPath.resolve(PREFIX_SPOT_LOUNGE_BANK + fileName.substring(1, lastPartIndex).toLowerCase() + fileName.substring(dotIndex));
                break;            
            case HOUSE_GARAGE_3D:
                filePath = parentPath.resolve(PREFIX_SPOT_GARAGE_BANK + fileName.substring(1, lastPartIndex).toLowerCase() + fileName.substring(dotIndex));
                break;
            case CLOTHES_3D:
                filePath = parentPath.resolve(resolveClothesBrandDirectoryName(onTheFlyMappingContext)).resolve(fileName);
                break;
            default:
                filePath = parentPath.resolve(fileName);
        }

        boolean exists = Files.exists(resolveBankFilePath(getEditorContext().getGameLocation(), filePath.toString()));
        boolean registered = MapHelper.hasEntryForPath(bankMapProperty.getValue(), filePath.toString());
        return new MappingEntry(kind.getDescription(), filePath.toString(), exists, registered);
    }

    void refreshMapping(String resourceReference, OnTheFlyMappingContext onTheFlyMappingContext) {
        StringProperty errorMessageProperty = mappingContext.getErrorMessageProperty();
        BooleanProperty errorProperty = mappingContext.getErrorProperty();

        ObservableList<MappingEntry> files = onTheFlyMappingContext.getFiles();
        files.clear();

        if (!HANDLED_TOPICS.contains(onTheFlyMappingContext.getCurrentTopic())) {
            errorMessageProperty.setValue(null);
            errorProperty.setValue(null);
            return;
        }

        BulkDatabaseMiner miner = getEditorContext().getMiner();
        String resourceValue = miner.getResourceEntryFromTopicAndReference(onTheFlyMappingContext.getRemoteTopic(), resourceReference)
                .flatMap(ResourceEntryDto::pickValue)
                .orElse(VALUE_RESOURCE_DEFAULT);

        switch (onTheFlyMappingContext.getCurrentTopic()) {
            case CAR_PHYSICS_DATA:
                addCarPhysicsEntries(resourceValue, onTheFlyMappingContext);
                break;
            case CAR_PACKS:
                addCarPacksEntries(resourceValue, onTheFlyMappingContext);
                break;
            case CAR_SHOPS:
                addCarShopsEntries(resourceValue, onTheFlyMappingContext);
                break;
            case CLOTHES:
                addClothesEntries(resourceValue, onTheFlyMappingContext);
                break;
            case HOUSES:
                addHousesEntries(resourceValue, onTheFlyMappingContext);
                break;
            case RIMS:
                addRimsEntries(resourceValue, onTheFlyMappingContext);
                break;
            case TUTORIALS:
                addTutorialsEntries(resourceValue, onTheFlyMappingContext);
                break;
            default:
        }

        boolean isRegistrationFailure = files.stream().anyMatch(entry -> !entry.isRegistered());
        errorProperty.setValue(isRegistrationFailure);
        errorMessageProperty.setValue(isRegistrationFailure ? LABEL_ERROR_TOOLTIP_UNREGISTERED : "");
    }

    private VBox createMainColumn(TableView<MappingEntry> filesTableView, OnTheFlyMappingContext onTheFlyMappingContext) {
        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(FxConstants.CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        mainColumnChildren.add(filesTableView);

        onTheFlyMappingContext.getRawValueProperty().addListener(handleResourceValueChange(onTheFlyMappingContext));

        return mainColumnBox;
    }

    private VBox createButtonColumn(TableView.TableViewSelectionModel<MappingEntry> selectionModel, OnTheFlyMappingContext onTheFlyMappingContext) {
        EditorContext editorContext = getEditorContext();

        Button refreshMappingButton = new Button(LABEL_BUTTON_REFRESH);
        refreshMappingButton.getStyleClass().addAll(CSS_CLASS_PLUGIN_BUTTON, CSS_CLASS_PLUGIN_BUTTON_MEDIUM);
        refreshMappingButton.setOnAction(handleRefreshButtonAction(onTheFlyMappingContext));

        Button seeDirectoryButton = new Button(LABEL_BUTTON_GOTO);
        seeDirectoryButton.getStyleClass().addAll(CSS_CLASS_PLUGIN_BUTTON, CSS_CLASS_PLUGIN_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(seeDirectoryButton, TOOLTIP_BUTTON_SEE_DIRECTORY);
        seeDirectoryButton.setOnAction(handleSeeDirectoryButtonAction(selectionModel, editorContext.getGameLocation()));

        Button registerButton = new Button(LABEL_BUTTON_REGISTER);
        registerButton.getStyleClass().addAll(CSS_CLASS_PLUGIN_BUTTON, CSS_CLASS_PLUGIN_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(registerButton, TOOLTIP_BUTTON_REGISTER);
        registerButton.setOnAction(handleRegisterButtonAction(selectionModel));

        return PluginComponentBuilders.buttonColumn()
                .withButton(refreshMappingButton)
                .withSeparator()
                .withBrowseResourceButton(editorContext, onTheFlyMappingContext)
                .withButton(seeDirectoryButton)
                .withButton(registerButton)
                .build();
    }

    private TableView<MappingEntry> createFilesTableView(OnTheFlyMappingContext onTheFlyMappingContext) {
        TableView<MappingEntry> mappingInfoTableView = new TableView<>(FXCollections.observableArrayList());
        mappingInfoTableView.getStyleClass().addAll(CSS_CLASS_TABLEVIEW, CSS_CLASS_PLUGIN_TABLEVIEW, CSS_CLASS_MAPPING_TABLEVIEW);

        TableColumn<MappingEntry, String> kindColumn = new TableColumn<>(HEADER_FILESTABLE_KIND);
        kindColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKind()));

        TableColumn<MappingEntry, String> pathColumn = new TableColumn<>(HEADER_FILESTABLE_PATH);
        pathColumn.getStyleClass().add(CSS_CLASS_PATH_TABLECOLUMN);
        pathColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPath()));

        TableColumn<MappingEntry, String> existsColumn = new TableColumn<>(HEADER_FILESTABLE_EXISTS);
        existsColumn.setCellValueFactory(cellData -> {
            Property<String> existStatusProperty = new SimpleStringProperty();
            Bindings.bindBidirectional(existStatusProperty, cellData.getValue().existingProperty(), new BooleanStatusToDisplayConverter());
            return existStatusProperty;
        });

        TableColumn<MappingEntry, String> registeredColumn = new TableColumn<>(HEADER_FILESTABLE_REGISTERED);
        registeredColumn.setCellValueFactory(cellData -> {
            Property<String> registerStatusProperty = new SimpleStringProperty();
            Bindings.bindBidirectional(registerStatusProperty, cellData.getValue().registeredProperty(), new BooleanStatusToDisplayConverter());
            return registerStatusProperty;
        });

        ObservableList<TableColumn<MappingEntry, ?>> columns = mappingInfoTableView.getColumns();
        columns.add(kindColumn);
        columns.add(pathColumn);
        columns.add(existsColumn);
        columns.add(registeredColumn);

        onTheFlyMappingContext.setFiles(mappingInfoTableView.getItems());

        return mappingInfoTableView;
    }

    private ChangeListener<String> handleResourceValueChange(OnTheFlyMappingContext onTheFlyMappingContext) {
        return (observable, oldValue, newValue) ->  {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            refreshMapping(newValue, onTheFlyMappingContext);
        };
    }

    private EventHandler<ActionEvent> handleSeeDirectoryButtonAction(TableView.TableViewSelectionModel<MappingEntry> selectionModel, String gameLocation) {
        return event -> {
            if (selectionModel.getSelectedItem() == null) {
                return;
            }

            String selectedPath = selectionModel.getSelectedItem().getPath();
            Path parentPath = resolveBankFilePath(gameLocation, selectedPath).getParent();

            DesktopHelper.openInFiles(parentPath);
        };
    }

    private EventHandler<ActionEvent> handleRegisterButtonAction(TableView.TableViewSelectionModel<MappingEntry> selectionModel) {
        return event -> {
            MappingEntry selectedItem = selectionModel.getSelectedItem();
            if (selectedItem == null) {
                return;
            }

            MapHelper.registerPath(bankMapProperty.getValue(), selectedItem.getPath());
            selectedItem.registered();
        };
    }

    private EventHandler<ActionEvent> handleRefreshButtonAction(OnTheFlyMappingContext onTheFlyMappingContext) {
        return event -> refreshMapping(onTheFlyMappingContext.getRawValueProperty().getValue(), onTheFlyMappingContext);
    }

    private void addTutorialsEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        if (FIELD_RANK_VOICE_FILE == onTheFlyMappingContext.getFieldRank()) {
            MappingEntry tutoMappingEntry = createMappingEntry(resourceValue, TUTO_INSTRUCTION, onTheFlyMappingContext);
            ObservableList<MappingEntry> files = onTheFlyMappingContext.getFiles();
            files.add(tutoMappingEntry);
        }
    }

    private void addRimsEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        ObservableList<MappingEntry> files = onTheFlyMappingContext.getFiles();
        int fieldRank = onTheFlyMappingContext.getFieldRank();
        if (FIELD_RANK_RSC_FILE_NAME_FRONT == fieldRank) {
            MappingEntry frontMappingEntry = createMappingEntry(resourceValue, FRONT_RIMS_3D, onTheFlyMappingContext);
            files.add(frontMappingEntry);
        } else if (FIELD_RANK_RSC_FILE_NAME_REAR == fieldRank) {
            MappingEntry rearMappingEntry = createMappingEntry(resourceValue, REAR_RIMS_3D, onTheFlyMappingContext);
            files.add(rearMappingEntry);
        }
    }

    private void addClothesEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        if (FIELD_RANK_FURNITURE_FILE == onTheFlyMappingContext.getFieldRank()) {
            MappingEntry clothesMappingEntry = createMappingEntry(resourceValue, CLOTHES_3D, onTheFlyMappingContext);
            onTheFlyMappingContext.getFiles().add(clothesMappingEntry);
        }
    }

    private void addCarShopsEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        if (FIELD_RANK_DEALER_NAME == onTheFlyMappingContext.getFieldRank()) {
            MappingEntry shopExtMappingEntry = createMappingEntry(resourceValue, SHOP_EXT_3D, onTheFlyMappingContext);
            MappingEntry shopIntMappingEntry = createMappingEntry(resourceValue, SHOP_INT_3D, onTheFlyMappingContext);
            MappingEntry shopThumbMappingEntry = createMappingEntry(resourceValue, SPOT_MAP_SCREEN, onTheFlyMappingContext);
            onTheFlyMappingContext.getFiles().addAll(shopExtMappingEntry, shopIntMappingEntry, shopThumbMappingEntry);
        }
    }

    private void addHousesEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        int fieldRank = onTheFlyMappingContext.getFieldRank();
        ObservableList<MappingEntry> files = onTheFlyMappingContext.getFiles();
        if (FIELD_RANK_SPOT_NAME == fieldRank) {
            MappingEntry houseExtMappingEntry = createMappingEntry(resourceValue, HOUSE_EXT_3D, onTheFlyMappingContext);
            MappingEntry houseLoungeMappingEntry = createMappingEntry(resourceValue, HOUSE_LOUNGE_3D, onTheFlyMappingContext);
            MappingEntry houseGarageMappingEntry = createMappingEntry(resourceValue, HOUSE_GARAGE_3D, onTheFlyMappingContext);
            MappingEntry thumbMappingEntry = createMappingEntry(resourceValue, SPOT_MAP_SCREEN, onTheFlyMappingContext);
            files.addAll(houseExtMappingEntry, houseLoungeMappingEntry, houseGarageMappingEntry, thumbMappingEntry);
        } else if (FIELD_RANK_REALTOR == fieldRank) {
            MappingEntry realtorExtMappingEntry = createMappingEntry(resourceValue, REALTOR_EXT_3D, onTheFlyMappingContext);
            MappingEntry realtorIntMappingEntry = createMappingEntry(resourceValue, REALTOR_INT_3D, onTheFlyMappingContext);
            MappingEntry thumbMappingEntry = createMappingEntry(resourceValue, SPOT_MAP_SCREEN, onTheFlyMappingContext);
            files.addAll(realtorExtMappingEntry, realtorIntMappingEntry,  thumbMappingEntry);
        }
    }

    private void addCarPacksEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        if (FIELD_RANK_CAR_FILE_NAME_SWAP == onTheFlyMappingContext.getFieldRank()) {
            MappingEntry extMappingEntry = createMappingEntry(resourceValue, EXT_3D, onTheFlyMappingContext);
            onTheFlyMappingContext.getFiles().add(extMappingEntry);
        }
    }

    private void addCarPhysicsEntries(String resourceValue, OnTheFlyMappingContext onTheFlyMappingContext) {
        int fieldRank = onTheFlyMappingContext.getFieldRank();
        ObservableList<MappingEntry> files = onTheFlyMappingContext.getFiles();
        if (FIELD_RANK_CAR_FILE_NAME == fieldRank) {
            MappingEntry extMappingEntry = createMappingEntry(resourceValue, EXT_3D, onTheFlyMappingContext);
            MappingEntry intMappingEntry = createMappingEntry(resourceValue, INT_3D, onTheFlyMappingContext);
            MappingEntry sndMappingEntry = createMappingEntry(resourceValue, SOUND, onTheFlyMappingContext);
            files.addAll(extMappingEntry, intMappingEntry, sndMappingEntry);
        } else if (FIELD_RANK_HUD_FILE_NAME == fieldRank) {
            MappingEntry lgeMappingEntry = createMappingEntry(resourceValue, HUD_LOW, onTheFlyMappingContext);
            MappingEntry hgeMappingEntry = createMappingEntry(resourceValue, HUD_HIGH, onTheFlyMappingContext);
            files.addAll(lgeMappingEntry, hgeMappingEntry);
        }
    }

    private String resolveRimDirectoryName(OnTheFlyMappingContext onTheFlyMappingContext) {
        String directoryRef = retrieveCurrentRawValueFromContext(onTheFlyMappingContext, FIELD_RANK_RSC_PATH)
                .orElseThrow(() -> new IllegalStateException("No content item for directory name"));
        return getEditorContext().getMiner().getResourceEntryFromTopicAndReference(onTheFlyMappingContext.getCurrentTopic(), directoryRef)
                .flatMap(ResourceEntryDto::pickValue)
                .orElseThrow(() -> new IllegalStateException("No resource value for ref: " + directoryRef));
    }

    private String resolveClothesBrandDirectoryName(OnTheFlyMappingContext onTheFlyMappingContext) {
        String brandRef =  retrieveCurrentRawValueFromContext(onTheFlyMappingContext, FIELD_RANK_FURNITURE_BRAND)
                .orElseThrow(() -> new IllegalStateException("No content item for brand reference"));
        BulkDatabaseMiner miner = getEditorContext().getMiner();
        return miner.getContentEntryFromTopicWithReference(brandRef, BRANDS)
                .orElseThrow(() -> new IllegalStateException("No brand content entry for ref: " + brandRef))
                .getItemAtRank(FIELD_RANK_MANUFACTURER_ID)
                .flatMap(item -> miner.getResourceEntryFromTopicAndReference(BRANDS, item.getRawValue()))
                .flatMap(ResourceEntryDto::pickValue)
                .orElseThrow(() -> new IllegalStateException("No manufacturer id available"));
    }

    private Optional<String> retrieveCurrentRawValueFromContext(OnTheFlyMappingContext onTheFlyMappingContext, int itemRank) {
        BulkDatabaseMiner miner = getEditorContext().getMiner();
        DbDto.Topic currentTopic = onTheFlyMappingContext.getCurrentTopic();
        int entryId = onTheFlyMappingContext.getContentEntryIndexProperty().getValue();
        return miner.getContentEntryFromTopicWithInternalIdentifier(entryId, currentTopic)
                .orElseThrow(() -> new IllegalStateException("No content entry for identifier: " + entryId))
                .getItemAtRank(itemRank)
                .map(ContentItemDto::getRawValue);
    }

    /**
     * Visible for testing
     */
    protected void setEditorContext(EditorContext editorContext) {
        super.setEditorContext(editorContext);
    }

    PluginContext getMappingContext() {
        return mappingContext;
    }
}

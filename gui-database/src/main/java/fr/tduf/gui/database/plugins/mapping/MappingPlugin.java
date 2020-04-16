package fr.tduf.gui.database.plugins.mapping;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.common.javafx.helper.DesktopHelper;
import fr.tduf.gui.database.plugins.cameras.common.FxConstants;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.gui.database.plugins.mapping.converter.BooleanStatusToDisplayConverter;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
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
import java.util.Set;

import static fr.tduf.gui.database.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.common.FxConstants.*;
import static fr.tduf.gui.database.plugins.mapping.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.mapping.common.FxConstants.*;
import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.*;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static java.util.Collections.singletonList;
import static javafx.geometry.Orientation.VERTICAL;

/**
 * File mapping status plugin
 */
public class MappingPlugin extends AbstractDatabasePlugin {
    private static final Class<MappingPlugin> thisClass = MappingPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private Property<BankMap> bankMapProperty = new SimpleObjectProperty<>();

    /**
     * Required contextual information:
     * @param context : all required information about Database Editor
     * @throws IOException when cameras file can't be parsed for some reason
     */
    @Override
    public void onInit(EditorContext context) throws IOException {
        MappingContext mappingContext = context.getMappingContext();
        mappingContext.reset();
        
        bankMapProperty.setValue(null);

        String gameLocation = context.getGameLocation();
        Path mappingFile = resolveMappingFilePath(gameLocation);
        if (!Files.exists(mappingFile)) {
            String warningMessage = String.format("No bnk1.map file was found under game directory: %s", gameLocation);
            Log.warn(THIS_CLASS_NAME, warningMessage);

            throw new IOException(warningMessage);
        }

        Log.info(THIS_CLASS_NAME, "Loading mapping info from " + mappingFile);

        String mapFileName = mappingFile.toString();
        BankMap bankMap = MapParser.load(mapFileName).parse();
        bankMapProperty.setValue(bankMap);

        Log.info(THIS_CLASS_NAME, "Mapping info loaded");        
        mappingContext.setPluginLoaded(true);
        mappingContext.setBinaryFileLocation(mapFileName);
    }

    @Override
    public void onSave(EditorContext context) throws IOException {
        MappingContext mappingContext = context.getMappingContext();
        if (!mappingContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Mapping plugin not loaded, no saving will be performed");
            return;
        }

        String mapFile = mappingContext.getBinaryFileLocation();
        Log.info(THIS_CLASS_NAME, "Saving mapping info to " + mapFile);
        MapHelper.saveBankMap(bankMapProperty.getValue(), mapFile);
    }

    @Override
    public Node renderControls(EditorContext context) {
        MappingContext mappingContext = context.getMappingContext();
        mappingContext.setErrorProperty(context.getErrorProperty());
        mappingContext.setErrorMessageProperty(context.getErrorMessageProperty());

        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        if (!context.getMappingContext().isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Mapping plugin not loaded, no rendering will be performed");
            return hBox;
        }
        
        TableView<MappingEntry> filesTableView = createFilesTableView();
        
        VBox mainColumnBox = createMainColumn(context, filesTableView);
        VBox buttonColumnBox = createButtonColumn(context, filesTableView.getSelectionModel());
        
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

    MappingEntry createMappingEntry(String resourceValue, MappedFileKind kind, String gameLocation, EditorContext context) {
        String fileName = String.format(kind.getFileNameFormat(), resourceValue);
        int lastPartIndex = fileName.lastIndexOf("_");
        int dotIndex = fileName.lastIndexOf(".");
        Path parentPath = kind.getParentPath();
        Path filePath;

        switch(kind) {
            case FRONT_RIMS_3D:
            case REAR_RIMS_3D:
                filePath = parentPath.resolve(resolveRimDirectoryName(context)).resolve(fileName);
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
                filePath = parentPath.resolve(resolveClothesBrandDirectoryName(context)).resolve(fileName);
                break;
            default:
                filePath = parentPath.resolve(fileName);
        }

        boolean exists = Files.exists(resolveBankFilePath(gameLocation, filePath.toString()));
        boolean registered = MapHelper.hasEntryForPath(bankMapProperty.getValue(), filePath.toString());
        return new MappingEntry(kind.getDescription(), filePath.toString(), exists, registered);
    }

    private VBox createMainColumn(EditorContext context, TableView<MappingEntry> filesTableView) {
        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(FxConstants.CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        mainColumnChildren.add(filesTableView);

        context.getRawValueProperty().addListener(handleResourceValueChange(filesTableView.getItems(), context));

        return mainColumnBox;
    }

    private VBox createButtonColumn(EditorContext context, TableView.TableViewSelectionModel<MappingEntry> selectionModel) {
        VBox buttonColumnBox = new VBox();
        buttonColumnBox.getStyleClass().add(fr.tduf.gui.database.common.FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX);

        Button browseResourceButton = new Button(LABEL_BUTTON_BROWSE);
        browseResourceButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(browseResourceButton, TOOLTIP_BUTTON_BROWSE_RESOURCES);
        browseResourceButton.setOnAction(
                context.getMainStageController().getViewData().handleBrowseResourcesButtonMouseClick(context.getRemoteTopic(), context.getRawValueProperty(), context.getFieldRank()));

        Button seeDirectoryButton = new Button(LABEL_BUTTON_GOTO);
        seeDirectoryButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(seeDirectoryButton, TOOLTIP_BUTTON_SEE_DIRECTORY);
        seeDirectoryButton.setOnAction(handleSeeDirectoryButtonAction(selectionModel, context.getGameLocation()));

        Button registerButton = new Button(LABEL_BUTTON_REGISTER);
        registerButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(registerButton, TOOLTIP_BUTTON_REGISTER);
        registerButton.setOnAction(handleRegisterButtonAction(selectionModel));

        buttonColumnBox.getChildren().addAll(browseResourceButton, seeDirectoryButton, registerButton);

        return buttonColumnBox;
    }

    private TableView<MappingEntry> createFilesTableView() {
        TableView<MappingEntry> mappingInfoTableView = new TableView<>(FXCollections.observableArrayList());
        mappingInfoTableView.getStyleClass().addAll(CSS_CLASS_TABLEVIEW, CSS_CLASS_MAPPING_TABLEVIEW);

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

        return mappingInfoTableView;
    }

    private ChangeListener<String> handleResourceValueChange(ObservableList<MappingEntry> files, EditorContext editorContext) {
        int fieldRank = editorContext.getFieldRank();
        StringProperty errorMessageProperty = editorContext.getMappingContext().getErrorMessageProperty();
        BooleanProperty errorProperty = editorContext.getMappingContext().getErrorProperty();

        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            files.clear();

            BulkDatabaseMiner miner = editorContext.getMiner();
            String gameLocation = editorContext.getGameLocation();
            String resourceValue = miner.getResourceEntryFromTopicAndReference(editorContext.getRemoteTopic(), newValue)
                    .flatMap(ResourceEntryDto::pickValue)
                    .orElse(VALUE_RESOURCE_DEFAULT);

            switch (editorContext.getCurrentTopic()) {
                case CAR_PHYSICS_DATA:
                    addCarPhysicsEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                case CAR_PACKS:
                    addCarPacksEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                case CAR_SHOPS:
                    addCarShopsEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                case CLOTHES:
                    addClothesEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                case HOUSES:
                    addHousesEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                case RIMS:
                    addRimsEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                case TUTORIALS:
                    addTutorialsEntries(files, fieldRank, gameLocation, resourceValue, editorContext);
                    break;
                default:
            }

            boolean isRegistrationFailure = files.stream().anyMatch(entry -> !entry.isRegistered());
            errorProperty.setValue(isRegistrationFailure);
            errorMessageProperty.setValue(isRegistrationFailure ? LABEL_ERROR_TOOLTIP_UNREGISTERED : "");
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

    private void addTutorialsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_VOICE_FILE == fieldRank) {
            MappingEntry tutoMappingEntry = createMappingEntry(resourceValue, TUTO_INSTRUCTION, gameLocation, editorContext);
            files.add(tutoMappingEntry);
        }
    }

    private void addRimsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_RSC_FILE_NAME_FRONT == fieldRank) {
            MappingEntry frontMappingEntry = createMappingEntry(resourceValue, FRONT_RIMS_3D, gameLocation, editorContext);
            files.add(frontMappingEntry);
        } else if (FIELD_RANK_RSC_FILE_NAME_REAR == fieldRank) {
            MappingEntry rearMappingEntry = createMappingEntry(resourceValue, REAR_RIMS_3D, gameLocation, editorContext);
            files.add(rearMappingEntry);
        }
    }

    private void addClothesEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_FURNITURE_FILE == fieldRank) {
            MappingEntry clothesMappingEntry = createMappingEntry(resourceValue, CLOTHES_3D, gameLocation, editorContext);
            files.add(clothesMappingEntry);
        }
    }

    private void addCarShopsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_DEALER_NAME == fieldRank) {
            MappingEntry shopExtMappingEntry = createMappingEntry(resourceValue, SHOP_EXT_3D, gameLocation, editorContext);
            MappingEntry shopIntMappingEntry = createMappingEntry(resourceValue, SHOP_INT_3D, gameLocation, editorContext);
            MappingEntry shopThumbMappingEntry = createMappingEntry(resourceValue, SPOT_MAP_SCREEN, gameLocation, editorContext);
            files.addAll(shopExtMappingEntry, shopIntMappingEntry, shopThumbMappingEntry);
        }
    }

    private void addHousesEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_SPOT_NAME == fieldRank) {
            MappingEntry houseExtMappingEntry = createMappingEntry(resourceValue, HOUSE_EXT_3D, gameLocation, editorContext);
            MappingEntry houseLoungeMappingEntry = createMappingEntry(resourceValue, HOUSE_LOUNGE_3D, gameLocation, editorContext);
            MappingEntry houseGarageMappingEntry = createMappingEntry(resourceValue, HOUSE_GARAGE_3D, gameLocation, editorContext);
            MappingEntry thumbMappingEntry = createMappingEntry(resourceValue, SPOT_MAP_SCREEN, gameLocation, editorContext);
            files.addAll(houseExtMappingEntry, houseLoungeMappingEntry, houseGarageMappingEntry, thumbMappingEntry);
        } else if (FIELD_RANK_REALTOR == fieldRank) {
            MappingEntry realtorExtMappingEntry = createMappingEntry(resourceValue, REALTOR_EXT_3D, gameLocation, editorContext);
            MappingEntry realtorIntMappingEntry = createMappingEntry(resourceValue, REALTOR_INT_3D, gameLocation, editorContext);
            MappingEntry thumbMappingEntry = createMappingEntry(resourceValue, SPOT_MAP_SCREEN, gameLocation, editorContext);
            files.addAll(realtorExtMappingEntry, realtorIntMappingEntry,  thumbMappingEntry);
        }
    }

    private void addCarPacksEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_CAR_FILE_NAME_SWAP == fieldRank) {
            MappingEntry extMappingEntry = createMappingEntry(resourceValue, EXT_3D, gameLocation, editorContext);
            files.add(extMappingEntry);
        }
    }

    private void addCarPhysicsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue, EditorContext editorContext) {
        if (FIELD_RANK_CAR_FILE_NAME == fieldRank) {
            MappingEntry extMappingEntry = createMappingEntry(resourceValue, EXT_3D, gameLocation, editorContext);
            MappingEntry intMappingEntry = createMappingEntry(resourceValue, INT_3D, gameLocation, editorContext);
            MappingEntry sndMappingEntry = createMappingEntry(resourceValue, SOUND, gameLocation, editorContext);
            files.addAll(extMappingEntry, intMappingEntry, sndMappingEntry);
        } else if (FIELD_RANK_HUD_FILE_NAME == fieldRank) {
            MappingEntry lgeMappingEntry = createMappingEntry(resourceValue, HUD_LOW, gameLocation, editorContext);
            MappingEntry hgeMappingEntry = createMappingEntry(resourceValue, HUD_HIGH, gameLocation, editorContext);
            files.addAll(lgeMappingEntry, hgeMappingEntry);
        }
    }

    private String resolveRimDirectoryName(EditorContext context) {
        int entryId = context.getMainStageController().getCurrentEntryIndex();
        DbDto.Topic currentTopic = context.getCurrentTopic();
        BulkDatabaseMiner miner = context.getMiner();
        ContentEntryDto contentEntry = miner.getContentEntryFromTopicWithInternalIdentifier(entryId, currentTopic)
                .orElseThrow(() -> new IllegalStateException("No content entry for identifier: " + entryId));
        String directoryRef = contentEntry.getItemAtRank(FIELD_RANK_RSC_PATH)
                .map(ContentItemDto::getRawValue)
                .orElseThrow(() -> new IllegalStateException("No content item for directory name"));
        return miner.getResourceEntryFromTopicAndReference(currentTopic, directoryRef)
                .flatMap(ResourceEntryDto::pickValue)
                .orElseThrow(() -> new IllegalStateException("No resource value for ref: " + directoryRef));
    }

    private String resolveClothesBrandDirectoryName(EditorContext context) {
        int entryId = context.getMainStageController().getCurrentEntryIndex();
        DbDto.Topic currentTopic = context.getCurrentTopic();
        BulkDatabaseMiner miner = context.getMiner();
        ContentEntryDto clothesContentEntry = miner.getContentEntryFromTopicWithInternalIdentifier(entryId, currentTopic)
                .orElseThrow(() -> new IllegalStateException("No content entry for identifier: " + entryId));
        String brandRef = clothesContentEntry.getItemAtRank(FIELD_RANK_FURNITURE_BRAND)
                .map(ContentItemDto::getRawValue)
                .orElseThrow(() -> new IllegalStateException("No content item for brand reference"));
        ContentEntryDto brandsContentEntry = miner.getContentEntryFromTopicWithReference(brandRef, BRANDS)
                .orElseThrow(() -> new IllegalStateException("No brand content entry for ref: " + brandRef));
        return brandsContentEntry.getItemAtRank(FIELD_RANK_MANUFACTURER_ID)
                .flatMap(item -> miner.getResourceEntryFromTopicAndReference(BRANDS, item.getRawValue()))
                .flatMap(ResourceEntryDto::pickValue)
                .orElseThrow(() -> new IllegalStateException("No manufacturer id available"));
    }
}

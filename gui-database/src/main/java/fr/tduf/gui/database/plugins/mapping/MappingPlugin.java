package fr.tduf.gui.database.plugins.mapping;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.common.javafx.helper.DesktopHelper;
import fr.tduf.gui.database.plugins.cameras.common.FxConstants;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
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
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.*;
import static java.util.Collections.singletonList;
import static javafx.geometry.Orientation.VERTICAL;

/**
 * File mapping status plugin
 */
public class MappingPlugin implements DatabasePlugin {
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
            Log.warn(THIS_CLASS_NAME, "No bnk1.map file was found under game directory: " + gameLocation);
            return;
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

    MappingEntry createMappingEntry(String resourceValue, MappedFileKind kind, String gameLocation) {
        String fileName = String.format(kind.getFileNameFormat(), resourceValue);
        int lastPartIndex = fileName.lastIndexOf("_");
        int dotIndex = fileName.lastIndexOf(".");
        String brandName;
        Path filePath;
        
        switch(kind) {
            case FRONT_RIMS_3D:
            case REAR_RIMS_3D:
                // FIXME get real brand name
                brandName = "AC";
                filePath = kind.getParentPath().resolve(brandName).resolve(fileName);
                break;
            case SHOP_3D:
                // FIXME check location
                filePath = kind.getParentPath().resolve(fileName.substring(0, lastPartIndex) + fileName.substring(dotIndex));
                break;
            case CLOTHES_3D:
                // FIXME check location and find reliable way to extract brand
                brandName = fileName.substring(lastPartIndex + 1, dotIndex);
                filePath = kind.getParentPath().resolve(brandName).resolve(fileName);
                break;
            default:
                filePath = kind.getParentPath().resolve(fileName);
        }

        boolean exists = Files.exists(resolveBankFilePath(gameLocation, filePath.toString()));
        boolean registered = MapHelper.hasEntryForPath(bankMapProperty.getValue(), filePath.toString());
        return new MappingEntry(kind.getDescription(), filePath.toString(), exists, registered);
    }

    private VBox createMainColumn(EditorContext context, TableView<MappingEntry> filesTableView) {
        getEntries(context, filesTableView.getItems());

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(FxConstants.CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();
        
        mainColumnChildren.add(filesTableView);
        return mainColumnBox;
    }

    private VBox createButtonColumn(EditorContext context, TableView.TableViewSelectionModel<MappingEntry> selectionModel) {
        VBox buttonColumnBox = new VBox();
        buttonColumnBox.getStyleClass().add(fr.tduf.gui.database.common.FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX);

        Button browseResourceButton = new Button(LABEL_BUTTON_BROWSE);
        browseResourceButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(browseResourceButton, TOOLTIP_BUTTON_BROWSE_RESOURCES);
        browseResourceButton.setOnAction(
                context.getMainStageController().handleBrowseResourcesButtonMouseClick(context.getRemoteTopic(), context.getRawValueProperty(), context.getFieldRank()));
       
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

    private void getEntries(EditorContext context, ObservableList<MappingEntry> files) {
        int fieldRank = context.getFieldRank();
        DbDto.Topic currentTopic = context.getCurrentTopic();
        DbDto.Topic remoteTopic = context.getRemoteTopic();
        BulkDatabaseMiner miner = context.getMiner();
        String gameLocation = context.getGameLocation();

        context.getRawValueProperty().addListener(handleResourceValueChange(files, fieldRank, currentTopic, remoteTopic, miner, gameLocation));
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
//        registeredColumn.setCellFactory(forTableColumn());

        ObservableList<TableColumn<MappingEntry, ?>> columns = mappingInfoTableView.getColumns();
        columns.add(kindColumn);
        columns.add(pathColumn);
        columns.add(existsColumn);
        columns.add(registeredColumn);

        return mappingInfoTableView;
    }

    private ChangeListener<String> handleResourceValueChange(ObservableList<MappingEntry> files, int fieldRank, DbDto.Topic currentTopic, DbDto.Topic remoteTopic, BulkDatabaseMiner miner, String gameLocation) {
        return (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            files.clear();

            String resourceValue = miner.getResourceEntryFromTopicAndReference(remoteTopic, newValue)
                    .flatMap(ResourceEntryDto::pickValue)
                    .orElse(VALUE_RESOURCE_DEFAULT);

            switch (currentTopic) {
                case CAR_PHYSICS_DATA:
                    addCarPhysicsEntries(files, fieldRank, gameLocation, resourceValue);
                    break;
                case CAR_PACKS:
                    addCarPacksEntries(files, fieldRank, gameLocation, resourceValue);
                    break;
                case CAR_SHOPS:
                    addCarShopsEntries(files, fieldRank, gameLocation, resourceValue);
                    break;
                case CLOTHES:
                    addClothesEntries(files, fieldRank, gameLocation, resourceValue);
                    break;
                case RIMS:
                    addRimsEntries(files, fieldRank, gameLocation, resourceValue);
                    break;
                case TUTORIALS:
                    addTutorialsEntries(files, fieldRank, gameLocation, resourceValue);
                    break;
                default:
            }
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
    
    // TODO create and use Database constants
    
    private void addTutorialsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue) {
        if (4 == fieldRank) {
            MappingEntry tutoMappingEntry = createMappingEntry(resourceValue, TUTO_INSTRUCTION, gameLocation);
            files.add(tutoMappingEntry);
        }
    }

    private void addRimsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue) {
        if (14 == fieldRank) {
            MappingEntry frontMappingEntry = createMappingEntry(resourceValue, FRONT_RIMS_3D, gameLocation);
            files.add(frontMappingEntry);
        } else if (15 == fieldRank) {
            MappingEntry rearMappingEntry = createMappingEntry(resourceValue, REAR_RIMS_3D, gameLocation);
            files.add(rearMappingEntry);
        }
    }

    private void addClothesEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue) {
        if (2 == fieldRank) {
            MappingEntry clothesMappingEntry = createMappingEntry(resourceValue, CLOTHES_3D, gameLocation);
            files.add(clothesMappingEntry);
        }
    }

    private void addCarShopsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue) {
        if (2 == fieldRank) {
            MappingEntry shopMappingEntry = createMappingEntry(resourceValue, SHOP_3D, gameLocation);
            files.add(shopMappingEntry);
        }
    }

    private void addCarPacksEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue) {
        if (3 == fieldRank) {
            MappingEntry extMappingEntry = createMappingEntry(resourceValue, EXT_3D, gameLocation);
            files.add(extMappingEntry);
        }
    }

    private void addCarPhysicsEntries(ObservableList<MappingEntry> files, int fieldRank, String gameLocation, String resourceValue) {
        if (9 == fieldRank) {
            MappingEntry extMappingEntry = createMappingEntry(resourceValue, EXT_3D, gameLocation);
            MappingEntry intMappingEntry = createMappingEntry(resourceValue, INT_3D, gameLocation);
            MappingEntry sndMappingEntry = createMappingEntry(resourceValue, SOUND, gameLocation);
            files.addAll(extMappingEntry, intMappingEntry, sndMappingEntry);
        } else if (11 == fieldRank) {
            MappingEntry lgeMappingEntry = createMappingEntry(resourceValue, HUD_LOW, gameLocation);
            MappingEntry hgeMappingEntry = createMappingEntry(resourceValue, HUD_HIGH, gameLocation);
            files.addAll(lgeMappingEntry, hgeMappingEntry);
        }
    }
}

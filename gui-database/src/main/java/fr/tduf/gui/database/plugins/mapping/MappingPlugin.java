package fr.tduf.gui.database.plugins.mapping;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.cameras.common.FxConstants;
import fr.tduf.gui.database.plugins.common.DatabasePlugin;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_PLUGIN_BOX;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_TABLEVIEW;
import static fr.tduf.gui.database.plugins.mapping.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.mapping.common.FxConstants.*;
import static java.util.Collections.singletonList;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;

/**
 * File mapping status plugin
 */
public class MappingPlugin implements DatabasePlugin {
    private static final Class<MappingPlugin> thisClass = MappingPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private final Property<BankMap> bankMapProperty = new SimpleObjectProperty<>();

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

        BankMap bankMap = MapParser.load(mappingFile.toString()).parse();
        bankMapProperty.setValue(bankMap);

        Log.info(THIS_CLASS_NAME, "Mapping info loaded");        
        mappingContext.setPluginLoaded(true);        
    }

    @Override
    public void onSave(EditorContext context) throws IOException {

    }

    @Override
    public Node renderControls(EditorContext context) {
        HBox hBox = new HBox();
        hBox.getStyleClass().add(CSS_CLASS_PLUGIN_BOX);

        if (!context.getMappingContext().isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Mapping plugin not loaded, no rendering will be performed");
            return hBox;
        }
        
        VBox mainColumnBox = createMainColumn(context);
        VBox buttonColumnBox = createButtonColumn();
        
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

    private VBox createMainColumn(EditorContext context) {
        ObservableList<MappingEntry> files = FXCollections.observableArrayList();
        files.addAll(getEntries(context));

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(FxConstants.CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        TableView<MappingEntry> filesTableView = createFilesTableView(files);

        mainColumnChildren.add(filesTableView);
        return mainColumnBox;
    }

    private List<MappingEntry> getEntries(EditorContext context) {
        List<MappingEntry> mappingEntries = new ArrayList<>();
        int fieldRank = context.getFieldRank();
        String fileName = context.getMiner().getResourceEntryFromTopicAndReference(context.getCurrentTopic(), context.getRawValueProperty().get())
                .flatMap(ResourceEntryDto::pickValue)
                .orElse("??"); // TODO use global constant?

        switch (context.getCurrentTopic()) {
            case CAR_PHYSICS_DATA:
                // TODO create and use Database constants
                if (9 == fieldRank) {
                    // TODO generate file names and query mapping system
                    MappingEntry extMappingEntry = new MappingEntry("Exterior 3D model", fileName, false, false);
                    MappingEntry intMappingEntry = new MappingEntry("Interior 3D model", fileName, false, false);
                    MappingEntry sndMappingEntry = new MappingEntry("Engine sound", fileName, false, false);
                    mappingEntries.add(extMappingEntry);
                    mappingEntries.add(intMappingEntry);
                    mappingEntries.add(sndMappingEntry);
                } else if (11 == fieldRank) {
                    MappingEntry lgeMappingEntry = new MappingEntry("Gauges (low-resolution)", fileName, false, false);
                    MappingEntry hgeMappingEntry = new MappingEntry("Gauges (high-resolution)", fileName, false, false);
                    mappingEntries.add(lgeMappingEntry);
                    mappingEntries.add(hgeMappingEntry);
                }
                break;
                // TODO other topics
            default:
        }
        
        return mappingEntries;
    }

    private VBox createButtonColumn() {
        VBox buttonColumnBox = new VBox();
        buttonColumnBox.getStyleClass().add(fr.tduf.gui.database.common.FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX);

        return buttonColumnBox;
    }

    private TableView<MappingEntry> createFilesTableView(ObservableList<MappingEntry> files) {
        TableView<MappingEntry> mappingInfoTableView = new TableView<>(files);
        mappingInfoTableView.getStyleClass().addAll(CSS_CLASS_TABLEVIEW, CSS_CLASS_MAPPING_TABLEVIEW);

        TableColumn<MappingEntry, String> kindColumn = new TableColumn<>(HEADER_FILESTABLE_KIND);
        kindColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getKind()));
                
        TableColumn<MappingEntry, String> pathColumn = new TableColumn<>(HEADER_FILESTABLE_PATH);
        pathColumn.getStyleClass().add(CSS_CLASS_PATH_TABLECOLUMN);
        pathColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getPath()));

        TableColumn<MappingEntry, String> existsColumn = new TableColumn<>(HEADER_FILESTABLE_EXISTS);
        // TODO extract constants
        existsColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().isExists() ? "Y" : "N"));

        TableColumn<MappingEntry, String> registeredColumn = new TableColumn<>(HEADER_FILESTABLE_REGISTERED);
        registeredColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().isRegistered() ? "Y" : "N"));
        registeredColumn.setCellFactory(forTableColumn());

        ObservableList<TableColumn<MappingEntry, ?>> columns = mappingInfoTableView.getColumns();
        columns.add(kindColumn);
        columns.add(pathColumn);
        columns.add(existsColumn);
        columns.add(registeredColumn);

        return mappingInfoTableView;
    }
    
    private Path resolveMappingFilePath(String gameLocation) {
        return Paths.get(gameLocation, FileConstants.DIRECTORY_EURO, FileConstants.DIRECTORY_BANKS, MapHelper.MAPPING_FILE_NAME);
    }    
}

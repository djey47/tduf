package fr.tduf.gui.database.plugins.materials;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginComponentBuilders;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.common.contexts.PluginContext;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToItemConverter;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToRawValueConverter;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.helper.MaterialsHelper;
import fr.tduf.libunlimited.low.files.gfx.materials.rw.MaterialsParser;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_ITEM_LABEL;
import static fr.tduf.gui.database.plugins.common.PluginComponentBuilders.createBrowseResourceButton;
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.FORMAT_MESSAGE_WARN_NO_MATERIALS;
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.LABEL_AVAILABLE_MATERIALS;
import static fr.tduf.gui.database.plugins.materials.common.FxConstants.*;
import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static javafx.geometry.Orientation.VERTICAL;

public class MaterialsPlugin extends AbstractDatabasePlugin {
    private static final Class<MaterialsPlugin> thisClass = MaterialsPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private static final String FILE_COLORS_BANK = "colors.bnk";

    private final PluginContext materialsContext = new PluginContext();
    private final Property<MaterialDefs> materialsInfoEnhancedProperty = new SimpleObjectProperty<>();
    private Map<String, String> normalizedNamesDictionary;

    /**
     * Required contextual information:
     * - gameLocation
     * - mainStageController (for bank support)
     *
     * @param pluginName : name of plugin to initialize
     * @param context : all required information about Database Editor
     * @throws IOException when cameras file can't be parsed for some reason
     */
    @Override
    public void onInit(String pluginName, EditorContext context) throws IOException {
        super.onInit(pluginName, context);

        materialsContext.reset();

        // Loads colors.2dm in colors.bnk
        String gameLocation = context.getGameLocation();

        // Extracts all files from colors.bnk
        Path bankFilePath = resolveBankFilePath(gameLocation);
        if (!Files.exists(bankFilePath)) {
            String warningMessage = String.format(FORMAT_MESSAGE_WARN_NO_MATERIALS, bankFilePath.toString());
            Log.warn(THIS_CLASS_NAME, warningMessage);

            throw new IOException(warningMessage);
        }

        String extractedDirectory = createTempDirectory();
        Log.debug(THIS_CLASS_NAME, String.format("Extracting materials info from %s to %s...", bankFilePath, extractedDirectory));
        context.getMainStageController().getBankSupport().extractAll(bankFilePath.toString(), extractedDirectory);

        // Resolves material info binary file
        String binaryFileLocation = resolveColorsFileLocation(extractedDirectory).toString();
        Log.debug(THIS_CLASS_NAME, String.format("Materials binary location: %s", binaryFileLocation));
        materialsContext.setBinaryFileLocation(binaryFileLocation);

        // Loads binary file and create domain objects
        Log.info(THIS_CLASS_NAME, String.format("Loading materials info from %s...", binaryFileLocation));
        MaterialsParser materialsParser = MaterialsParser.load(getMaterialsInputStream(binaryFileLocation));
        MaterialDefs materialDefs = materialsParser.parse();

        materialsInfoEnhancedProperty.setValue(materialDefs);

        Log.info(THIS_CLASS_NAME, String.format("Material definitions loaded, %d materials available", materialDefs.getMaterials().size()));

        normalizedNamesDictionary = MaterialsHelper.buildNormalizedDictionary(context.getMiner());

        materialsContext.setPluginLoaded(true);
    }

    @Override
    public void onSave() {
        // Not implemented
    }

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        HBox hBox = new HBox();
        if (!materialsContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Materials plugin not loaded, no rendering will be performed");
            return hBox;
        }

        VBox mainColumnBox = createMainColumn(onTheFlyContext);
        VBox buttonColumnBox = createButtonColumn(onTheFlyContext);

        ObservableList<Node> mainRowChildren = hBox.getChildren();
        mainRowChildren.add(mainColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));
        mainRowChildren.add(buttonColumnBox);
        mainRowChildren.add(new Separator(VERTICAL));

        return hBox;
    }

    private VBox createButtonColumn(OnTheFlyContext onTheFlyContext) {
        return PluginComponentBuilders.buttonColumn()
            .withButton(createBrowseResourceButton(getEditorContext(), onTheFlyContext))
            .build();
    }

    private VBox createMainColumn(OnTheFlyContext onTheFlyContext) {
        ObservableList<Material> materialInfos = FXCollections.observableArrayList(materialsInfoEnhancedProperty.getValue().getMaterials());
        ComboBox<Material> materialSelectorComboBox = new ComboBox<>(materialInfos.sorted(comparing(Material::getName)));

        materialSelectorComboBox.getStyleClass().add(CSS_CLASS_MAT_SELECTOR_COMBOBOX);
        materialSelectorComboBox.setConverter(new MaterialToItemConverter());

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox camSelectorBox = createMaterialSelectorBox(materialSelectorComboBox, onTheFlyContext);

        mainColumnChildren.add(camSelectorBox);

        return mainColumnBox;
    }

    private HBox createMaterialSelectorBox(ComboBox<Material> materialSelectorComboBox, OnTheFlyContext onTheFlyContext) {
        HBox matSelectorBox = new HBox();

        matSelectorBox.getStyleClass().add(CSS_CLASS_MAT_SELECTOR_BOX);

        materialSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getMaterialSelectorChangeListener());
        Bindings.bindBidirectional(
                onTheFlyContext.getRawValueProperty(), materialSelectorComboBox.valueProperty(), new MaterialToRawValueConverter(this.materialsInfoEnhancedProperty.getValue(), getEditorContext(), onTheFlyContext.getCurrentTopic(), normalizedNamesDictionary));

        Label availableMaterialsLabel = new Label(LABEL_AVAILABLE_MATERIALS);
        availableMaterialsLabel.setLabelFor(materialSelectorComboBox);
        availableMaterialsLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        matSelectorBox.getChildren().add(availableMaterialsLabel);
        matSelectorBox.getChildren().add(materialSelectorComboBox);

        return matSelectorBox;
    }

    private ChangeListener<Material> getMaterialSelectorChangeListener() {
        return (ObservableValue<? extends Material> observable, Material oldValue, Material newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
        };
    }

    @Override
    public Set<String> getCss() {
        return new HashSet<>(singletonList(thisClass.getResource(PATH_RESOURCE_CSS_MATERIALS).toExternalForm()));
    }

    private static Path resolveBankFilePath(String gameLocation) {
        return Paths.get(gameLocation, DIRECTORY_EURO, DIRECTORY_BANKS, DIRECTORY_VEHICLES, FILE_COLORS_BANK);
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("tduf-materials-colorsBank").toString();
    }

    private static Path resolveColorsFileLocation(String extractedDirectory) {
        return Paths.get(extractedDirectory, "4Build", "PC", "EURO", "Vehicules", "common", "common_mesh", "shader", "Colors.2DM");
    }

    private static XByteArrayInputStream getMaterialsInputStream(String sourceMaterialsFile) throws IOException {
        return new XByteArrayInputStream(readAllBytes(Paths.get(sourceMaterialsFile)));
    }
}

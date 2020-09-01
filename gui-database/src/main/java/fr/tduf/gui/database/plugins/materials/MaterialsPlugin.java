package fr.tduf.gui.database.plugins.materials;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginComponentBuilders;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.common.contexts.PluginContext;
import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.gui.database.plugins.materials.common.FxConstants;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToItemConverter;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToRawValueConverter;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.framework.lang.UByte;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialSettings;
import fr.tduf.libunlimited.low.files.gfx.materials.helper.MaterialsHelper;
import fr.tduf.libunlimited.low.files.gfx.materials.rw.MaterialsParser;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.materials.common.FxConstants.*;
import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.geometry.Orientation.HORIZONTAL;
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
                .withSeparator()
                .withButton(createLayersInfoButton())
                .withButton(createShaderSettingsButton())
                .build();
    }

    private Button createLayersInfoButton() {
        Button button = new Button(LABEL_BUTTON_LAYERS);
        button.getStyleClass().addAll(CSS_CLASS_LAYERS_INFO_BUTTON, fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_BUTTON_MEDIUM);
        return button;
    }

    private Button createShaderSettingsButton() {
        Button button = new Button(LABEL_BUTTON_SHADER);
        button.getStyleClass().addAll(CSS_CLASS_SHADER_SETTINGS_BUTTON, fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_BUTTON_MEDIUM);
        return button;
    }

    private VBox createMainColumn(OnTheFlyContext onTheFlyContext) {
        ObservableList<Material> materialInfos = FXCollections.observableArrayList(materialsInfoEnhancedProperty.getValue().getMaterials());
        ComboBox<Material> materialSelectorComboBox = new ComboBox<>(materialInfos.sorted(comparing(Material::getName)));

        materialSelectorComboBox.getStyleClass().add(CSS_CLASS_MAT_SELECTOR_COMBOBOX);
        materialSelectorComboBox.setConverter(new MaterialToItemConverter(normalizedNamesDictionary));

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(CSS_CLASS_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox matSelectorBox = createMaterialSelectorBox(materialSelectorComboBox, onTheFlyContext);
        VBox matSettingsBox = createMaterialSettingsBox(onTheFlyContext);

        mainColumnChildren.add(matSelectorBox);
        mainColumnChildren.add(matSettingsBox);

        return mainColumnBox;
    }

    private VBox createMaterialSettingsBox(OnTheFlyContext onTheFlyContext) {
        VBox matSettingsBox = new VBox();

        matSettingsBox.getStyleClass().add(CSS_CLASS_MAT_SETTINGS_BOX);

        VBox colorsBox = createColorsBox((OnTheFlyMaterialsContext) onTheFlyContext);
        VBox propertiesBox = createPropertiesBox((OnTheFlyMaterialsContext) onTheFlyContext);

        matSettingsBox.getChildren().addAll(
                colorsBox,
                new Separator(HORIZONTAL),
                propertiesBox,
                new Separator(HORIZONTAL)
        );

        return matSettingsBox;
    }

    private VBox createPropertiesBox(OnTheFlyMaterialsContext onTheFlyContext) {
        VBox propertiesBox = new VBox();

        propertiesBox.getStyleClass().addAll(CSS_CLASS_PROPERTIES_BOX);

        propertiesBox.getChildren().addAll(
            createShaderBox(onTheFlyContext)
        );

        return propertiesBox;
    }

    private HBox createShaderBox(OnTheFlyMaterialsContext onTheFlyContext) {
        HBox shaderBox = new HBox();

        shaderBox.getStyleClass().addAll(CSS_CLASS_SHADER_BOX);

        Label shaderValueLabel = new Label();
        shaderValueLabel.textProperty().bind(createShaderValueBinding(onTheFlyContext.getCurrentMaterialProperty()));

        shaderBox.getChildren().addAll(
                new Label(DisplayConstants.LABEL_SHADER),
                shaderValueLabel
        );

        return shaderBox;
    }

    private HBox createAlphaBox(OnTheFlyMaterialsContext onTheFlyContext) {
        HBox shaderBox = new HBox();

        shaderBox.getStyleClass().addAll(CSS_CLASS_ALPHA_BOX);

        Label alphaValueLabel = new Label();
        alphaValueLabel.textProperty().bind(createAlphaValueBinding(onTheFlyContext.getCurrentMaterialProperty()));

        shaderBox.getChildren().addAll(
                new Label(LABEL_ALPHA),
                alphaValueLabel
        );

        return shaderBox;
    }

    private VBox createColorsBox(OnTheFlyMaterialsContext onTheFlyContext) {
        VBox colorsBox = new VBox();

        colorsBox.getStyleClass().addAll(CSS_CLASS_COLORS_BOX);

        colorsBox.getChildren().addAll(
                createColorBox(LABEL_COLOR_AMBIENT, onTheFlyContext),
                createColorBox(LABEL_COLOR_DIFFUSE, onTheFlyContext),
                createColorBox(LABEL_COLOR_SPECULAR, onTheFlyContext),
                createColorBox(LABEL_COLOR_OTHER, onTheFlyContext),
                createAlphaBox(onTheFlyContext)
        );

        return colorsBox;
    }

    private HBox createColorBox(String label, OnTheFlyMaterialsContext onTheFlyMaterialsContext) {
        HBox colorBox = new HBox();
        colorBox.getStyleClass().add(CSS_CLASS_COLOR_BOX);

        Label colorTypeLabel = new Label(label);
        colorTypeLabel.getStyleClass().add(CSS_CLASS_COLOR_TYPE_LABEL);

        Label colorDescriptionLabel = new Label();
        colorDescriptionLabel.getStyleClass().add(CSS_CLASS_COLOR_DESC_LABEL);
        colorDescriptionLabel.textProperty().bind(createDescriptionBinding(onTheFlyMaterialsContext, label));

        HBox colorPreviewField = new HBox();
        colorPreviewField.getStyleClass().add(CSS_CLASS_COLOR_PREVIEW_BOX);
        colorPreviewField.backgroundProperty().bind(createBackgroundBinding(onTheFlyMaterialsContext, label));

        colorBox.getChildren().addAll(
                colorTypeLabel,
                colorPreviewField,
                colorDescriptionLabel
        );

        return colorBox;
    }

    private ObjectBinding<Background> createBackgroundBinding(OnTheFlyMaterialsContext onTheFlyMaterialsContext, String label) {
        return createObjectBinding(() -> {
            Material currentMaterial = onTheFlyMaterialsContext.getCurrentMaterialProperty().getValue();
            if (currentMaterial == null) {
                return Background.EMPTY;
            }
            MaterialSettings materialSettings = currentMaterial.getProperties();
            fr.tduf.libunlimited.low.files.gfx.materials.domain.Color currentColor = retrieveColorFromSettings(label, materialSettings);
            Color backColor = Color.color(
                    currentColor.getRedCompound(),
                    currentColor.getGreenCompound(),
                    currentColor.getBlueCompound(),
                    currentColor.getOpacity());
            BackgroundFill fill = new BackgroundFill(backColor, CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(fill);
        }, onTheFlyMaterialsContext.getCurrentMaterialProperty());
    }

    private ObjectBinding<String> createDescriptionBinding(OnTheFlyMaterialsContext onTheFlyMaterialsContext, String label) {
        return createObjectBinding(() -> {
            Material currentMaterial = onTheFlyMaterialsContext.getCurrentMaterialProperty().getValue();
            if (currentMaterial == null) {
                return "";
            }
            MaterialSettings materialSettings = currentMaterial.getProperties();
            fr.tduf.libunlimited.low.files.gfx.materials.domain.Color currentColor = retrieveColorFromSettings(label, materialSettings);
            return String.format(FORMAT_COLOR_DESCRIPTION, currentColor.getRedCompound(), currentColor.getGreenCompound(), currentColor.getBlueCompound(), currentColor.getOpacity());
        }, onTheFlyMaterialsContext.getCurrentMaterialProperty());
    }

    private ObjectBinding<String> createShaderValueBinding(Property<Material> currentMaterialProperty) {
        return createObjectBinding(() -> {
            Material currentMaterial = currentMaterialProperty.getValue();
            if (currentMaterial == null) {
                return "";
            }
            MaterialSettings materialSettings = currentMaterial.getProperties();
            return String.format(FORMAT_SHADER_VALUE, materialSettings.getShader().getConfiguration().getName(), materialSettings.getShader().getConfiguration().name());
        }, currentMaterialProperty);
    }

    private ObjectBinding<String> createAlphaValueBinding(Property<Material> currentMaterialProperty) {
        return createObjectBinding(() -> {
            Material currentMaterial = currentMaterialProperty.getValue();
            if (currentMaterial == null) {
                return "";
            }
            MaterialSettings materialSettings = currentMaterial.getProperties();
            UByte[] alphaBlending = materialSettings.getAlphaBlending();
            return String.format(FORMAT_ALPHA_VALUE, materialSettings.getAlpha().get(), alphaBlending[0].getSigned(), alphaBlending[1].getSigned());
        }, currentMaterialProperty);
    }


    private HBox createMaterialSelectorBox(ComboBox<Material> materialSelectorComboBox, OnTheFlyContext onTheFlyContext) {
        HBox matSelectorBox = new HBox();

        matSelectorBox.getStyleClass().add(CSS_CLASS_MAT_SELECTOR_BOX);

        materialSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getMaterialSelectorChangeListener((OnTheFlyMaterialsContext) onTheFlyContext));
        Bindings.bindBidirectional(
                onTheFlyContext.getRawValueProperty(), materialSelectorComboBox.valueProperty(), new MaterialToRawValueConverter(this.materialsInfoEnhancedProperty.getValue(), getEditorContext(), onTheFlyContext.getCurrentTopic(), normalizedNamesDictionary));

        Label availableMaterialsLabel = new Label(LABEL_AVAILABLE_MATERIALS);
        availableMaterialsLabel.setLabelFor(materialSelectorComboBox);
        availableMaterialsLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        matSelectorBox.getChildren().add(availableMaterialsLabel);
        matSelectorBox.getChildren().add(materialSelectorComboBox);

        return matSelectorBox;
    }

    private ChangeListener<Material> getMaterialSelectorChangeListener(OnTheFlyMaterialsContext onTheFlyContext) {
        return (ObservableValue<? extends Material> observable, Material oldValue, Material newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            onTheFlyContext.setCurrentMaterial(newValue);
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

    private static fr.tduf.libunlimited.low.files.gfx.materials.domain.Color retrieveColorFromSettings(String label, MaterialSettings materialSettings) {
        switch (label) {
            case LABEL_COLOR_AMBIENT:
                return materialSettings.getAmbientColor();
            case LABEL_COLOR_DIFFUSE:
                return materialSettings.getDiffuseColor();
            case LABEL_COLOR_OTHER:
                return materialSettings.getOtherColor();
            case LABEL_COLOR_SPECULAR:
                return materialSettings.getSpecularColor();
            default:
                return fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.builder()
                        .fromRGB(0.0f, 0.0f, 0.0f)
                        .build();
        }
    }
}

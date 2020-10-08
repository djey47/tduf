package fr.tduf.gui.database.plugins.materials;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import fr.tduf.gui.database.controllers.main.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.PluginComponentBuilders;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.common.contexts.PluginContext;
import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToItemConverter;
import fr.tduf.gui.database.plugins.materials.converter.MaterialToRawValueConverter;
import fr.tduf.gui.database.plugins.materials.converter.ShaderToItemConverter;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.framework.lang.UByte;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialPiece;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialSettings;
import fr.tduf.libunlimited.low.files.gfx.materials.helper.MaterialsHelper;
import fr.tduf.libunlimited.low.files.gfx.materials.rw.MaterialsParser;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.*;

import static fr.tduf.gui.database.common.FxConstants.CSS_CLASS_BUTTON;
import static fr.tduf.gui.database.common.FxConstants.CSS_CLASS_COMBOBOX;
import static fr.tduf.gui.database.plugins.common.FxConstants.*;
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.*;
import static fr.tduf.gui.database.plugins.materials.common.FxConstants.*;
import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static fr.tduf.libunlimited.framework.fx.Bindings.bindBidirectional;
import static fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind.*;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.OK;

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
        Path bankFilePath = resolveAndCheckBankFilePath(gameLocation);

        String extractedDirectory = createTempDirectory();
        Log.debug(THIS_CLASS_NAME, String.format("Extracting materials info from %s to %s...", bankFilePath, extractedDirectory));
        context.getMainStageController().getBankSupport().extractAll(bankFilePath.toString(), extractedDirectory);

        // Resolves material info binary file
        String binaryFileLocation = resolveColorsFileLocation(extractedDirectory).toString();
        Log.debug(THIS_CLASS_NAME, String.format("Materials binary location: %s", binaryFileLocation));
        materialsContext.setBinaryFileLocation(binaryFileLocation);
        materialsContext.setBankExtractedDirectory(extractedDirectory);

        // Loads binary file and create domain objects
        Log.debug(THIS_CLASS_NAME, String.format("Loading materials info from %s...", binaryFileLocation));
        MaterialsParser materialsParser = MaterialsParser.load(getMaterialsInputStream(binaryFileLocation));
        MaterialDefs materialDefs = materialsParser.parse();

        materialsInfoEnhancedProperty.setValue(materialDefs);

        Log.info(THIS_CLASS_NAME, String.format("Material definitions loaded, %d materials available", materialDefs.getMaterials().size()));

        normalizedNamesDictionary = MaterialsHelper.buildNormalizedDictionary(context.getMiner());

        materialsContext.setPluginLoaded(true);
    }

    @Override
    public void onSave() throws IOException {
        if (!materialsContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Materials plugin not loaded, no saving will be performed");
            return;
        }

        String materialsFile = materialsContext.getBinaryFileLocation();
        Log.info(THIS_CLASS_NAME, "Saving material definitions to " + materialsFile);
        MaterialsHelper.saveMaterialDefinitions(materialsInfoEnhancedProperty.getValue(), materialsFile);

        // Repacking to Colors.bnk
        EditorContext context = getEditorContext();
        Path bankFilePath = resolveAndCheckBankFilePath(context.getGameLocation());
        String extractedDirectory = materialsContext.getBankExtractedDirectory();
        Log.debug(THIS_CLASS_NAME, String.format("Repacking materials info from %s to %s...", extractedDirectory, bankFilePath));
        context.getMainStageController().getBankSupport().packAll(extractedDirectory, bankFilePath.toString());
    }

    @Override
    public Set<String> getCss() {
        return new HashSet<>(singletonList(thisClass.getResource(PATH_RESOURCE_CSS_MATERIALS).toExternalForm()));
    }

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        HBox hBox = new HBox();
        if (!materialsContext.isPluginLoaded()) {
            Log.warn(THIS_CLASS_NAME, "Materials plugin not loaded, no rendering will be performed");
            return hBox;
        }

        hBox.getChildren().addAll(
                createMainColumn(onTheFlyContext),
                new Separator(VERTICAL),
                createButtonColumn(onTheFlyContext),
                new Separator(VERTICAL)
        );

        return hBox;
    }

    private VBox createButtonColumn(OnTheFlyContext onTheFlyContext) {
        return PluginComponentBuilders.buttonColumn()
                .withBrowseResourceButton(getEditorContext(), onTheFlyContext)
                .withSeparator()
                .withButton(createLayersInfoButton())
                .withButton(createShaderSettingsButton())
                .build();
    }

    private Button createLayersInfoButton() {
        Button button = new Button(LABEL_BUTTON_LAYERS);
        button.getStyleClass().addAll(CSS_CLASS_BUTTON, CSS_CLASS_PLUGIN_BUTTON, CSS_CLASS_PLUGIN_BUTTON_MEDIUM, CSS_CLASS_LAYERS_INFO_BUTTON);
        return button;
    }

    private Button createShaderSettingsButton() {
        Button button = new Button(LABEL_BUTTON_SHADER);
        button.getStyleClass().addAll(CSS_CLASS_BUTTON, CSS_CLASS_PLUGIN_BUTTON, CSS_CLASS_PLUGIN_BUTTON_MEDIUM, CSS_CLASS_SHADER_SETTINGS_BUTTON);
        return button;
    }

    private VBox createMainColumn(OnTheFlyContext onTheFlyContext) {
        ObservableList<Material> materialInfos = observableArrayList(materialsInfoEnhancedProperty.getValue().getMaterials());
        ComboBox<Material> materialSelectorComboBox = new ComboBox<>(materialInfos.sorted(comparing(Material::getName)));

        materialSelectorComboBox.getStyleClass().addAll(CSS_CLASS_COMBOBOX, CSS_CLASS_MAT_SELECTOR_COMBOBOX);
        materialSelectorComboBox.setConverter(new MaterialToItemConverter(this::getFullNameFromDictionary));

        ObservableList<MaterialPiece> availableShaders = observableArrayList(stream(MaterialPiece.values())
                .sorted()
                .collect(toList()));
        ComboBox<MaterialPiece> shaderSelectorComboBox = new ComboBox<>(availableShaders);

        shaderSelectorComboBox.getStyleClass().addAll(CSS_CLASS_COMBOBOX, CSS_CLASS_SHADER_SELECTOR_COMBOBOX);
        shaderSelectorComboBox.setConverter(new ShaderToItemConverter());

        VBox mainColumnBox = new VBox();
        mainColumnBox.getStyleClass().add(CSS_CLASS_MAT_MAIN_COLUMN);
        ObservableList<Node> mainColumnChildren = mainColumnBox.getChildren();

        HBox matSelectorBox = createMaterialSelectorBox(materialSelectorComboBox, onTheFlyContext);
        VBox matSettingsBox = createMaterialSettingsBox(shaderSelectorComboBox, onTheFlyContext);

        mainColumnChildren.add(matSelectorBox);
        mainColumnChildren.add(matSettingsBox);

        return mainColumnBox;
    }

    private VBox createMaterialSettingsBox(ComboBox<MaterialPiece> shaderSelectorComboBox, OnTheFlyContext onTheFlyContext) {
        VBox matSettingsBox = new VBox();

        matSettingsBox.getStyleClass().add(CSS_CLASS_MAT_SETTINGS_BOX);

        matSettingsBox.getChildren().addAll(
                createColorsBox((OnTheFlyMaterialsContext) onTheFlyContext),
                new Separator(HORIZONTAL),
                createPropertiesBox(shaderSelectorComboBox, (OnTheFlyMaterialsContext) onTheFlyContext),
                new Separator(HORIZONTAL)
        );

        return matSettingsBox;
    }

    private VBox createPropertiesBox(ComboBox<MaterialPiece> shaderSelectorComboBox, OnTheFlyMaterialsContext onTheFlyContext) {
        VBox propertiesBox = new VBox();

        propertiesBox.getStyleClass().addAll(CSS_CLASS_MAT_PROPERTIES_BOX);

        propertiesBox.getChildren().addAll(
            createShaderBox(shaderSelectorComboBox, onTheFlyContext)
        );

        return propertiesBox;
    }

    private HBox createShaderBox(ComboBox<MaterialPiece> shaderSelectorComboBox, OnTheFlyMaterialsContext onTheFlyContext) {
        HBox shaderBox = new HBox();

        shaderBox.getStyleClass().addAll(CSS_CLASS_SHADER_BOX);

        Label shaderValueLabel = new Label(DisplayConstants.LABEL_SHADER);
        shaderValueLabel.setLabelFor(shaderSelectorComboBox);
        shaderValueLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        shaderSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
                getShaderSelectorChangeListener(onTheFlyContext));
        Bindings.bindBidirectional(
                onTheFlyContext.getCurrentShaderProperty(), shaderSelectorComboBox.valueProperty());

        shaderBox.getChildren().addAll(
                shaderValueLabel,
                shaderSelectorComboBox
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

        colorsBox.getStyleClass().addAll(CSS_CLASS_MAT_COLORS_BOX);

        colorsBox.getChildren().addAll(
                createColorBox(AMBIENT, onTheFlyContext),
                createColorBox(DIFFUSE, onTheFlyContext),
                createColorBox(SPECULAR, onTheFlyContext),
                createColorBox(OTHER, onTheFlyContext),
                createAlphaBox(onTheFlyContext)
        );

        return colorsBox;
    }

    private HBox createColorBox(fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind colorKind, OnTheFlyMaterialsContext onTheFlyMaterialsContext) {
        HBox colorBox = new HBox();
        colorBox.getStyleClass().add(CSS_CLASS_COLOR_BOX);

        Label colorTypeLabel = new Label(DICTIONARY_LABELS_COLORS.get(colorKind));
        colorTypeLabel.getStyleClass().add(CSS_CLASS_COLOR_TYPE_LABEL);

        Label colorDescriptionLabel = new Label();
        colorDescriptionLabel.getStyleClass().add(CSS_CLASS_COLOR_DESC_LABEL);
        colorDescriptionLabel.setTooltip(new Tooltip(LABEL_TOOLTIP_COLOR_PICKER));
        colorDescriptionLabel.textProperty().bind(createDescriptionBinding(onTheFlyMaterialsContext, colorKind));

        HBox colorPreviewField = new HBox();
        colorPreviewField.getStyleClass().add(CSS_CLASS_COLOR_PREVIEW_BOX);
        colorPreviewField.backgroundProperty().bind(createBackgroundBinding(onTheFlyMaterialsContext, colorKind));

        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);
        colorPicker.getStyleClass().add(CSS_CLASS_COLOR_PICKER);
        colorPicker.setTooltip(new Tooltip(LABEL_TOOLTIP_COLOR_PICKER));
        Property<fr.tduf.libunlimited.low.files.gfx.materials.domain.Color> currentColorProperty = onTheFlyMaterialsContext.getCurrentColorPropertyOfKind(colorKind);
        bindBidirectional(
                colorPicker.valueProperty(),
                currentColorProperty,
                getColorPickerChangeListener(colorKind, onTheFlyMaterialsContext),
                getCurrentColorChangeListener(colorPicker.valueProperty(), onTheFlyMaterialsContext.getCurrentMaterialProperty()));

        colorBox.getChildren().addAll(
                colorTypeLabel,
                colorPreviewField,
                colorPicker,
                colorDescriptionLabel
        );

        return colorBox;
    }

    /* One of current colors has changed -> we update picker and material*/
    private ChangeListener<fr.tduf.libunlimited.low.files.gfx.materials.domain.Color> getCurrentColorChangeListener(Property<Color> colorPickerValueProperty, Property<Material> currentMaterialProperty) {
        return (observableValue, oldValue, newValue) -> {
            Log.debug(THIS_CLASS_NAME, "currentColorChangeListener: " + oldValue + " => "  + newValue);

            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            colorPickerValueProperty.setValue(materialColorToFxColor(newValue));

            Material currentMaterial = currentMaterialProperty.getValue();
            if (newValue == null || currentMaterial == null) {
                return;
            }

            getEditorContext().getChangeDataController().updateMaterialColor(currentMaterial, newValue);
        };
    }

    /* One of picker colors has changed -> we update color property */
    private ChangeListener<Color> getColorPickerChangeListener(fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind colorKind, OnTheFlyMaterialsContext onTheFlyMaterialsContext) {
        return (observableValue, oldValue, newValue) -> {
            Log.debug(THIS_CLASS_NAME, "colorPickerChangeListener: " + oldValue + " -> " + newValue);

            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            Material currentMaterial = onTheFlyMaterialsContext.getCurrentMaterialProperty().getValue();
            if (currentMaterial == null) {
                return;
            }

            fr.tduf.libunlimited.low.files.gfx.materials.domain.Color materialColor = fxColorToMaterialColor(newValue, colorKind);
            onTheFlyMaterialsContext.getCurrentColorPropertyOfKind(colorKind).setValue(materialColor);
        };
    }

    private ObjectBinding<Background> createBackgroundBinding(OnTheFlyMaterialsContext onTheFlyMaterialsContext, fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind colorKind) {
        return createObjectBinding(() -> {
            fr.tduf.libunlimited.low.files.gfx.materials.domain.Color currentColor = onTheFlyMaterialsContext.getCurrentColorPropertyOfKind(colorKind).getValue();
            if (currentColor == null) {
                return Background.EMPTY;
            }
            Color fxBackColor = materialColorToFxColor(currentColor);
            BackgroundFill fill = new BackgroundFill(fxBackColor, CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(fill);
        }, onTheFlyMaterialsContext.getCurrentColorPropertyOfKind(colorKind));
    }

    private ObjectBinding<String> createDescriptionBinding(OnTheFlyMaterialsContext onTheFlyMaterialsContext, fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind colorKind) {
        return createObjectBinding(() -> {
            fr.tduf.libunlimited.low.files.gfx.materials.domain.Color currentColor = onTheFlyMaterialsContext.getCurrentColorPropertyOfKind(colorKind).getValue();
            if (currentColor == null) {
                return LABEL_COLOR_DESCRIPTION_DEFAULT;
            }

            String colorDescription = currentColor.getDescription();
            Log.debug(THIS_CLASS_NAME, "Description binding: " + colorDescription);

            return colorDescription;
        }, onTheFlyMaterialsContext.getCurrentColorPropertyOfKind(colorKind));
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
                onTheFlyContext.getRawValueProperty(), materialSelectorComboBox.valueProperty(), new MaterialToRawValueConverter(this.materialsInfoEnhancedProperty, getEditorContext(), onTheFlyContext.getCurrentTopic(), this::getFullNameFromDictionary));

        Label availableMaterialsLabel = new Label(LABEL_AVAILABLE_MATERIALS);
        availableMaterialsLabel.setLabelFor(materialSelectorComboBox);
        availableMaterialsLabel.getStyleClass().add(CSS_CLASS_ITEM_LABEL);

        matSelectorBox.getChildren().addAll(
                availableMaterialsLabel,
                materialSelectorComboBox);

        return matSelectorBox;
    }

    private ChangeListener<Material> getMaterialSelectorChangeListener(OnTheFlyMaterialsContext onTheFlyMaterialsContext) {
        return (ObservableValue<? extends Material> observable, Material oldValue, Material newValue) -> {
            Log.debug(THIS_CLASS_NAME, "materialSelectorChangeListener:" + oldValue + " => " + newValue);

            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            if (newValue == null) {
                onTheFlyMaterialsContext.resetAllCurrentColorProperties();
                onTheFlyMaterialsContext.setCurrentShader(null);
                onTheFlyMaterialsContext.setCurrentMaterial(null);
                return;
            }

            DbDto.Topic currentTopic = onTheFlyMaterialsContext.getCurrentTopic();

            String materialNormalizedName = newValue.getName();
            String materialFullName = getFullNameFromDictionary(materialNormalizedName);

            MainStageChangeDataController changeDataController = getEditorContext().getChangeDataController();

            BulkDatabaseMiner miner = getEditorContext().getMiner();
            DbDto currentTopicObject = miner.getDatabaseTopic(currentTopic)
                    .orElseThrow(() -> new IllegalStateException(String.format("Database topic should exist: %s", currentTopic)));
            if (!MaterialsHelper.isExistingMaterialNameInResources(materialFullName, currentTopic, miner)
                    && !MaterialsHelper.isExistingMaterialNameInResources(materialNormalizedName, currentTopic, miner)) {
                handleAutoResourceCreation(currentTopicObject, materialNormalizedName, onTheFlyMaterialsContext);
            }

            onTheFlyMaterialsContext.setCurrentMaterial(newValue);
            onTheFlyMaterialsContext.setCurrentShader(newValue.getProperties().getShader().getConfiguration());

            // Update color props
            updateAllColorProperties(newValue, onTheFlyMaterialsContext);

            // Update database entry
            changeDataController.updateContentItem(currentTopic, onTheFlyMaterialsContext.getFieldRank(), onTheFlyMaterialsContext.getRawValueProperty().getValue());
        };
    }

    private ChangeListener<MaterialPiece> getShaderSelectorChangeListener(OnTheFlyMaterialsContext onTheFlyContext) {
        return (ObservableValue<? extends MaterialPiece> observable, MaterialPiece oldValue, MaterialPiece newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            Material currentMaterial = onTheFlyContext.getCurrentMaterialProperty().getValue();
            if (newValue != null && currentMaterial != null) {
                getEditorContext().getChangeDataController().updateShaderConfiguration(currentMaterial, newValue);
            }

            onTheFlyContext.setCurrentShader(newValue);
        };
    }

    private String getFullNameFromDictionary(String materialNormalizedName) {
        return normalizedNamesDictionary.get(materialNormalizedName);
    }

    private void handleAutoResourceCreation(DbDto topicObject, String materialName, OnTheFlyMaterialsContext onTheFlyContext) {
        String newReference = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject);
        DbDto.Topic currentTopic = onTheFlyContext.getCurrentTopic();
        if (askForResourceCreation(currentTopic, newReference, materialName)) {
            getEditorContext().getChangeDataController().addResourceWithReference(currentTopic, DEFAULT, newReference, materialName);
            onTheFlyContext.getRawValueProperty().setValue(newReference);
            MaterialsHelper.updateNormalizedDictionary(normalizedNamesDictionary, getEditorContext().getMiner());
        }
    }

    private boolean askForResourceCreation(DbDto.Topic topic, String ref, String value) {
        SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(CONFIRMATION)
                .withTitle(TITLE_SELECTING_MATERIAL)
                .withMessage(String.format(FORMAT_MESSAGE_RESOURCE_NAME_NOT_FOUND, value))
                .withDescription(String.format(FORMAT_DESCRIPTION_RESOURCE_CREATION, ref, topic.getLabel()))
                .build();
        Optional<ButtonType> buttonResult = CommonDialogsHelper.showDialog(dialogOptions, getEditorContext().getMainWindow());

        return buttonResult.isPresent() && OK == buttonResult.get();
    }

    private static void updateAllColorProperties(Material material, OnTheFlyMaterialsContext onTheFlyContext) {
        Log.debug(THIS_CLASS_NAME, "updateAllColorProperties for material " + material);

        MaterialSettings materialSettings = material.getProperties();
        onTheFlyContext.setCurrentColor(materialSettings.getAmbientColor());
        onTheFlyContext.setCurrentColor(materialSettings.getDiffuseColor());
        onTheFlyContext.setCurrentColor(materialSettings.getOtherColor());
        onTheFlyContext.setCurrentColor(materialSettings.getSpecularColor());
    }

    private static Color materialColorToFxColor(fr.tduf.libunlimited.low.files.gfx.materials.domain.Color materialColor) {
        if (materialColor == null) {
            return null;
        }

        return Color.color(
                materialColor.getRedCompound(),
                materialColor.getGreenCompound(),
                materialColor.getBlueCompound(),
                materialColor.getOpacity());
    }

    private static fr.tduf.libunlimited.low.files.gfx.materials.domain.Color fxColorToMaterialColor(Color fxColor, fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind colorKind) {
        if (fxColor == null) {
            return null;
        }

        return fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.builder()
                .ofKind(colorKind)
                .fromRGB((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue())
                .withOpacity((float) fxColor.getOpacity())
                .build();
    }

    private static Path resolveAndCheckBankFilePath(String gameLocation) throws IOException {
        Path bankFilePath = Paths.get(gameLocation, DIRECTORY_EURO, DIRECTORY_BANKS, DIRECTORY_VEHICLES, FILE_COLORS_BANK);
        if (!Files.exists(bankFilePath)) {
            String warningMessage = String.format(FORMAT_MESSAGE_WARN_NO_MATERIALS, bankFilePath.toString());
            Log.warn(THIS_CLASS_NAME, warningMessage);

            throw new IOException(warningMessage);
        }
        return bankFilePath;
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

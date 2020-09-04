package fr.tduf.libunlimited.low.files.gfx.materials.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.framework.lang.UByte;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.*;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.framework.lang.UByte.fromSigned;
import static fr.tduf.libunlimited.low.files.gfx.materials.rw.StoreConstants.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Allows to read data from a .2DM file.
 */
public class MaterialsParser extends GenericParser<MaterialDefs> {

    private MaterialsParser(XByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static MaterialsParser load(XByteArrayInputStream inputStream) throws IOException {
        return new MaterialsParser(
                requireNonNull(inputStream, "A stream containing material definitions is required"));
    }

    @Override
    protected MaterialDefs generate() {
        List<Material> materials = parseMaterialsFromStore();
        List<AdditionalSetting> additionalSettings = parseAdditionalSettingsFromStore();
        long fileSize = getDataStore().getInteger(FIELD_FILE_SIZE_BYTES)
                .orElseThrow(() -> new IllegalStateException("Datastore should contain fileSizeBytes data"));
        return MaterialDefs.builder()
                .withFileSize(fileSize)
                .fromDatastore(getDataStore())
                .withMaterials(materials)
                .withAdditionalSettings(additionalSettings)
                .build();
    }

    private List<Material> parseMaterialsFromStore() {
        List<DataStore> materialStores = getDataStore().getRepeatedValues(FIELD_HASHES);
        return materialStores.stream()
                .map(matStore -> {
                    String materialName = matStore.getText(StoreConstants.FIELD_H)
                            .orElseThrow(() -> new IllegalStateException("Material store should contain hash data"))
                            .trim();
                    String defsKeyName = matStore.getTargetKeyAtAddress(FIELD_MATERIAL_ADDRESS)
                            .orElseThrow(() -> new IllegalStateException("No global settings available at key: materialAddress"));
                    String parameterAddressFieldName = defsKeyName + FIELD_PARAMETER_ADDRESS;
                    String parameterAddressKeyName = getDataStore().getTargetKeyAtAddress(parameterAddressFieldName)
                            .orElseThrow(() -> new IllegalStateException(String.format("No shader parameters available at key: %s", parameterAddressFieldName)));
                    MaterialSettings materialSettings = parseGlobalSettings(parameterAddressKeyName, defsKeyName);
                    LayerGroup layerGroup = parseMaterialLayerGroup(parameterAddressKeyName);
                    return Material.builder()
                            .withName(materialName)
                            .withGlobalSettings(materialSettings)
                            .withLayerGroup(layerGroup)
                            .build();
                })
                .collect(toList());
   }

    private List<AdditionalSetting> parseAdditionalSettingsFromStore() {
        List<DataStore> settingsStore = getDataStore().getRepeatedValues(FIELD_ADDITIONAL_SECTION);
        return settingsStore.stream()
                .map(settingStore -> {
                    String tag = settingStore.getText(FIELD_ENTRY_TAG)
                            .orElseThrow(() -> new IllegalStateException("Additional settings should contain name data"))
                            .trim();
                    byte[] data = settingStore.getRawValue(FIELD_REST)
                            .orElseThrow(() -> new IllegalStateException("Additional settings should contain rest data"));
                    return AdditionalSetting.builder()
                            .withName(tag)
                            .withData(data)
                            .build();
                })
                .collect(toList());
    }

    private MaterialSettings parseGlobalSettings(String parameterAddressKeyName, String settingsKeyName) {
        byte alpha = getDataStore().getInteger(settingsKeyName + FIELD_ALPHA)
                .orElseThrow(() -> new IllegalStateException("Settings alpha data should be present in store"))
                .byteValue();
        List<UByte> alphaBlendValues = parseSettingsGroupAsUnsignedByte(getDataStore(), settingsKeyName, FIELD_ALPHA_BLEND, 2);
        Color ambientColor = parseSettingsGroupAsColor(settingsKeyName, FIELD_AMBIENT_COLOR);
        Color diffuseColor = parseSettingsGroupAsColor(settingsKeyName, FIELD_DIFFUSE_COLOR);
        Color specularColor = parseSettingsGroupAsColor(settingsKeyName, FIELD_SPECULAR_COLOR);
        Color otherColor = parseSettingsGroupAsColor(settingsKeyName, FIELD_OTHER_COLOR);
        Shader shader = parseShaderParameters(parameterAddressKeyName);
        byte[] unknownSettings = getDataStore().getRawValue(settingsKeyName + FIELD_UNK_1)
                .orElseThrow(() -> new IllegalStateException("unk1 data should be present in store"));
        return MaterialSettings.builder()
                .withAlpha(fromSigned(alpha))
                .withAlphaBlending(alphaBlendValues)
                .withAmbientColor(ambientColor)
                .withDiffuseColor(diffuseColor)
                .withSpecularColor(specularColor)
                .withOtherColor(otherColor)
                .withShaderParameters(shader)
                .withUnknownSettings(unknownSettings)
                .build();
    }

    private Shader parseShaderParameters(String paramsKeyName) {
        byte[] shaderConfiguration = getDataStore().getRawValue(paramsKeyName + FIELD_SHADER_CONFIGURATION)
                .orElseThrow(() -> new IllegalStateException("Shader configuration data should be present in store"));
        MaterialPiece configuration = MaterialPiece.fromBinaryContents(shaderConfiguration);
        List<MaterialSubSetting> subSettings = parseMaterialSubSettings(paramsKeyName);
        List<Float> reflectionLayerScaleValues = null;
        try {
            reflectionLayerScaleValues = parseSettingsGroupAsFloatingPoint(getDataStore(), paramsKeyName, FIELD_REFLECTION_LAYER_SCALE, 2);
        } catch (IllegalStateException ise) {
            Log.debug("No reflection layer scale settings");
        }
        return Shader.builder()
                .withConfiguration(configuration)
                .withSubSettings(subSettings)
                .withReflectionLayerScale(reflectionLayerScaleValues)
                .build();
    }

    private LayerGroup parseMaterialLayerGroup(String paramsKeyName) {
        String layerGroupKeyName = getDataStore().getTargetKeyAtAddress(paramsKeyName + FIELD_LAYER_GROUP_ADDRESS)
                .orElseThrow(() -> new IllegalStateException("No layer groups available at key: layerGroupAddress"));
        int layerCount = getDataStore().getInteger(layerGroupKeyName + FIELD_LAYERS_COUNT)
                .orElseThrow(() -> new IllegalStateException("No layer count available at key: layersCount"))
                .intValue();
        List<Layer> layers = parseLayers(layerGroupKeyName);
        byte[] magic = getDataStore().getRawValue(layerGroupKeyName + FIELD_LAYER_GROUP_MAGIC_2)
                .orElseThrow(() -> new IllegalStateException("layerGroupMagic2 data should be present in store"));
        return LayerGroup.builder()
                .withLayerCount(layerCount)
                .withLayers(layers)
                .withMagic(magic)
                .build();
    }

    private List<Layer> parseLayers(String layerGroupKeyName) {
        return getDataStore().getRepeatedValues(FIELD_LAYERS, layerGroupKeyName).stream()
                .map(layerStore -> {
                    String name = layerStore.getText(FIELD_LAYER_NAME)
                            .orElseThrow(() -> new IllegalStateException("Layer name data should be present in store"))
                            .trim();
                    String targetFile = layerStore.getText(FIELD_TEXTURE_FILE)
                            .orElseThrow(() -> new IllegalStateException("Texture file data should be present in store"))
                            .trim();
                    List<UByte> flagValues = parseSettingsGroupAsUnsignedByte(layerStore, "", FIELD_LAYER_FLAG, 4);
                    // TODO
                    AnimationSettings animationSettings = new AnimationSettings();
                    return Layer.builder()
                            .withName(name)
                            .withTextureFile(targetFile)
                            .withFlags(flagValues)
                            .withAnimationSettings(animationSettings)
                            .build();
                })
                .collect(toList());
    }

    private List<MaterialSubSetting> parseMaterialSubSettings(String paramsKeyName) {
        List<DataStore> subSettingsStores = getDataStore().getRepeatedValues(FIELD_SETTINGS, paramsKeyName);

        return subSettingsStores.stream()
                .map(subSettingStore -> {
                    long id = subSettingStore.getInteger(FIELD_SUB_ID)
                            .orElseThrow(() -> new IllegalStateException("No id data available in store"));
                    long value1 = subSettingStore.getInteger(FIELD_UNK_1)
                            .orElseThrow(() -> new IllegalStateException("No unk1 data available in store"));
                    long value2 = subSettingStore.getInteger(FIELD_UNK_2)
                            .orElseThrow(() -> new IllegalStateException("No unk2 data available in store"));
                    long value3 = subSettingStore.getInteger("unk3")
                            .orElseThrow(() -> new IllegalStateException("No unk3 data available in store"));
                    return new MaterialSubSetting(id, value1, value2, value3);
                })
                .collect(toList());
    }

    private Color parseSettingsGroupAsColor(String groupKeyName, String settingKeyName) {
        List<Float> values = getColorSettingsKeyStream(groupKeyName, settingKeyName)
                .map(itemKey -> getDataStore().getFloatingPoint(itemKey)
                        .orElseThrow(() -> new IllegalStateException(String.format("Color settings data should be present in store at key %s", itemKey))))
                .collect(toList());
        return Color.builder().fromRGBAndOpacity(values).build();
    }

    private List<UByte> parseSettingsGroupAsUnsignedByte(DataStore store, String groupKeyName, String settingKeyName, int groupSize) {
        return getSettingsKeyStream(groupKeyName, settingKeyName, groupSize)
                .map(itemKey -> store.getInteger(itemKey)
                        .orElseThrow(() -> new IllegalStateException(String.format("Settings data should be present in store at key %s", itemKey)))
                        .byteValue())
                .map(UByte::fromSigned)
                .collect(toList());
    }

    private List<Float> parseSettingsGroupAsFloatingPoint(DataStore store, String groupKeyName, String settingKeyName, int groupSize) {
        return getSettingsKeyStream(groupKeyName, settingKeyName, groupSize)
                .map(itemKey -> store.getFloatingPoint(itemKey)
                        .orElseThrow(() -> new IllegalStateException(String.format("Settings data should be present in store at key %s", itemKey))))
                .collect(toList());
    }

    private static Stream<String> getSettingsKeyStream(String groupKeyName, String settingKeyName, int groupSize) {
        return IntStream.range(1, groupSize + 1)
                .mapToObj(itemIndex -> String.format(FORMAT_SETTINGS_KEY, groupKeyName, settingKeyName, itemIndex));
    }

    private static Stream<String> getColorSettingsKeyStream(String groupKeyName, String settingKeyName) {
        return IntStream.range(0, 4)
                .mapToObj(itemIndex -> {
                    String currentSuffix = FIELD_SUFFIXES_COLORS[itemIndex];
                    return String.format(FORMAT_COLOR_SETTINGS_KEY, groupKeyName, settingKeyName, currentSuffix);
                });
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/2DM-map.json";
    }

    @Override
    public FileStructureDto getStructure() {
        return null;
    }
}

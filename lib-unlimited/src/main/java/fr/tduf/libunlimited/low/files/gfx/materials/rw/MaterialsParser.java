package fr.tduf.libunlimited.low.files.gfx.materials.rw;

import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.*;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Allow to read data from a .2DM file.
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
        long fileSize = getDataStore().getInteger("fileSizeBytes")
                .orElseThrow(() -> new IllegalStateException("Datastore should contain fileSizeBytes data"));
        return MaterialDefs.builder()
                .withFileSize(fileSize)
                .fromDatastore(getDataStore())
                .withMaterials(materials)
                .withAdditionalSettings(additionalSettings)
                .build();
    }

    private List<Material> parseMaterialsFromStore() {
        List<DataStore> materialStores = getDataStore().getRepeatedValues("hashes");
        return materialStores.stream()
                .map(matStore -> {
                    String materialName = matStore.getText("h")
                            .orElseThrow(() -> new IllegalStateException("Material store should contain hash data"))
                            .trim();
                    MaterialSettings materialSettings = parseGlobalSettings(matStore);
                    return Material.builder()
                            .withName(materialName)
                            .withGlobalSettings(materialSettings)
                            .build();
                })
                .collect(toList());
   }

    private List<AdditionalSetting> parseAdditionalSettingsFromStore() {
        List<DataStore> settingsStore = getDataStore().getRepeatedValues("additionalSection");
        return settingsStore.stream()
                .map(settingStore -> {
                    String tag = settingStore.getText("entryTag")
                            .orElseThrow(() -> new IllegalStateException("Additional settings should contain name data"))
                            .trim();
                    byte[] data = settingStore.getRawValue("rest")
                            .orElseThrow(() -> new IllegalStateException("Additional settings should contain rest data"));
                    return AdditionalSetting.builder()
                            .withName(tag)
                            .withData(data)
                            .build();
                })
                .collect(toList());
    }

    private MaterialSettings parseGlobalSettings(DataStore matStore) {
        String settingsKeyName = matStore.getTargetKeyAtAddress("materialAddress")
                .orElseThrow(() -> new IllegalStateException("No global settings available at key: materialAddress"));
        byte alpha = getDataStore().getInteger(settingsKeyName + "alpha")
                .orElseThrow(() -> new IllegalStateException("Settings alpha data should be present in store"))
                .byteValue();
        List<Byte> saturationValues = parseSettingsGroupAsByte(getDataStore(), settingsKeyName, "saturation", 2);
        List<Float> ambientValues = parseSettingsGroupAsFloatingPoint(settingsKeyName, "ambient", 4);
        List<Float> diffuseValues = parseSettingsGroupAsFloatingPoint(settingsKeyName, "diffuse", 4);
        List<Float> specularValues = parseSettingsGroupAsFloatingPoint(settingsKeyName, "specular", 4);
        List<Float> otherValues = parseSettingsGroupAsFloatingPoint(settingsKeyName, "other", 4);
        ShaderParameters shaderParameters = parseShaderParameters(settingsKeyName + "parameterAddress");
        byte[] unknownSettings = getDataStore().getRawValue(settingsKeyName + "unk1")
                .orElseThrow(() -> new IllegalStateException("unk1 data should be present in store"));

        return MaterialSettings.builder()
                .withAlpha(alpha)
                .withAmbient(ambientValues)
                .withDiffuse(diffuseValues)
                .withSaturation(saturationValues)
                .withSpecular(specularValues)
                .withOtherSettings(otherValues)
                .withShaderParameters(shaderParameters)
                .withUnknownSettings(unknownSettings)
                .build();
    }

    private ShaderParameters parseShaderParameters(String parameterAddressKeyName) {
        String paramsKeyName = getDataStore().getTargetKeyAtAddress(parameterAddressKeyName)
                .orElseThrow(() -> new IllegalStateException(String.format("No shader parameters available at key: %s", parameterAddressKeyName)));
        byte[] shaderConfiguration = getDataStore().getRawValue(paramsKeyName + "shaderConfiguration")
                .orElseThrow(() -> new IllegalStateException("Shader configuration data should be present in store"));
        MaterialPiece configuration = MaterialPiece.fromBinaryContents(shaderConfiguration);
        List<MaterialSubSetting> subSettings = parseMaterialSubSettings(paramsKeyName);
        LayerGroup layerGroup = parseMaterialLayerGroup(paramsKeyName);
        return ShaderParameters.builder()
                .withConfiguration(configuration)
                .withSubSettings(subSettings)
                .withLayerGroup(layerGroup)
                .build();
    }

    private LayerGroup parseMaterialLayerGroup(String paramsKeyName) {
        String layerGroupKeyName = getDataStore().getTargetKeyAtAddress(paramsKeyName + "layerGroupAddress")
                .orElseThrow(() -> new IllegalStateException("No layer groups available at key: layerGroupAddress"));
        int layerCount = getDataStore().getInteger(layerGroupKeyName + "layersCount")
                .orElseThrow(() -> new IllegalStateException("No layer count available at key: layersCount"))
                .intValue();
        List<Layer> layers = parseLayers(layerGroupKeyName);
        byte[] magic = getDataStore().getRawValue(layerGroupKeyName + "layerGroupMagic2")
                .orElseThrow(() -> new IllegalStateException("layerGroupMagic2 data should be present in store"));
        return LayerGroup.builder()
                .withLayerCount(layerCount)
                .withLayers(layers)
                .withMagic(magic)
                .build();
    }

    private List<Layer> parseLayers(String layerGroupKeyName) {
        return getDataStore().getRepeatedValues("layers", layerGroupKeyName).stream()
                .map(layerStore -> {
                    String name = layerStore.getText("layerName")
                            .orElseThrow(() -> new IllegalStateException("Layer name data should be present in store"))
                            .trim();
                    String targetFile = layerStore.getText("textureFile")
                            .orElseThrow(() -> new IllegalStateException("Texture file data should be present in store"))
                            .trim();
                    List<Byte> flagValues = parseSettingsGroupAsByte(layerStore, "", "layerFlag", 4);
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
        List<DataStore> subSettingsStores = getDataStore().getRepeatedValues("settings", paramsKeyName);

        return subSettingsStores.stream()
                .map(subSettingStore -> {
                    long id = subSettingStore.getInteger("subId")
                            .orElseThrow(() -> new IllegalStateException("No id data available in store"));
                    long value1 = subSettingStore.getInteger("unk1")
                            .orElseThrow(() -> new IllegalStateException("No unk1 data available in store"));
                    long value2 = subSettingStore.getInteger("unk2")
                            .orElseThrow(() -> new IllegalStateException("No unk2 data available in store"));
                    long value3 = subSettingStore.getInteger("unk3")
                            .orElseThrow(() -> new IllegalStateException("No unk3 data available in store"));
                    return new MaterialSubSetting(id, value1, value2, value3);
                })
                .collect(toList());
    }

    private List<Float> parseSettingsGroupAsFloatingPoint(String groupKeyName, String settingKeyName, int groupSize) {
        return getSettingsKeyStream(groupKeyName, settingKeyName, groupSize)
                .map(itemKey -> getDataStore().getFloatingPoint(itemKey)
                        .orElseThrow(() -> new IllegalStateException(String.format("Settings data ashould be present in store at key %s", itemKey))))
                .collect(toList());
    }

    private List<Byte> parseSettingsGroupAsByte(DataStore store, String groupKeyName, String settingKeyName, int groupSize) {
        return getSettingsKeyStream(groupKeyName, settingKeyName, groupSize)
                .map(itemKey -> store.getInteger(itemKey)
                        .orElseThrow(() -> new IllegalStateException(String.format("Settings data ashould be present in store at key %s", itemKey)))
                        .byteValue())
                .collect(toList());
    }

    private static Stream<String> getSettingsKeyStream(String groupKeyName, String settingKeyName, int groupSize) {
        return IntStream.range(1, groupSize + 1)
                .mapToObj(itemIndex -> String.format("%s%s%d", groupKeyName, settingKeyName, itemIndex));
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

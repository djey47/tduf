package fr.tduf.libunlimited.low.files.gfx.materials.rw;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Color;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.helper.MaterialsHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.tduf.libunlimited.low.files.gfx.materials.helper.StoreConstants.*;
import static java.util.Objects.requireNonNull;

/**
 * Allows to write materials data to a .2DM file
 */
public class MaterialsWriter extends GenericWriter<MaterialDefs> {
    protected MaterialsWriter(MaterialDefs data) throws IOException {
        super(data);
    }

    /**
     * Creates a writer from pre-existing domain object
     * @param materialDefs : store providing data to be written
     */
    public static MaterialsWriter load(MaterialDefs materialDefs) throws IOException {
        return new MaterialsWriter(requireNonNull(materialDefs, "A material definitions domain object is required."));
    }

    @Override
    protected void fillStore() {
        // Q&D method: we get original data store and only update supported fields...
        getDataStore().merge(getData().getOriginalDataStore());

        updateAllColors();
    }

    private void updateAllColors() {
        AtomicInteger materialIndex = new AtomicInteger(0);
        getData().getMaterials()
                .forEach(material -> {
                    int index = materialIndex.getAndIncrement();
                    String sourceFieldName = String.format("%s[%d].%s", FIELD_HASHES, index, FIELD_MATERIAL_ADDRESS);
                    String defsKeyName = getDataStore().getTargetKeyAtAddress(sourceFieldName)
                            .orElseThrow(() -> new IllegalStateException("No global settings available at key: materialAddress"));
                    Color ambientColor = material.getProperties().getAmbientColor();
                    Color diffuseColor = material.getProperties().getDiffuseColor();
                    Color specularColor = material.getProperties().getSpecularColor();
                    Color otherColor = material.getProperties().getOtherColor();

                    updateColorAtMaterialIndex(ambientColor, FIELD_AMBIENT_COLOR, defsKeyName);
                    updateColorAtMaterialIndex(diffuseColor, FIELD_DIFFUSE_COLOR, defsKeyName);
                    updateColorAtMaterialIndex(specularColor, FIELD_SPECULAR_COLOR, defsKeyName);
                    updateColorAtMaterialIndex(otherColor, FIELD_OTHER_COLOR, defsKeyName);
                });

    }

    private void updateColorAtMaterialIndex(Color color, String fieldPrefix, String defsKeyName) {
        MaterialsHelper.getColorSettingsKeyStream(defsKeyName, fieldPrefix)
                .forEach(settingsKey -> {
                    float currentCompoundValue;
                    switch (settingsKey.substring(settingsKey.length() - 1)) {
                        case FIELD_SUFFIX_COLOR_RED:
                            currentCompoundValue = color.getRedCompound();
                            break;
                        case FIELD_SUFFIX_COLOR_GREEN:
                            currentCompoundValue = color.getGreenCompound();
                            break;
                        case FIELD_SUFFIX_COLOR_BLUE:
                            currentCompoundValue = color.getBlueCompound();
                            break;
                        case FIELD_SUFFIX_COLOR_OPACITY:
                            currentCompoundValue = color.getOpacity();
                            break;
                        default: throw new IllegalStateException("Unsupported color compound for key: " + settingsKey);
                    }
                    getDataStore().addFloatingPoint(settingsKey, currentCompoundValue);
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

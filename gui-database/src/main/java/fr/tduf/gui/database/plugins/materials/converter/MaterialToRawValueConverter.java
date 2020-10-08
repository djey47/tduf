package fr.tduf.gui.database.plugins.materials.converter;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.helper.MaterialsHelper;
import javafx.beans.property.Property;
import javafx.util.StringConverter;

import java.util.function.Function;

import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static fr.tduf.libunlimited.common.game.helper.GameEngineHelper.normalizeString;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.RESOURCE_VALUE_INTERIOR_COLOR_NONE;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.RESOURCE_VALUE_NONE;
import static java.util.Arrays.asList;

public class MaterialToRawValueConverter extends StringConverter<Material> {
    private static final Class<MaterialToRawValueConverter> thisClass = MaterialToRawValueConverter.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private final Property<MaterialDefs> materialDefsProperty;
    private final EditorContext editorContext;
    private final DbDto.Topic currentTopic;
    private final Function<String, String> fullNameProvider;

    public MaterialToRawValueConverter(Property<MaterialDefs> materialDefsProperty, EditorContext editorContext, DbDto.Topic currentTopic, Function<String, String> fullNameProvider ) {
        this.materialDefsProperty = materialDefsProperty;
        this.editorContext = editorContext;
        this.currentTopic = currentTopic;
        this.fullNameProvider = fullNameProvider;
    }

    @Override
    public String toString(Material material) {
        if (material == null) {
            return "";
        }

        String materialName = material.getName();
        BulkDatabaseMiner miner = editorContext.getMiner();
        return MaterialsHelper.getResourceRefForMaterialName(materialName, currentTopic, miner)
                .orElse(MaterialsHelper.getResourceRefForMaterialName(fullNameProvider.apply(materialName), currentTopic, miner)
                        .orElse(MaterialsHelper.resolveNoMaterialReference(currentTopic)));
    }

    @Override
    public Material fromString(String materialRawValue) {
        String materialName = editorContext.getMiner().getLocalizedResourceValueFromTopicAndReference(materialRawValue, currentTopic, DEFAULT)
                .orElseThrow(() -> new IllegalStateException("No resource with raw value " + materialRawValue));
        if (asList(RESOURCE_VALUE_NONE, RESOURCE_VALUE_INTERIOR_COLOR_NONE).contains(materialName)) {
            return null;
        }

        String materialNameNormalized = normalizeString(materialName);

        return materialDefsProperty.getValue().getMaterials().stream()
                .filter(material -> material.getName().equals(materialNameNormalized))
                .findAny()
                .orElseGet(() -> {
                    Log.warn(THIS_CLASS_NAME, "No material available for name " + materialNameNormalized);
                    return null;
                });
    }
}

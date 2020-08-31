package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import javafx.util.StringConverter;

import java.util.Map;

import static fr.tduf.libunlimited.common.game.helper.GameEngineHelper.normalizeString;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.RESOURCE_VALUE_INTERIOR_COLOR_NONE;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.RESOURCE_VALUE_NONE;
import static java.util.Arrays.asList;

public class MaterialToRawValueConverter extends StringConverter<Material> {
    private final MaterialDefs materialDefs;
    private final EditorContext editorContext;
    private final DbDto.Topic currentTopic;
    private final Map<String, String> normalizedNamesDictionary;

    public MaterialToRawValueConverter(MaterialDefs materialDefs, EditorContext editorContext, DbDto.Topic currentTopic, Map<String, String> normalizedNamesDictionary) {
        this.materialDefs = materialDefs;
        this.editorContext = editorContext;
        this.currentTopic = currentTopic;
        this.normalizedNamesDictionary = normalizedNamesDictionary;
    }

    @Override
    public String toString(Material material) {
        if (material == null) {
            return "";
        }

        String materialName = material.getName();
        String materialNameAsResourceValue = normalizedNamesDictionary.get(materialName);

        return editorContext.getMiner().getResourcesFromTopic(currentTopic)
                .orElseThrow(() -> new IllegalStateException("No resources for topic " + currentTopic))
                .getEntries().parallelStream()
                .filter(resourceEntry -> resourceEntry.pickValue()
                        .map(String::toUpperCase)
                        .orElseThrow(() -> new IllegalStateException("No resource value")).equals(materialNameAsResourceValue))
                .findAny()
                // TODO Reference not found => ask and create resource entry in current topic
                .orElseThrow(() -> new IllegalStateException("No resource entry for material name " + materialName))
                .getReference();
    }

    @Override
    public Material fromString(String materialRawValue) {
        String materialName = editorContext.getMiner().getLocalizedResourceValueFromTopicAndReference(materialRawValue, currentTopic, Locale.DEFAULT)
                .orElseThrow(() -> new IllegalStateException("No resource with raw value " + materialRawValue))
                .toUpperCase();
        if (asList(RESOURCE_VALUE_NONE, RESOURCE_VALUE_INTERIOR_COLOR_NONE).contains(materialName)) {
            return null;
        }

        String materialNameNormalized = normalizeString(materialName);

        return materialDefs.getMaterials().stream()
                .filter(material -> material.getName().equals(materialNameNormalized))
                .findAny()
                // TODO Material not found => error
                .orElseThrow(() -> new IllegalStateException("No material available for name " + materialNameNormalized));
    }
}

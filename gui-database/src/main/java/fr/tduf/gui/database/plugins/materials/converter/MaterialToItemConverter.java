package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.util.StringConverter;

import java.util.Map;

public class MaterialToItemConverter extends StringConverter<Material> {
    private final Map<String, String> normalizedNamesDictionary;

    public MaterialToItemConverter(Map<String, String> normalizedNamesDictionary) {
        this.normalizedNamesDictionary = normalizedNamesDictionary;
    }

    @Override
    public String toString(Material material) {
        String materialRawName = material.getName();
        String materialClearName = normalizedNamesDictionary.getOrDefault(materialRawName, DisplayConstants.LABEL_NO_NAME);
        return String.format(DisplayConstants.FORMAT_MATERIAL_LABEL, materialRawName, materialClearName);
    }

    @Override
    public Material fromString(String materialName) {
        return null;
    }
}

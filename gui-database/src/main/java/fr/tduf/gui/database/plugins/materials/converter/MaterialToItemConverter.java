package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.util.StringConverter;

import java.util.Map;

import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.FORMAT_MATERIAL_LABEL;
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.LABEL_NO_NAME;

public class MaterialToItemConverter extends StringConverter<Material> {
    private final Map<String, String> normalizedNamesDictionary;

    public MaterialToItemConverter(Map<String, String> normalizedNamesDictionary) {
        this.normalizedNamesDictionary = normalizedNamesDictionary;
    }

    @Override
    public String toString(Material material) {
        String materialRawName = material.getName();
        String materialClearName = normalizedNamesDictionary.getOrDefault(materialRawName, LABEL_NO_NAME);
        return String.format(FORMAT_MATERIAL_LABEL, materialRawName, materialClearName);
    }

    @Override
    public Material fromString(String materialName) {
        return null;
    }
}

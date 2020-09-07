package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.util.StringConverter;

import java.util.function.Function;

import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.FORMAT_MATERIAL_LABEL;
import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.LABEL_NO_NAME;

public class MaterialToItemConverter extends StringConverter<Material> {
    private final Function<String, String> fullNameProvider;

    public MaterialToItemConverter(Function<String, String> fullNameProvider) {
        this.fullNameProvider = fullNameProvider;
    }

    @Override
    public String toString(Material material) {
        String materialRawName = material.getName();
        String materialClearName = fullNameProvider.apply(materialRawName);
        return String.format(FORMAT_MATERIAL_LABEL, materialRawName, materialClearName == null ? LABEL_NO_NAME : materialClearName);
    }

    @Override
    public Material fromString(String materialName) {
        return null;
    }
}

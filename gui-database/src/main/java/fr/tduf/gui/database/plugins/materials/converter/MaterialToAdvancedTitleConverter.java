package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;

import java.util.function.Function;

public class MaterialToAdvancedTitleConverter extends MaterialToItemConverter {
    public MaterialToAdvancedTitleConverter(Function<String, String> fullNameProvider) {
        super(fullNameProvider);
    }

    @Override
    public String toString(Material material) {
        if (material == null) {
            return "";
        }
        return String.format(DisplayConstants.FORMAT_TITLE_ADVANCED, super.toString(material));
    }

    @Override
    public Material fromString(String s) {
        return null;
    }
}

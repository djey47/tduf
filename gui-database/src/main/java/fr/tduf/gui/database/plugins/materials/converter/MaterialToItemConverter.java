package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.util.StringConverter;

public class MaterialToItemConverter extends StringConverter<Material> {
    @Override
    public String toString(Material material) {
        return material.getName();
    }

    @Override
    public Material fromString(String materialName) {
        return null;
    }
}

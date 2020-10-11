package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.function.Function;

public class MaterialToReflectionLayerScaleLabelConverter extends StringConverter<Material> {
    @Override
    public String toString(Material material) {
        if (material == null) {
            return "";
        }
        return Arrays.toString(material.getProperties().getShader().getReflectionLayerScale());
    }

    @Override
    public Material fromString(String s) {
        return null;
    }
}

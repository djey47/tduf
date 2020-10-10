package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Layer;
import javafx.util.StringConverter;

import java.util.Arrays;

public class LayerFlagsToLabelConverter extends StringConverter<Layer> {
    @Override
    public String toString(Layer layer) {
        if (layer == null) {
            return "";
        }
        return Arrays.toString(layer.getFlags());
    }

    @Override
    public Layer fromString(String s) {
        return null;
    }
}

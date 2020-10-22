package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Layer;
import javafx.util.StringConverter;

public class LayerToItemConverter extends StringConverter<Layer> {
    @Override
    public String toString(Layer layer) {
        if (layer == null) {
            return "";
        }
        return String.format(DisplayConstants.FORMAT_LAYER_LABEL, layer.getName(), layer.getTextureFile());
    }

    @Override
    public Layer fromString(String s) {
        return null;
    }
}

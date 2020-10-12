package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Layer;
import javafx.util.StringConverter;

public class LayerTextureToLabelConverter extends StringConverter<Layer> {
    @Override
    public String toString(Layer layer) {
        if (layer == null) {
            return "";
        }
        // TODO Improvement: full file name??
        String textureFile = layer.getTextureFile();
        return textureFile.isEmpty() ?
                DisplayConstants.LABEL_NO_TEXTURE : String.format(DisplayConstants.FORMAT_TEXTURE_LABEL, textureFile);
    }

    @Override
    public Layer fromString(String s) {
        return null;
    }
}

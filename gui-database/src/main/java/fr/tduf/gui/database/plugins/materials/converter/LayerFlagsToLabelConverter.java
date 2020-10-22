package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.materials.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Layer;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class LayerFlagsToLabelConverter extends StringConverter<Layer> {
    @Override
    public String toString(Layer layer) {
        if (layer == null) {
            return "";
        }
        return Arrays.stream(layer.getFlags())
                .map(flag -> Integer.toString(flag.get()))
                .collect(joining(DisplayConstants.SEPARATOR_FLAGS));
    }

    @Override
    public Layer fromString(String s) {
        return null;
    }
}

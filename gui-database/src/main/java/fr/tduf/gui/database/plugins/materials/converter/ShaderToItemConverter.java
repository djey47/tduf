package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialPiece;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.plugins.materials.common.DisplayConstants.FORMAT_SHADER_LABEL;

public class ShaderToItemConverter extends StringConverter<MaterialPiece> {
    @Override
    public String toString(MaterialPiece shader) {
        return String.format(FORMAT_SHADER_LABEL, shader.getName(), shader.name());
    }

    @Override
    public MaterialPiece fromString(String shaderCode) {
        return MaterialPiece.valueOf(shaderCode);
    }
}

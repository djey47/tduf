package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialPiece;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ShaderToItemConverterTest {
    private final ShaderToItemConverter shaderToItemConverter = new ShaderToItemConverter();

    @Test
    void testToString_whenNullShader_shouldReturnEmptyString() {
        // given-when-then
        assertThat(shaderToItemConverter.toString(null)).isEmpty();
    }

    @Test
    void testToString_shouldReturnShaderDescription() {
        // given
        MaterialPiece shader = MaterialPiece.SHADER_ASPHALT;

        // when-then
        assertThat(shaderToItemConverter.toString(shader)).isEqualTo("Asphalt shader (SHADER_ASPHALT)");
    }
}

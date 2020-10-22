package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialSettingsTest {
    @Test
    void builder_withColors_shouldBuild_withRightColorKind() {
        // given-when
        MaterialSettings actual = MaterialSettings.builder()
                .withAmbientColor(getColor())
                .withDiffuseColor(getColor())
                .withSpecularColor(getColor())
                .withOtherColor(getColor())
                .build();

        // then
        assertThat(actual.getAmbientColor().getKind()).isEqualTo(Color.ColorKind.AMBIENT);
        assertThat(actual.getDiffuseColor().getKind()).isEqualTo(Color.ColorKind.DIFFUSE);
        assertThat(actual.getSpecularColor().getKind()).isEqualTo(Color.ColorKind.SPECULAR);
        assertThat(actual.getOtherColor().getKind()).isEqualTo(Color.ColorKind.OTHER);
    }

    private Color getColor() {
        return Color.builder()
                .fromRGB(1.0f, 1.0f, 1.0f)
                .build();
    }
}
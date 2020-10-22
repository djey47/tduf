package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ColorTest {
    @Test
    void getDescription_shouldReturnNormalizedContents() {
        // given
        Color c = Color.builder()
                .fromRGB(1.0f, 0.705f, 0.505f)
                .withOpacity(0.25f)
                .build();

        // when-then
        assertThat(c.getDescription()).isEqualTo("#ffb380 - (255,179,128,63)");
    }
}

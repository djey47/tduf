package fr.tduf.libunlimited.high.files.common.patcher.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceholderResolverTest {
    private PlaceholderResolver placeholderResolver = new PlaceholderResolver() {
        @Override
        public void resolveAllPlaceholders() {}
    };

    @BeforeEach
    void setUp() {

    }

    @Test
    void resolveSimplePlaceholder_whenNoPlaceholder_shouldReturnProvidedValue() {
        // given-when
        String actualId = placeholderResolver.resolveSimplePlaceholder("125");

        // then
        assertThat(actualId).isEqualTo("125");
    }

    @Test
    void resolveSimplePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // given
        placeholderResolver.patchProperties.register("CAM.ID", "125");

        // when
        String actualId = placeholderResolver.resolveSimplePlaceholder("{CAM.ID}");

        // then
        assertThat(actualId).isEqualTo("125");
    }

    @Test
    void resolveSimplePlaceholder_whenPlaceholder_butNoProperty_shouldThrowException() {
        // given-when-then
        assertThrows(IllegalArgumentException.class,
                () -> placeholderResolver.resolveSimplePlaceholder("{CAM.ID}"));
    }
}
package fr.tduf.libunlimited.common.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexHelperTest {
    @Test
    void createRegexFromGlob_withSimplePattern_shouldReturnRegex() {
        // given-when
        String actualPattern = RegexHelper.createRegexFromGlob("*.json");

        // then
        assertThat(actualPattern).isEqualTo(".*\\.json");
    }

    @Test
    void createRegexFromGlob_withAnotherPattern_shouldReturnRegex() {
        // given-when
        String actualPattern = RegexHelper.createRegexFromGlob("Spot-*.spt");

        // then
        assertThat(actualPattern).isEqualTo("Spot-.*\\.spt");
    }
}

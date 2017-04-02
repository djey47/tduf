package fr.tduf.libunlimited.high.files.common.patcher.domain;


import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PatchPropertiesTest {

    @Test
    void retrieve_whenPropertyDoesNotExist_shouldReturnEmpty() throws Exception {
        // GIVEN
        final String placeholder = "P";
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("Q", " VALUE  ");


        // WHEN
        final Optional<String> actualValue = patchProperties.retrieve(placeholder);

        // THEN
        assertThat(actualValue).isEmpty();
    }

    @Test
    void retrieve_whenPropertyExist_andValueWithWhitespaces_shouldReturnTrimmed() throws Exception {
        // GIVEN
        final String placeholder = "P";
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register(placeholder, " VALUE  ");

        // WHEN
        final Optional<String> actualValue = patchProperties.retrieve(placeholder);

        // THEN
        assertThat(actualValue).contains("VALUE");
    }
}

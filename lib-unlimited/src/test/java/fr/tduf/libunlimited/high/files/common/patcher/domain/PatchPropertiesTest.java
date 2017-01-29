package fr.tduf.libunlimited.high.files.common.patcher.domain;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchPropertiesTest {

    @Test
    public void retrieve_whenPropertyDoesNotExist_shouldReturnEmpty() throws Exception {
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
    public void retrieve_whenPropertyExist_andValueWithWhitespaces_shouldReturnTrimmed() throws Exception {
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

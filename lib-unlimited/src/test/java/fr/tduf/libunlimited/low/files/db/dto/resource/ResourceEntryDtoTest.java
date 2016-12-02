package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceEntryDtoTest {
    @Test
    void getMissingLocales_whenNoItem_shouldNotReturnSpecialAnyLocale() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getMissingLocales())
                .hasSize(8)
                .doesNotContain(Locale.ANY);
    }

    @Test
    void getMissingLocales_whenGlobalItem_shouldReturnEmptySet() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withGlobalItem("GLOBAL")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getMissingLocales()).isEmpty();
    }
}

package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
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

    @Test
    void getItemForLocale_whenNoItem_shouldReturnEmpty() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getItemForLocale(Locale.ANY)).isEmpty();
    }

    @Test
    void getItemForLocale_whenExistingLocalItem_shouldReturnIt() {
        // GIVEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withLocale(FRANCE).withValue("FR").build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withItems(Collections.singletonList(expectedItem))
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getItemForLocale(FRANCE)).contains(expectedItem);
    }

    @Test
    void getItemForLocale_whenExistingGlobalItem_shouldReturnIt() {
        // GIVEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withGlobalValue("GLOBAL").build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withGlobalItem("GLOBAL")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getItemForLocale(FRANCE)).contains(expectedItem);
    }
}

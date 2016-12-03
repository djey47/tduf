package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.junit.jupiter.api.Test;

import static fr.tduf.libunlimited.common.game.domain.Locale.ANY;
import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.expectThrows;

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
                .withItems(singletonList(expectedItem))
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

    @Test
    void setValueForLocale_whenSpecialAnyLocale_andNonExistingGlobalItem_shouldThrowException() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN-THEN
        expectThrows(IllegalArgumentException.class, () -> resourceEntryDto.setValueForLocale("VAL", ANY));
    }

    @Test
    void setValueForLocale_whenSpecialAnyLocale_andExistingGlobalItem_shouldUpdateValue() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withGlobalItem("GLOBAL")
                .build();

        // WHEN
        resourceEntryDto.setValueForLocale("NEW GLOBAL", ANY);

        // THEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withGlobalValue("NEW GLOBAL").build();
        assertThat(resourceEntryDto.getItemForLocale(ANY)).contains(expectedItem);
    }

    @Test
    void setValueForLocale_whenNonExistingItem_shouldCreateIt() {
        // GIVEN
        ResourceItemDto expectedItem = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VAL")
                .build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN
        resourceEntryDto.setValueForLocale("VAL", FRANCE);

        // THEN
        assertThat(resourceEntryDto.getItemForLocale(FRANCE)).contains(expectedItem);
    }

    @Test
    void setValueForLocale_whenExistingItem_shouldUpdateValue() {
        // GIVEN
        ResourceItemDto initialItem = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VAL")
                .build();
        ResourceItemDto expectedItem = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("NEWVAL")
                .build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withItems(singletonList(initialItem))
                .build();

        // WHEN
        resourceEntryDto.setValueForLocale("NEWVAL", FRANCE);

        // THEN
        assertThat(resourceEntryDto.getItemForLocale(FRANCE)).contains(expectedItem);
    }
}

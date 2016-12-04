package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.junit.jupiter.api.Test;

import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceEntryDtoTest {
    @Test
    void getPresentLocales_shouldNotReturnDefaultLocale() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withDefaultItem("VAL")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getPresentLocales()).isEmpty();
    }

    @Test
    void getMissingLocales_whenNoItem_shouldNotReturnSpecialAnyLocale() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getMissingLocales())
                .hasSize(8)
                .doesNotContain(Locale.DEFAULT);
    }

    @Test
    void getMissingLocales_whenDefaultItem_shouldReturnEmptySet() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withDefaultItem("GLOBAL")
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
        assertThat(resourceEntryDto.getItemForLocale(Locale.DEFAULT)).isEmpty();
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
    void pickValue_whenDefaultValue_shouldReturnIt() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withDefaultItem("VAL")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.pickValue()).contains("VAL");
    }

    @Test
    void pickValue_whenNoValue_shouldReturnEmpty() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.pickValue()).isEmpty();
    }

    @Test
    void pickValue_whenNoDefaultValue_shouldReturnAnyLocalizedValue() {
        // GIVEN
        ResourceItemDto localizedItem = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VALFR")
                .build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withItems(singletonList(localizedItem))
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.pickValue()).contains("VALFR");
    }

    @Test
    void getValueForLocale_whenExistingItem_shouldReturnLocalizedValue() {
        // GIVEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withLocale(FRANCE).withValue("FR").build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withItems(singletonList(expectedItem))
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getValueForLocale(FRANCE)).contains("FR");
    }

    @Test
    void getValueForLocale_whenNoItem_shouldReturnEmpty() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder().forReference("REF").build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getValueForLocale(FRANCE)).isEmpty();
    }

    @Test
    void getValueForLocale_whenNoLocalizedItem_butDefaultItem_shouldReturnDefaultValue() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withDefaultItem("DEF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.getValueForLocale(FRANCE)).contains("DEF");
    }

    @Test
    void setValueForLocale_whenSpecialAnyLocale_andNonExistingGlobalItem_shouldCreateIt() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN
        resourceEntryDto.setValueForLocale("VAL", DEFAULT);

        // THEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withGlobalValue("VAL").build();
        assertThat(resourceEntryDto.getItemForLocale(DEFAULT)).contains(expectedItem);
    }

    @Test
    void setValueForLocale_whenSpecialAnyLocale_andExistingGlobalItem_shouldUpdateValue() {
        // GIVEN
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withDefaultItem("GLOBAL")
                .build();

        // WHEN
        resourceEntryDto.setValueForLocale("NEW GLOBAL", DEFAULT);

        // THEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withGlobalValue("NEW GLOBAL").build();
        assertThat(resourceEntryDto.getItemForLocale(DEFAULT)).contains(expectedItem);
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

    @Test
    void setDefaultValue_shouldSetDefaultItem() {
        // GIVEN
        ResourceItemDto initialItem = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VAL")
                .build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withItems(singletonList(initialItem))
                .build();

        // WHEN
        resourceEntryDto.setDefaultValue("NEWVAL");

        // THEN
        ResourceItemDto expectedItem = ResourceItemDto.builder().withGlobalValue("NEWVAL").build();
        assertThat(resourceEntryDto.getItemForLocale(DEFAULT)).contains(expectedItem);
        assertThat(resourceEntryDto.getItemForLocale(FRANCE)).contains(initialItem);
    }

    @Test
    void isGlobalized_whenOnlyDefaultItem_shouldReturnTrue() {
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withDefaultItem("VAL")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.isGlobalized()).isTrue();
    }
    @Test
    void isGlobalized_whenNoDefaultItem_shouldReturnFalse() {
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.isGlobalized()).isFalse();
    }

    @Test
    void isGlobalized_whenDefaultItem_andLocalizedItem_shouldReturnFalse() {
        ResourceItemDto localizedItem = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VALFR")
                .build();
        ResourceEntryDto resourceEntryDto = ResourceEntryDto.builder()
                .forReference("REF")
                .withItems(singletonList(localizedItem))
                .withDefaultItem("VALDEF")
                .build();

        // WHEN-THEN
        assertThat(resourceEntryDto.isGlobalized()).isFalse();
    }
}

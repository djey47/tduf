package fr.tduf.libunlimited.low.files.db.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbResourceDtoTest {

    @Test
    public void localeFromCode_whenCodeExists_shouldReturnKnownLocale() {
        //GIVEN-WHEN-THEN
        assertThat(DbResourceEnhancedDto.Locale.fromCode("ge")).isEqualTo(DbResourceEnhancedDto.Locale.GERMANY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void localeFromCode_whenCodeDoesNotExist_shouldThrowException() {
        //GIVEN-WHEN-THEN
        DbResourceEnhancedDto.Locale.fromCode("xx");
    }
}
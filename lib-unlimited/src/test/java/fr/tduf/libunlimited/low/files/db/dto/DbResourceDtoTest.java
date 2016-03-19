package fr.tduf.libunlimited.low.files.db.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbResourceDtoTest {

    @Test
    public void localeFromCode_whenCodeExists_shouldReturnKnownLocale() {
        //GIVEN-WHEN-THEN
        assertThat(DbResourceDto.Locale.fromCode("ge")).isEqualTo(DbResourceDto.Locale.GERMANY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void localeFromCode_whenCodeDoesNotExist_shouldThrowException() {
        //GIVEN-WHEN-THEN
        DbResourceDto.Locale.fromCode("xx");
    }
}
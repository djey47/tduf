package fr.tduf.libunlimited.low.files.db.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbResourceDtoTest {

    @Test
    public void localeFromCode_whenCodeExists_shouldReturnKnownLocale() {
        //GIVEN-WHEN-THEN
        assertThat(Locale.fromCode("ge")).isEqualTo(Locale.GERMANY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void localeFromCode_whenCodeDoesNotExist_shouldThrowException() {
        //GIVEN-WHEN-THEN
        Locale.fromCode("xx");
    }
}
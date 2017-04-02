package fr.tduf.libunlimited.low.files.db.dto.resource;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbResourceDtoTest {

    @Test
    void localeFromCode_whenCodeExists_shouldReturnKnownLocale() {
        //GIVEN-WHEN-THEN
        assertThat(Locale.fromCode("ge")).isEqualTo(Locale.GERMANY);
    }

    @Test
    void localeFromCode_whenCodeDoesNotExist_shouldThrowException() {
        //GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> Locale.fromCode("xx"));
    }
}

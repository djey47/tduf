package fr.tduf.gui.database.converter;

import fr.tduf.libunlimited.common.game.domain.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseLocaleToStringConverterTest {
    
    private final DatabaseLocaleToStringConverter converter = new DatabaseLocaleToStringConverter();

    @Test
    void toString_shouldReturnCorrectLabel() {
        // given-when-then
        assertThat(converter.toString(Locale.FRANCE)).isEqualTo("[fr] Français");
    }

    @Test
    void fromString_shouldReturnNull() {
        assertThat(converter.fromString("")).isNull();
    }    
}
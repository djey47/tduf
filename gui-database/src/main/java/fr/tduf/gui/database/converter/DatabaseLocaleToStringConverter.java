package fr.tduf.gui.database.converter;

import fr.tduf.libunlimited.common.game.domain.Locale;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.common.DisplayConstants.DATA_FORMAT_LOCALE_OBJECT;

public class DatabaseLocaleToStringConverter extends StringConverter<Locale> {

    @Override
    public String toString(Locale locale) {
        return String.format(DATA_FORMAT_LOCALE_OBJECT, locale.getCode(), locale.getLanguage());
    }

    @Override
    public Locale fromString(String label) {
        return null;
    }
}

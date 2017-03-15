package fr.tduf.gui.database.plugins.mapping.converter;

import javafx.util.StringConverter;

import static fr.tduf.gui.database.common.DisplayConstants.VALUE_NO;
import static fr.tduf.gui.database.common.DisplayConstants.VALUE_YES;

public class BooleanStatusToDisplayConverter extends StringConverter<Boolean> {
    @Override
    public String toString(Boolean object) {
        return (object != null && object) ? VALUE_YES : VALUE_NO;
    }

    @Override
    public Boolean fromString(String string) {
        return VALUE_YES.equals(string);
    }
}

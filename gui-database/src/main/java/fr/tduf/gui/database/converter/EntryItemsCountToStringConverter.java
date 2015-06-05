package fr.tduf.gui.database.converter;

import javafx.util.StringConverter;

public class EntryItemsCountToStringConverter extends StringConverter<Integer> {
    @Override
    public String toString(Integer entryItemsCount) {
        if (entryItemsCount == -1) {
            return "";
        }
        return "/ " + entryItemsCount;
    }

    @Override
    public Integer fromString(String string) {
        return null;
    }
}
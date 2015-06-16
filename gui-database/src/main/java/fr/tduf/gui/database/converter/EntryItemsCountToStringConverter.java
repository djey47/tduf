package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import javafx.util.StringConverter;

public class EntryItemsCountToStringConverter extends StringConverter<Integer> {
    @Override
    public String toString(Integer entryItemsCount) {
        if (entryItemsCount == -1) {
            return DisplayConstants.LABEL_ITEM_ENTRY_COUNT_DEFAULT;
        }
        return String.format(DisplayConstants.LABEL_ITEM_ENTRY_COUNT, entryItemsCount);
    }

    @Override
    public Integer fromString(String string) {
        return null;
    }
}
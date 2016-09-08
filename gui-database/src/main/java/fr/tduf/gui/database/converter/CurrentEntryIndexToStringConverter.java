package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import javafx.util.StringConverter;

public class CurrentEntryIndexToStringConverter extends StringConverter<Integer> {
    @Override
    public String toString(Integer entryIndex) {
        if (entryIndex == -1) {
            return DisplayConstants.LABEL_ITEM_ENTRY_INDEX_DEFAULT;
        }
        return Integer.toString(entryIndex + 1);
    }

    @Override
    public Integer fromString(String displayedIndex) {

        if (displayedIndex.matches("\\d+")) {
            return Integer.valueOf(displayedIndex) - 1;
        }
        return 0;
    }
}

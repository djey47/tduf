package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import javafx.util.StringConverter;

public class CurrentEntryIndexToStringConverter extends StringConverter<Long> {
    @Override
    public String toString(Long entryIndex) {
        if (entryIndex == -1) {
            return DisplayConstants.LABEL_ITEM_ENTRY_INDEX_DEFAULT;
        }
        return "" + (entryIndex + 1);
    }

    @Override
    public Long fromString(String displayedIndex) {

        if (displayedIndex.matches("\\d+")) {
            return Long.valueOf(displayedIndex) - 1;
        }
        return 0L;
    }
}

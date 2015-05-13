package fr.tduf.gui.database.converter;

import javafx.util.StringConverter;

public class CurrentEntryIndexToStringConverter extends StringConverter<Integer> {
    @Override
    public String toString(Integer entryIndex) {
        if (entryIndex == -1) {
            return "<?>";
        }
        return "" + (entryIndex + 1);

    }

    @Override
    public Integer fromString(String string) {
        return null;
    }
}

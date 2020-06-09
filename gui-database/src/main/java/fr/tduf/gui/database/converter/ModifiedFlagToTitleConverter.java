package fr.tduf.gui.database.converter;

import javafx.util.StringConverter;

import static fr.tduf.gui.database.common.DisplayConstants.TITLE_APPLICATION;
import static fr.tduf.gui.database.common.DisplayConstants.TITLE_FRAGMENT_MODIFIED;

/**
 * Converts value of modified flag to application title
 */
public class ModifiedFlagToTitleConverter extends StringConverter<Boolean> {
    @Override
    public String toString(Boolean isModified) {
        String modifiedMark = isModified ? TITLE_FRAGMENT_MODIFIED : "";
        return String.format("%s %s", TITLE_APPLICATION, modifiedMark);
    }

    @Override
    public Boolean fromString(String s) {
        return null;
    }
}

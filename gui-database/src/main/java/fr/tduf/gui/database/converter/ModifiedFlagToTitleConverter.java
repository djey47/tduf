package fr.tduf.gui.database.converter;

import com.esotericsoftware.minlog.Log;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.common.DisplayConstants.*;

/**
 * Converts value of modified flag to application title
 */
public class ModifiedFlagToTitleConverter extends StringConverter<Boolean> {
    @Override
    public String toString(Boolean isModified) {
        String modifiedMark = isModified ? TITLE_FRAGMENT_MODIFIED : "";
        return String.format(TITLE_FORMAT_APP, TITLE_APPLICATION, getDebugModeLabel(), modifiedMark);
    }

    @Override
    public Boolean fromString(String s) {
        return null;
    }

    private static String getDebugModeLabel() {
        if (Log.TRACE) {
            return String.format(TITLE_FORMAT_MODE, TITLE_MODE_TRACE);
        }
        if (Log.DEBUG) {
            return String.format(TITLE_FORMAT_MODE, TITLE_MODE_DEBUG);
        }
        return "";
    }
}

package fr.tduf.gui.database.plugins.iks.converter;

import javafx.util.StringConverter;

import java.util.Map;

import static fr.tduf.gui.database.plugins.iks.common.DisplayConstants.FORMAT_IK_ITEM_LABEL;

public class IKReferenceToItemConverter extends StringConverter<Map.Entry<Integer, String>> {
    @Override
    public String toString(Map.Entry<Integer, String> refEntry) {
        return String.format(FORMAT_IK_ITEM_LABEL, refEntry.getKey(), refEntry.getValue());
    }

    @Override
    public Map.Entry<Integer, String> fromString(String identifier) {
        return null;
    }
}

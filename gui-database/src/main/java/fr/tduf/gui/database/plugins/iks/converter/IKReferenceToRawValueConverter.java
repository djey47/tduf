package fr.tduf.gui.database.plugins.iks.converter;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.iks.common.DisplayConstants;
import javafx.util.StringConverter;

import java.util.Map;

public class IKReferenceToRawValueConverter extends StringConverter<Map.Entry<Integer, String>> {
    private static final String THIS_CLASS_NAME = IKReferenceToRawValueConverter.class.getSimpleName();

    private final Map<Integer, String> reference;

    public IKReferenceToRawValueConverter(Map<Integer, String> reference) {
        this.reference = reference;
    }

    @Override
    public String toString(Map.Entry<Integer, String> refEntry) {
        if (refEntry == null) {
            return DisplayConstants.LABEL_IK_RAW_VALUE_DEFAULT;
        }
        return Integer.toString(refEntry.getKey());
    }

    @Override
    public Map.Entry<Integer, String> fromString(String ikIdentifierAsString) {
        try {
            int id = Integer.valueOf(ikIdentifierAsString);
            return reference.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(id))
                    .findAny()
                    .orElse(null);
        } catch (NumberFormatException nfe) {
            Log.error(THIS_CLASS_NAME, "Unable to resolve IK reference from raw value: " + ikIdentifierAsString, nfe);
            return null;
        }
    }
}

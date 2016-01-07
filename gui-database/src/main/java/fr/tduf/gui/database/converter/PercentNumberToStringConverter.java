package fr.tduf.gui.database.converter;

import com.google.common.base.Strings;
import fr.tduf.gui.database.common.DisplayConstants;
import javafx.util.converter.NumberStringConverter;

/**
 * Converts a percent number ranging from 0 to 100 from/to a raw value in range [0..1].
 * Decimal separator is always the dot, whatever the locale is.
 */
public class PercentNumberToStringConverter extends NumberStringConverter {

    @Override
    public String toString(Number percentNumber) {
        if (percentNumber == null) {
            return DisplayConstants.LABEL_ITEM_PERCENT_DEFAULT;
        }

        float rawNumber = percentNumber.floatValue() / 100;
        return super.toString(rawNumber).replace(",", ".");
    }

    @Override
    public Number fromString(String percentRawValue) {
        if (Strings.isNullOrEmpty(percentRawValue)) {
            return null;
        }

        Float percentNumber = Float.valueOf(percentRawValue) * 100;
        return super.fromString(percentNumber.toString());
    }
}
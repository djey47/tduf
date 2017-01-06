package fr.tduf.gui.database.plugins.percent.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isEmpty(percentRawValue)) {
            return null;
        }

        Float percentNumber = Float.valueOf(percentRawValue) * 100;
        return super.fromString(percentNumber.toString());
    }
}
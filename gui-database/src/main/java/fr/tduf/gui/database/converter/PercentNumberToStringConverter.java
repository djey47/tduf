package fr.tduf.gui.database.converter;

import javafx.util.converter.NumberStringConverter;

public class PercentNumberToStringConverter extends NumberStringConverter {

    @Override
    public String toString(Number percentNumber) {
        if (percentNumber == null) {
            return "";
        }

        // TODO Take current decimal separator into account (UK = . )
        return super.toString(percentNumber).replace(",", ".");
    }

    @Override
    public Number fromString(String percentRawValue) {
        if (percentRawValue == null) {
            return null;
        }

        // TODO Take current decimal separator into account (UK = . )
        return super.fromString(percentRawValue.replace(".", ","));
    }
}

package fr.tduf.gui.database.converter;

import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class PercentNumberToStringConverter extends StringConverter<Number> {

    private NumberStringConverter numberStringConverter = new NumberStringConverter();

    @Override
    public String toString(Number percentNumber) {
        if (percentNumber == null) {
            return "";
        }

        // TODO Take current decimal separator into account (UK = . )
        return numberStringConverter.toString(percentNumber).replace(",", ".");
    }

    @Override
    public Number fromString(String percentRawValue) {
        if (percentRawValue == null) {
            return null;
        }

        // TODO Take current decimal separator into account (UK = . )
        return numberStringConverter.fromString(percentRawValue.replace("." ,","));
    }
}

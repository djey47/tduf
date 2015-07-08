package fr.tduf.gui.database.converter;

import com.google.common.base.Strings;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;

/**
 * Converts a bitfield item into a raw value String and vice-versa.
 */
public class BitfieldToStringConverter extends StringConverter<Boolean>{

    public static final int MAXIMUM_BIT_COUNT = 18;
    private static final char BINARY_ZERO = '0';
    private static final char BINARY_ONE = '1';

    private final SimpleStringProperty rawValueProperty;
    private final int bitIndex;

    public BitfieldToStringConverter(int bitIndex, SimpleStringProperty rawValueProperty) {
        this.rawValueProperty = rawValueProperty;
        this.bitIndex = bitIndex;
    }

    @Override
    public String toString(Boolean object) {
        String rawValue = rawValueProperty.getValue();

        if ("".equals(rawValue)) {
            rawValue = String.valueOf(BINARY_ZERO);
        }

        String binaryString = Integer.toBinaryString(Integer.valueOf(rawValue));
        binaryString = Strings.padStart(binaryString, MAXIMUM_BIT_COUNT, BINARY_ZERO);

        char[] chars = binaryString.toCharArray();
        chars[binaryString.length() - bitIndex] = object ? BINARY_ONE : BINARY_ZERO;

        return Integer.valueOf(Integer.parseInt(new String(chars), 2)).toString();
    }

    @Override
    public Boolean fromString(String rawValue) {
        if ("".equals(rawValue)) {
            rawValue = String.valueOf(BINARY_ZERO);
        }

        String binaryString = Integer.toBinaryString(Integer.valueOf(rawValue));
        if (bitIndex > binaryString.length()) {
            return false;
        }

        int position = binaryString.length() - bitIndex;
        String bitValue = binaryString.substring(position, position + 1);
        return String.valueOf(BINARY_ONE).equals(bitValue);
    }
}

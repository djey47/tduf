package fr.tduf.gui.database.converter;

import com.google.common.base.Strings;
import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Converts a bitfield item into a raw value String and vice-versa.
 */
public class BitfieldToStringConverter extends StringConverter<Boolean>{

    public static final int MAXIMUM_BIT_COUNT = 18;
    private static final char BINARY_ZERO = '0';
    private static final char BINARY_ONE = '1';

    private final SimpleStringProperty rawValueProperty;
    private final int bitIndex;
    private final DbDto.Topic currentTopic;
    private BitfieldHelper bitfieldHelper;

    public BitfieldToStringConverter(DbDto.Topic topic, int bitIndex, SimpleStringProperty rawValueProperty, BitfieldHelper bitfieldHelper) {
        requireNonNull(bitfieldHelper, "BitfieldHelper instance is required.");

        this.currentTopic = topic;
        this.rawValueProperty = rawValueProperty;
        this.bitIndex = bitIndex;
        this.bitfieldHelper = bitfieldHelper;
    }

    @Override
    public String toString(Boolean object) {
        // TODO extract to Bitfield helper to generate raw value
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

        Optional<List<Boolean>> resolvedValues = bitfieldHelper.resolve(currentTopic, rawValue);
        if (resolvedValues.isPresent() && bitIndex <= resolvedValues.get().size()) {
            return resolvedValues.get().get(bitIndex - 1);
        } else {
            return false;
        }
    }
}

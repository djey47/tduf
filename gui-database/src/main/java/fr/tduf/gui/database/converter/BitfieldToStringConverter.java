package fr.tduf.gui.database.converter;

import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Converts a bitfield item into a raw value String and vice-versa.
 */
public class BitfieldToStringConverter extends StringConverter<Boolean>{

    private static final char BINARY_ZERO = '0';

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
    public String toString(Boolean switchState) {
        String rawValue = rawValueProperty.getValue();
        if (StringUtils.isEmpty(rawValue)) {
            rawValue = String.valueOf(BINARY_ZERO);
        }

        return bitfieldHelper.updateRawValue(currentTopic, rawValue, bitIndex, switchState).get();
    }

    @Override
    public Boolean fromString(String bitfieldRawValue) {
        String rawValue = bitfieldRawValue;
        if (StringUtils.isEmpty(bitfieldRawValue)) {
            rawValue = String.valueOf(BINARY_ZERO);
        }

        Optional<List<Boolean>> resolvedValues = bitfieldHelper.resolve(currentTopic, rawValue);
        if (resolvedValues.isPresent() && bitIndex <= resolvedValues.get().size()) {
            return resolvedValues.get().get(bitIndex - 1);
        }

        return false;
    }
}

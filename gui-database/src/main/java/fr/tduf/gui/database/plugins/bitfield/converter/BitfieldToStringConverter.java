package fr.tduf.gui.database.plugins.bitfield.converter;

import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.StringProperty;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.database.plugins.bitfield.common.DisplayConstants.VALUE_BINARY_ZERO;
import static java.util.Objects.requireNonNull;

/**
 * Converts a bitfield item into a raw value String and vice-versa.
 */
public class BitfieldToStringConverter extends StringConverter<Boolean>{
    private final StringProperty rawValueProperty;
    private final int bitIndex;
    private final DbDto.Topic currentTopic;
    private final BitfieldHelper bitfieldHelper;

    public BitfieldToStringConverter(DbDto.Topic topic, int bitIndex, StringProperty rawValueProperty, BitfieldHelper bitfieldHelper) {
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
            rawValue = String.valueOf(VALUE_BINARY_ZERO);
        }

        return bitfieldHelper.updateRawValue(currentTopic, rawValue, bitIndex, switchState)
                .orElseThrow(() -> new IllegalStateException("No reference for bitfield at index " + bitIndex));
    }

    @Override
    public Boolean fromString(String bitfieldRawValue) {
        String rawValue = bitfieldRawValue;
        if (StringUtils.isEmpty(bitfieldRawValue)) {
            rawValue = String.valueOf(VALUE_BINARY_ZERO);
        }

        Optional<List<Boolean>> resolvedValues = bitfieldHelper.resolve(currentTopic, rawValue);
        if (resolvedValues.isPresent() && bitIndex <= resolvedValues.get().size()) {
            return resolvedValues.get().get(bitIndex - 1);
        }

        return false;
    }
}

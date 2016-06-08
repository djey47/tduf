package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.framework.base.Strings;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

/**
 * Helper class to access bitfield information and bring bitfield reference.
 */
public class BitfieldHelper extends MetaDataHelper {

    private static final char BINARY_ZERO = '0';
    private static final char BINARY_ONE = '1';

    /**
     * @param topic : database topic to get bitfield reference from
     * @return a list of bitfield reference if available, empty otherwise.
     */
    public Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> getBitfieldReferenceForTopic(DbDto.Topic topic) {
        return databaseMetadataObject.getTopics().stream()

                .filter(topicMetaData -> topicMetaData.getTopic() == topic)

                .findAny()

                .map(DbMetadataDto.TopicMetadataDto::getBitfields);
    }

    /**
     * @param topic             : database topic to get bitfield reference from
     * @param bitfieldRawValue  : value of bitfield, as read in database topic
     * @return list of switch values (ON: true/OFF: false) when reference is available, empty otherwise.
     */
    public Optional<List<Boolean>> resolve(DbDto.Topic topic, String bitfieldRawValue) {
        requireNonNull(bitfieldRawValue, "Raw value is required.");

        return getBitfieldReferenceForTopic(topic)

                .map(reference -> resolveWithReference(reference, bitfieldRawValue));
    }

    /**
     * @param topic             : database topic to get bitfield reference from
     * @param bitfieldRawValue  : current value of bitfield, as read in database topic
     * @param bitIndex          : 1-based rank of modified bit in bitfield
     * @param switchState       : value of modified bit (true = 1, false = 0)
     * @return updated raw value if specified bit change does apply to it , or empty if no reference is available.
     */
    public Optional<String> updateRawValue(DbDto.Topic topic, String bitfieldRawValue, int bitIndex, boolean switchState) {

        return getBitfieldReferenceForTopic(topic)

                .map(reference -> {
                    int bitCount = reference.size();
                    if (bitIndex < 1 || bitIndex > bitCount) {
                        return bitfieldRawValue;
                    }

                    String binaryString = Integer.toBinaryString(Integer.valueOf(bitfieldRawValue));
                    binaryString = Strings.padStart(binaryString, bitCount, BINARY_ZERO);

                    char[] chars = binaryString.toCharArray();
                    chars[binaryString.length() - bitIndex] = switchState ? BINARY_ONE : BINARY_ZERO;

                    return Integer.toString(Integer.parseInt(new String(chars), 2));
                });
    }

    private List<Boolean> resolveWithReference(List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto> reference, String bitfieldRawValue) {
        String binaryString = Integer.toBinaryString(Integer.parseInt(bitfieldRawValue));
        String paddedBinaryValue = Strings.padStart(binaryString, reference.size(), BINARY_ZERO);

        List<Boolean> switches = new ArrayList<>();
        paddedBinaryValue.chars()

                .mapToObj(bit -> BINARY_ONE == (char) bit)

                .collect(toCollection(LinkedList::new))

                .descendingIterator()

                .forEachRemaining(switches::add);

        return switches;
    }
}

package fr.tduf.libunlimited.high.files.db.common.helper;

import com.google.common.base.Strings;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

/**
 * Helper class to access bitfield information and bring bitfield reference.
 */
public class BitfieldHelper {

    private static final char BINARY_ZERO = '0';
    private static final char BINARY_ONE = '1';

    private DbMetadataDto databaseMetadataObject;

    public BitfieldHelper() throws IOException, URISyntaxException {
        loadDatabaseReference();
    }

    /**
     * @param topic : database topic to get bitfield reference from
     * @return a list of bitfield reference if available, empty otherwise.
     */
    public Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> getBitfieldReferenceForTopic(DbDto.Topic topic) {
        return databaseMetadataObject.getTopics().stream()

                .filter((topicMetaData) -> topicMetaData.getTopic() == topic)

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

                .map((reference) -> resolveWithReference(reference, bitfieldRawValue));
    }

    private List<Boolean> resolveWithReference(List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto> reference, String bitfieldRawValue) {
        String binaryString = Integer.toBinaryString(Integer.parseInt(bitfieldRawValue));
        String paddedBinaryValue = Strings.padStart(binaryString, reference.size(), BINARY_ZERO);

        List<Boolean> switches = new ArrayList<>();
        paddedBinaryValue.chars()

                .mapToObj((bit) -> BINARY_ONE == (char) bit)

                .collect(toCollection(LinkedList::new))

                .descendingIterator()

                .forEachRemaining(switches::add);

        return switches;
    }

    private void loadDatabaseReference() throws IOException, URISyntaxException {
        databaseMetadataObject = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.class, "/files/db/databaseMetadata.json");
    }

    DbMetadataDto getDatabaseMetadataObject() {
        return databaseMetadataObject;
    }
}
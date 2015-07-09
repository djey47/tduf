package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Helper class to access bitfield information and bring bitfield reference.
 */
public class BitfieldHelper {

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

    private void loadDatabaseReference() throws IOException, URISyntaxException {
        databaseMetadataObject = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.class, "/files/db/databaseMetadata.json");
    }

    DbMetadataDto getDatabaseMetadataObject() {
        return databaseMetadataObject;
    }
}
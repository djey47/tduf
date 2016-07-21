package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

/**
 * Helper class to access structure information.
 */
public class DatabaseStructureHelper extends MetaDataHelper {
    /**
     * @return true if provided topic supports REF, false otherwise
     */
    public boolean isRefSupportForTopic(DbDto.Topic topic) {
        return databaseMetadataObject.getTopics().stream()
                .filter(topicMetaData -> topicMetaData.getTopic() == topic)
                .findAny()
                .map(DbMetadataDto.TopicMetadataDto::isRefSupport)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No metadata found for topic: " + topic));
    }
}

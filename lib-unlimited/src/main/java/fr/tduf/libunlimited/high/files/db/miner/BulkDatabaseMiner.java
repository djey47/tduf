package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Class providing utility methods to request data from JSON database.
 */
public class BulkDatabaseMiner {

    private final List<DbDto> topicObjects;

    /**
     * Provides miner instance.
     * @param topicObjects
     * @return
     */
    public static BulkDatabaseMiner load(List<DbDto> topicObjects) {
        requireNonNull(topicObjects, "A list of database topic objects is required.");

        return new BulkDatabaseMiner(topicObjects);
    }

    private BulkDatabaseMiner(List<DbDto> topicObjects) {
        this.topicObjects = topicObjects;
    }

    /**
     *
     * @param topic
     * @return
     */
    public List<DbResourceDto> getAllResourcesFromTopic(DbDto.Topic topic) {
        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findAny().get().getResources();
    }
}

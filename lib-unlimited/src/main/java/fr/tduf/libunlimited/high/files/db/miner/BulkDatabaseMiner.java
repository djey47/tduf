package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
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

    /**
     *
     * @param locale
     * @param topic
     * @return
     */
    public DbResourceDto getResourceFromTopicAndLocale(DbDto.Topic topic, DbResourceDto.Locale locale) {
        return getAllResourcesFromTopic(topic).stream()

                .filter((resourceObject) -> resourceObject.getLocale() == locale)

                .findAny().get();
    }

    /**
     *
     * @param topic
     * @return
     */
    public DbDto getDatabaseTopic(DbDto.Topic topic) {
        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findAny().get();
    }

    /**
     *
     * @param topicReference
     * @return
     */
    public DbDto getDatabaseTopicFromReference(String topicReference) {
        if (topicReference == null) {
            return null;
        }

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getRef().equals(topicReference))

                .findAny().get();
    }

    /**
     *
     * @param entryInternalIdentifier
     * @param topic
     * @return
     */
    public DbDataDto.Entry getContentEntryFromTopicWithInternalIdentifier(long entryInternalIdentifier, DbDto.Topic topic) {
        return getDatabaseTopic(topic).getData().getEntries().stream()

                .filter((entry) -> entry.getId() == entryInternalIdentifier)

                .findAny().get();
    }

    /**
     *
     * @param reference
     * @param topic
     * @param locale
     * @return
     */
    public DbResourceDto.Entry getResourceEntryFromTopicAndLocaleWithReference(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {
        return getResourceFromTopicAndLocale(topic, locale).getEntries().stream()

                .filter((entry) -> entry.getReference().equals(reference))

                .findAny().get();
    }
}
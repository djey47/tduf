package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Class providing utility methods to request data from database objects.
 */
public class BulkDatabaseMiner {

    private final List<DbDto> topicObjects;

    /**
     * @param topicObjects  : list of per-topic database objects
     * @return a miner instance.
     */
    public static BulkDatabaseMiner load(List<DbDto> topicObjects) {
        requireNonNull(topicObjects, "A list of per-topic database objects is required.");

        return new BulkDatabaseMiner(topicObjects);
    }

    private BulkDatabaseMiner(List<DbDto> topicObjects) {
        this.topicObjects = topicObjects;
    }

    /**
     * @param topic : topic in TDU Database to search resources from
     * @return a list of per-locale database resource objects.
     */
    public List<DbResourceDto> getAllResourcesFromTopic(DbDto.Topic topic) {
        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findAny().get().getResources();
    }

    /**
     * @param locale    : game language to fetch related resources
     * @param topic     : topic in TDU Database to search resources from
     * @return an optional value: either such a resource object if it exists, else empty.
     */
    public Optional<DbResourceDto> getResourceFromTopicAndLocale(DbDto.Topic topic, DbResourceDto.Locale locale) {
        return getAllResourcesFromTopic(topic).stream()

                .filter((resourceObject) -> resourceObject.getLocale() == locale)

                .findAny();
    }

    /**
     * @param topic : topic in TDU Database to search
     * @return database object related to this topic.
     */
    public DbDto getDatabaseTopic(DbDto.Topic topic) {
        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findAny().get();
    }

    /**
     * @param topicReference    : identifier in database structure
     * @return database object having specified reference.
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
     * @param entryIdentifier   : unique identifier of entry (TDUF specific)
     * @param topic             : topic in TDU Database to search
     * @return database entry having specified identifier.
     */
    public DbDataDto.Entry getContentEntryFromTopicWithInternalIdentifier(long entryIdentifier, DbDto.Topic topic) {
        return getDatabaseTopic(topic).getData().getEntries().stream()

                .filter((entry) -> entry.getId() == entryIdentifier)

                .findAny().get();
    }

    /**
     * @param reference : unique identifier of resource
     * @param topic     : topic in TDU Database to search
     * @param locale    : game language to fetch related resources
     * @return an optional value: either such a resource entry if it exists, else absent.
     */
    public Optional<DbResourceDto.Entry> getResourceEntryFromTopicAndLocaleWithReference(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {
        Optional<DbResourceDto> resourceFromTopicAndLocale = getResourceFromTopicAndLocale(topic, locale);

        if (!resourceFromTopicAndLocale.isPresent()) {
            return Optional.empty();
        }

        return resourceFromTopicAndLocale.get().getEntries().stream()

                .filter((entry) -> entry.getReference().equals(reference))

                .findAny();
    }
}
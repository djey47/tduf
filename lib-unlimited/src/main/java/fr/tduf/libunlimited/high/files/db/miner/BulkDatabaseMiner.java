package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

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
    public Optional<List<DbResourceDto>> getAllResourcesFromTopic(DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getAllResourcesFromTopic(" + topic + ")");

        Optional<DbDto> dbDto = topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findAny();

        if (!dbDto.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(dbDto.get().getResources());
    }

    /**
     * @param locale    : game language to fetch related resources
     * @param topic     : topic in TDU Database to search resources from
     * @return an optional value: either such a resource object if it exists, else empty.
     */
    public Optional<DbResourceDto> getResourceFromTopicAndLocale(DbDto.Topic topic, DbResourceDto.Locale locale) {
//        System.out.println(new Date().getTime() + " - getResourceFromTopicAndLocale(" + topic + ", " + locale + ")");

        Optional<List<DbResourceDto>> allResourcesFromTopic = getAllResourcesFromTopic(topic);

        if (!allResourcesFromTopic.isPresent()) {
            return Optional.empty();
        }

        return allResourcesFromTopic.get().stream()

                .filter((resourceObject) -> resourceObject.getLocale() == locale)

                .findAny();
    }

    /**
     * @param topic : topic in TDU Database to search
     * @return database object related to this topic.
     */
    public Optional<DbDto> getDatabaseTopic(DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getDatabaseTopic(" + topic + ")");

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findAny();
    }

    /**
     * @param topicReference    : identifier in database structure
     * @return database object having specified reference.
     */
    public DbDto getDatabaseTopicFromReference(String topicReference) {
//        System.out.println(new Date().getTime() + " - getDatabaseTopicFromReference(" + topicReference + ")");

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
//        System.out.println(new Date().getTime() + " - getContentEntryFromTopicWithInternalIdentifier(" + entryIdentifier + ", " + topic + ")");

        return getDatabaseTopic(topic).get().getData().getEntries().stream()

                .filter((entry) -> entry.getId() == entryIdentifier)

                .findAny().get();
    }

    /**
     * @param ref       : external identifier of entry
     * @param topic     : topic in TDU Database to search
     * @return database entry having specified reference as identifier.
     */
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithRef(String ref, DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getContentEntryFromTopicWithRef(" + ref + ", " + topic + ")");

        Optional<DbDto> potentialTopicObject = getDatabaseTopic(topic);
        if (!potentialTopicObject.isPresent()) {
            return Optional.empty();
        }
        DbDto topicObject = potentialTopicObject.get();

        OptionalInt potentialUidFieldRank = getUidFieldRank(topicObject);
        if (!potentialUidFieldRank.isPresent()) {
            return Optional.empty();
        }
        int uidFieldRank = potentialUidFieldRank.getAsInt();

        return topicObject.getData().getEntries().stream()

                .filter((entry) -> entryHasForIdentifier(entry, uidFieldRank, ref))

                .findAny();
    }

    /**
     * @param reference : unique identifier of resource
     * @param topic     : topic in TDU Database to search
     * @param locale    : game language to fetch related resources
     * @return an optional value: either such a resource entry if it exists, else absent.
     */
    public Optional<DbResourceDto.Entry> getResourceEntryFromTopicAndLocaleWithReference(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {
//        System.out.println(new Date().getTime() + " - getResourceEntryFromTopicAndLocaleWithReference(" + reference + ", " + topic + ", " + locale + ")");

        Optional<DbResourceDto> resourceFromTopicAndLocale = getResourceFromTopicAndLocale(topic, locale);

        if (!resourceFromTopicAndLocale.isPresent()) {
            return Optional.empty();
        }

        return resourceFromTopicAndLocale.get().getEntries().stream()

                .filter((entry) -> entry.getReference().equals(reference))

                .findAny();
    }

    private static OptionalInt getUidFieldRank(DbDto topicObject) {
        return topicObject.getStructure().getFields().stream()

                .filter((structureField) -> DbStructureDto.FieldType.UID == structureField.getFieldType())

                .mapToInt(DbStructureDto.Field::getRank)

                .findAny();
    }

    private static boolean entryHasForIdentifier(DbDataDto.Entry entry, int uidFieldRank, String ref) {
        return entry.getItems().stream()

                .filter((item) -> item.getFieldRank() == uidFieldRank
                        && item.getRawValue().equals(ref))

                .findAny()

                .isPresent();
    }

    List<DbDto> getTopicObjects() {
        return topicObjects;
    }
}
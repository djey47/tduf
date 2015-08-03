package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing utility methods to request data from database objects.
 */
// TODO unit tests (0.7.0+)
// TODO use debug logs to get performance info (0.7.0+)
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

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getTopic() == topic)

                .findAny()

                .map(DbDto::getResources);
    }

    /**
     * @param locale    : game language to fetch related resources
     * @param topic     : topic in TDU Database to search resources from
     * @return an optional value: either such a resource object if it exists, else empty.
     */
    public Optional<DbResourceDto> getResourceFromTopicAndLocale(DbDto.Topic topic, DbResourceDto.Locale locale) {
//        System.out.println(new Date().getTime() + " - getResourceFromTopicAndLocale(" + topic + ", " + locale + ")");

        return getAllResourcesFromTopic(topic)

                .map((allResourcesFromTopic) -> allResourcesFromTopic.stream()

                        .filter((resourceObject) -> resourceObject.getLocale() == locale)

                        .findAny()

                        .orElse(null));
    }

    /**
     * @param topic : topic in TDU Database to search
     * @return database object related to this topic.
     */
    public Optional<DbDto> getDatabaseTopic(DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getDatabaseTopic(" + topic + ")");

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getTopic() == topic)

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
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithInternalIdentifier(long entryIdentifier, DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getContentEntryFromTopicWithInternalIdentifier(" + entryIdentifier + ", " + topic + ")");

        return getDatabaseTopic(topic).get().getData().getEntries().stream()

                .filter((entry) -> entry.getId() == entryIdentifier)

                .findAny();
    }

    /**
     * @param ref       : external identifier of entry
     * @param topic     : topic in TDU Database to search
     * @return database entry having specified reference as identifier.
     */
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithReference(String ref, DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getContentEntryFromTopicWithReference(" + ref + ", " + topic + ")");

        return getDatabaseTopic(topic)
                .map((topicObject) -> topicObject.getData().getEntries().stream()

                        .filter((entry) -> contentEntryHasForReference(entry, ref, topicObject.getStructure().getFields()))

                        .findAny()

                        .orElse(null));
    }

    /**
     * @param entry     : entry containing items to be looked at
     * @param fieldRank : rank of field content item belongs to
     * @return item if it exists, empty otherwise.
     */
    public static Optional<DbDataDto.Item> getContentItemFromEntryAtFieldRank(DbDataDto.Entry entry, int fieldRank) {
//        System.out.println(new Date().getTime() + " - getContentItemFromEntryAtFieldRank(" + entry + "," + fieldRank + ")");

        return entry.getItems().stream()

                .filter((contentItem) -> contentItem.getFieldRank() == fieldRank)

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

        return getResourceFromTopicAndLocale(topic, locale)

                .map((resourceFromTopicAndLocale) -> resourceFromTopicAndLocale.getEntries().stream()

                        .filter((entry) -> entry.getReference().equals(reference))

                        .findAny()

                        .orElse(null));
    }

    /**
     * @param reference             : unique identifier of resource
     * @param topicResourceObjects  : list of topic resource objects to search into
     * @return a set of corresponding values
     */
    public static Set<String> getAllResourceValuesForReference(String reference, List<DbResourceDto> topicResourceObjects) {
//        System.out.println(new Date().getTime() + " - getAllResourceValuesForReference(" + reference + "," + topicResourceObjects + ")");

        return topicResourceObjects.stream()

                .map((resource) -> getResourceValueWithReference(resource, reference))

                .filter((value) -> value != null)

                .collect(toSet());
    }

    /**
     * @param entry         : contents entry to be analyzed
     * @param uidFieldRank  : rank of UID field in structure
     * @return raw value of entry reference
     */
    public static String getContentEntryReference(DbDataDto.Entry entry, int uidFieldRank) {
//        System.out.println(new Date().getTime() + " - getContentEntryReference(" + entry + "," + uidFieldRank + ")");

        return getContentItemFromEntryAtFieldRank(entry, uidFieldRank).get().getRawValue();
    }

    private static String getResourceValueWithReference(DbResourceDto resource, String reference) {
        return resource.getEntries().stream()

                .filter((resourceEntry) -> resourceEntry.getReference().equals(reference))

                .findAny()

                .map(DbResourceDto.Entry::getValue)

                .orElse(null);
    }

    private static boolean contentEntryHasForReference(DbDataDto.Entry entry, String ref, List<DbStructureDto.Field> structureFields) {
        OptionalInt potentialUidFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);
        return potentialUidFieldRank.isPresent()
                && entry.getItems().stream()

                .filter((item) -> item.getFieldRank() == potentialUidFieldRank.getAsInt()
                        && item.getRawValue().equals(ref))

                .findAny().isPresent();
    }

    List<DbDto> getTopicObjects() {
        return topicObjects;
    }
}
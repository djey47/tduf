package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_REMOTE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing utility methods to request data from database objects.
 */
// TODO clean usages (0.7.0+)
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

        Optional<DbDto> dbDto = topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getTopic() == topic)

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
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithReference(String ref, DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getContentEntryFromTopicWithReference(" + ref + ", " + topic + ")");

        Optional<DbDto> potentialTopicObject = getDatabaseTopic(topic);
        if (!potentialTopicObject.isPresent()) {
            return Optional.empty();
        }
        DbDto topicObject = potentialTopicObject.get();

        return topicObject.getData().getEntries().stream()

                .filter((entry) -> contentEntryHasForReference(entry, ref, topicObject.getStructure().getFields()))

                .findAny();
    }

    /**
     * @param ref       : external identifier of entry
     * @param topic     : topic in TDU Database to search
     * @return identifier of database entry having specified reference as identifier, empty otherwise.
     */
    public OptionalLong getContentEntryIdFromReference(String ref, DbDto.Topic topic) {
//        System.out.println(new Date().getTime() + " - getContentEntryIdFromReference(" + ref + "," + topic + ")");

        Optional<DbDataDto.Entry> potentialEntry = getContentEntryFromTopicWithReference(ref, topic);
        if(potentialEntry.isPresent()) {
            return OptionalLong.of(potentialEntry.get().getId());
        }
        return OptionalLong.empty();
    }

    /**
     * @param topic             : topic in TDU Database to search
     * @param fieldRank         : rank of field to resolve resource
     * @param entryIdentifier   : index of entry in source topic
     * @return item if it exists, empty otherwise.
     */
    public Optional<DbDataDto.Item> getContentItemFromEntryIdentifierAndFieldRank(DbDto.Topic topic, int fieldRank, long entryIdentifier) {
//        System.out.println(new Date().getTime() + " - getContentItemFromInternalIdentifierAndFieldRank(" + fieldRank + "," + entryIdentifier + "," + topic + ")");

        return getContentEntryFromTopicWithInternalIdentifier(entryIdentifier, topic).getItems().stream()

                .filter((contentItem) -> contentItem.getFieldRank() == fieldRank)

                .findAny();
    }

    /**
     * @param sourceTopic   : topic in TDU Database to search
     * @param fieldRank     : rank of field to resolve resource
     * @param entryIndex    : index of entry in source topic
     * @param targetTopic   : topic targeted by current entry
     * @return full entry if it exists, empty otherwise.
     */
    public Optional<DbDataDto.Entry> getRemoteContentEntryWithInternalIdentifier(DbDto.Topic sourceTopic, int fieldRank, long entryIndex, DbDto.Topic targetTopic) {
//        System.out.println(new Date().getTime() + " - getRemoteContentEntryWithInternalIdentifier(" + sourceTopic + "," + fieldRank + "," + entryIndex + "," + targetTopic +")");

        String remoteReference = getRawValueAtEntryIndexAndRank(sourceTopic, fieldRank, entryIndex);
        return getContentEntryFromTopicWithReference(remoteReference, targetTopic);
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

    /**
     * @param sourceTopic   : topic in TDU Database to search
     * @param fieldRank     : rank of field to resolve resource
     * @param entryIndex    : index of entry in source topic
     * @param locale        : language to be used when resolving resource
     * @return full resource entry targeted by specified entry field.
     */
    public Optional<DbResourceDto.Entry> getResourceEntryWithInternalIdentifier(DbDto.Topic sourceTopic, int fieldRank, long entryIndex, DbResourceDto.Locale locale) {
//        System.out.println(new Date().getTime() + " - getResourceEntryWithInternalIdentifier(" + sourceTopic + "," + fieldRank + "," + entryIndex + "," + locale +")");

        List<DbStructureDto.Field> sourceTopicStructureFields = getDatabaseTopic(sourceTopic).get().getStructure().getFields();
        List<DbDataDto.Item> entryItems = getContentEntryFromTopicWithInternalIdentifier(entryIndex, sourceTopic).getItems();

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(entryItems.get(fieldRank - 1), sourceTopicStructureFields);
        if (structureField.isAResourceField()) {
            DbDto.Topic finalTopic = sourceTopic;
            if (RESOURCE_REMOTE == structureField.getFieldType()) {
                finalTopic = getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
            }
            String resourceReference = getRawValueAtEntryIndexAndRank(sourceTopic, fieldRank, entryIndex);
            return getResourceEntryFromTopicAndLocaleWithReference(resourceReference, finalTopic, locale);
        }

        return Optional.empty();
    }

    /**
     * @param reference             : unique identifier of resource
     * @param topicResourceObjects  : list of topic resource objects to search into
     * @return a set of corresponding values
     */
    public static Set<String> getAllResourceValuesForReference(String reference, List<DbResourceDto> topicResourceObjects) {
//        System.out.println(new Date().getTime() + " - getAllResourceValuesForReference(" + reference + "," + topicResourceObjects + ")");
        return topicResourceObjects.stream()

                .map((resource) -> resource.getEntries().stream()

                        .filter((resourceEntry) -> resourceEntry.getReference().equals(reference))

                        .findAny().get().getValue())

                .collect(toSet());
    }

    /**
     * @param structureFields   : list of structure fields for a topic
     * @return rank of uid field in structure if such a field exists, empty otherwise
     */
    public static Optional<Integer> getUidFieldRank(List<DbStructureDto.Field> structureFields) {
//        System.out.println(new Date().getTime() + " - getUidFieldRank(" + structureFields + ")");

        return DatabaseStructureQueryHelper.getIdentifierField(structureFields)

                .map(DbStructureDto.Field::getRank);
    }

    /**
     * @param entry         : contents entry to be analyzed
     * @param uidFieldRank  : rank of UID field in structure
     * @return raw value of entry reference
     */
    public static String getEntryReference(DbDataDto.Entry entry, int uidFieldRank) {
//        System.out.println(new Date().getTime() + " - getEntryReference(" + entry + "," + uidFieldRank + ")");

        return entry.getItems().stream()

                .filter((item) -> item.getFieldRank() == uidFieldRank)

                .findAny().get().getRawValue();
    }

    private String getRawValueAtEntryIndexAndRank(DbDto.Topic topic, int fieldRank, long entryIndex) {
        return getContentEntryFromTopicWithInternalIdentifier(entryIndex, topic).getItems().stream()

                .filter((contentsItem) -> contentsItem.getFieldRank() == fieldRank)

                .findAny().get().getRawValue();
    }

    private static boolean contentEntryHasForReference(DbDataDto.Entry entry, String ref, List<DbStructureDto.Field> structureFields) {
        Optional<Integer> potentialUidFieldRank = getUidFieldRank(structureFields);
        return potentialUidFieldRank.isPresent()

                && entry.getItems().stream()

                .filter((item) -> item.getFieldRank() == potentialUidFieldRank.get()
                        && item.getRawValue().equals(ref))

                .findAny().isPresent();
    }

    List<DbDto> getTopicObjects() {
        return topicObjects;
    }
}
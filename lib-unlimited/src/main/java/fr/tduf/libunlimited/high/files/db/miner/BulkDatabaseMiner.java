package fr.tduf.libunlimited.high.files.db.miner;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.cache.CacheManager;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_REMOTE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * Class providing utility methods to request data from database objects.
 */
public class BulkDatabaseMiner {

    static {
        Log.trace("BulkDatabaseMiner", "*** new perf session ***");
    }

    private static CacheManager.CacheManagerInstance cacheManager = CacheManager.it.self();

    private final List<DbDto> topicObjects;


    /**
     * @param topicObjects : list of per-topic database objects
     * @return a miner instance.
     */
    public static BulkDatabaseMiner load(List<DbDto> topicObjects) {
        requireNonNull(topicObjects, "A list of per-topic database objects is required.");

        clearAllCaches();

        return new BulkDatabaseMiner(topicObjects);
    }

    private BulkDatabaseMiner(List<DbDto> topicObjects) {
        this.topicObjects = topicObjects;
    }

    /**
     * Clears only variant caches.
     */
    public static void clearAllCaches() {
        cacheManager.clearAllStores();
    }

    /**
     * @param topic : topic in TDU Database to search resources from
     * @return a list of per-locale database resource objects.
     */
    public Optional<List<DbResourceDto>> getAllResourcesFromTopic(DbDto.Topic topic) {
        Log.trace("BulkDatabaseMiner", "getAllResourcesFromTopic(" + topic + ")");

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getTopic() == topic)

                .findAny()

                .map(DbDto::getResources);
    }

    /**
     * @param locale : game language to fetch related resources
     * @param topic  : topic in TDU Database to search resources from
     * @return an optional value: either such a resource object if it exists, else empty.
     */
    public Optional<DbResourceDto> getResourceFromTopicAndLocale(DbDto.Topic topic, DbResourceDto.Locale locale) {
        return(cacheManager.getValueFromKey("resourceFromTopicAndLocale", getCacheKey(topic.name(), locale.name()), () -> {
            Log.trace("BulkDatabaseMiner", "getResourceFromTopicAndLocale(" + topic + ", " + locale + ")");

            return getAllResourcesFromTopic(topic)

                    .map((allResourcesFromTopic) -> allResourcesFromTopic.stream()

                            .filter((resourceObject) -> resourceObject.getLocale() == locale)

                            .findAny()

                            .orElse(null));
        }));
    }

    /**
     * @param topic : topic in TDU Database to search
     * @return database object related to this topic.
     */
    public Optional<DbDto> getDatabaseTopic(DbDto.Topic topic) {
        Log.trace("BulkDatabaseMiner", "getDatabaseTopic(" + topic + ")");

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getTopic() == topic)

                .findAny();
    }

    /**
     * @param topicReference : identifier in database structure
     * @return database object having specified reference.
     */
    public DbDto getDatabaseTopicFromReference(String topicReference) {
        Log.trace("BulkDatabaseMiner", "getDatabaseTopicFromReference(" + topicReference + ")");

        if (topicReference == null) {
            return null;
        }

        return topicObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getRef().equals(topicReference))

                .findAny().get();
    }

    /**
     * @param fieldRank : rank of field to look for value
     * @param itemValue : raw value to be matched
     * @param topic     : topic in TDU Database to search
     * @return list of database entries having specified value at an item rank
     */
    public List<DbDataDto.Entry> getAllContentEntriesFromTopicWithItemValueAtFieldRank(int fieldRank, String itemValue, DbDto.Topic topic) {
        String key = getCacheKey(topic.name(), Integer.valueOf(fieldRank).toString(), itemValue);
        return cacheManager.getValueFromKey("allContentEntriesFromTopicWithItemValueAtFieldRank", key, () -> {
            DbDto topicObject = getDatabaseTopic(topic).get();

            return topicObject.getData().getEntries().stream()

                    .filter((entry) -> {
                        Optional<DbDataDto.Item> contentItemFromEntryAtFieldRank = getContentItemFromEntryAtFieldRank(entry, fieldRank);

                        return contentItemFromEntryAtFieldRank.isPresent()
                                && itemValue.equals(contentItemFromEntryAtFieldRank.get().getRawValue());
                    })

                    .collect(toList());
        });
    }

    /**
     * @param entryIdentifier : unique identifier of entry (TDUF specific)
     * @param topic           : topic in TDU Database to search
     * @return database entry having specified identifier.
     */
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithInternalIdentifier(long entryIdentifier, DbDto.Topic topic) {
        String key = getCacheKey(topic.name(), Long.valueOf(entryIdentifier).toString());
        return cacheManager.getValueFromKey("contentEntryFromTopicWithInternalIdentifier", key, () -> {
            Log.trace("BulkDatabaseMiner", "getContentEntryFromTopicWithInternalIdentifier(" + entryIdentifier + ", " + topic + ")");

            return getDatabaseTopic(topic).get().getData().getEntries().stream()

                    .filter((entry) -> entry.getId() == entryIdentifier)

                    .findAny();
        });
    }

    /**
     * @param ref   : external identifier of entry
     * @param topic : topic in TDU Database to search
     * @return database entry having specified reference as identifier.
     */
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithReference(String ref, DbDto.Topic topic) {
        return cacheManager.getValueFromKey("contentEntryFromTopicWithReference", getCacheKey(ref, topic.name()), () -> {
            Log.trace("BulkDatabaseMiner", "getContentEntryFromTopicWithReference(" + ref + ", " + topic + ")");

            return getDatabaseTopic(topic)
                    .map((topicObject) -> topicObject.getData().getEntries().stream()

                            .filter((entry) -> contentEntryHasForReference(entry, ref, topicObject.getStructure().getFields()))

                            .findAny()

                            .orElse(null));
        });
    }

    /**
     * @param criteria      : list of conditions to select content entries
     * @param topic         : topic in TDU Database to search
     * @return all database entries satisfying all conditions.
     */
    public List<DbDataDto.Entry> getContentEntriesMatchingCriteria(List<DbPatchDto.DbChangeDto.DbFieldValueDto> criteria, DbDto.Topic topic) {
        return getContentEntryStreamMatchingCriteria(criteria, topic)

                .collect(toList());
    }

    /**
     * @param criteria      : list of conditions to select content entries
     * @param topic         : topic in TDU Database to search
     * @return a stream of all database entries satisfying all conditions.
     */
    public Stream<DbDataDto.Entry> getContentEntryStreamMatchingCriteria(List<DbPatchDto.DbChangeDto.DbFieldValueDto> criteria, DbDto.Topic topic) {
        return criteria.stream()

                .flatMap((filter) -> getAllContentEntriesFromTopicWithItemValueAtFieldRank(filter.getRank(), filter.getValue(), topic).stream())

                .collect(groupingBy((topicEntry) -> topicEntry, counting()))

                .entrySet().stream()

                .filter((entry) -> entry.getValue() == criteria.size())

                .map(Map.Entry::getKey);
    }

    /**
     * @param sourceTopic : topic in TDU Database to search
     * @param fieldRank   : rank of field to resolve reference
     * @param entryIndex  : index of entry in source topic
     * @param targetTopic : topic targeted by current entry
     * @return full entry if it exists, empty otherwise.
     */
    public Optional<DbDataDto.Entry> getRemoteContentEntryWithInternalIdentifier(DbDto.Topic sourceTopic, int fieldRank, long entryIndex, DbDto.Topic targetTopic) {
        String key = getCacheKey(sourceTopic.name(), targetTopic.name(), Integer.valueOf(fieldRank).toString(), Long.valueOf(entryIndex).toString());
        return cacheManager.getValueFromKey("remoteContentEntryWithInternalIdentifier", key, () -> {
            Log.trace("BulkDatabaseMiner", "getRemoteContentEntryWithInternalIdentifier(" + sourceTopic + ", " + fieldRank + ", " + entryIndex + ", " + targetTopic + ")");

            String remoteReference = getRawValueAtEntryIndexAndRank(sourceTopic, fieldRank, entryIndex);
            return getContentEntryFromTopicWithReference(remoteReference, targetTopic);
        });
    }

    /**
     * @param entryIdentifier : index of entry in source topic
     * @param topic           : topic in TDU Database to search
     * @return identifier of database entry having specified reference as identifier, empty otherwise.
     */
    public Optional<String> getContentEntryReferenceWithInternalIdentifier(long entryIdentifier, DbDto.Topic topic) {
        String key = getCacheKey(topic.name(), Long.valueOf(entryIdentifier).toString());
        return cacheManager.getValueFromKey("contentEntryReferenceWithInternalIdentifier", key, () -> {
            Log.trace("BulkDatabaseMiner", "getContentEntryRefFromEntryIdentifier(" + entryIdentifier + ", " + topic + ")");

            List<DbStructureDto.Field> structureFields = getDatabaseTopic(topic).get().getStructure().getFields();
            OptionalInt potentialRefFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);
            if (potentialRefFieldRank.isPresent()) {
                Optional<DbDataDto.Item> potentialRefItem = getContentItemWithEntryIdentifierAndFieldRank(topic, potentialRefFieldRank.getAsInt(), entryIdentifier);
                if (potentialRefItem.isPresent()) {
                    return Optional.of(potentialRefItem.get().getRawValue());
                }
            }
            return Optional.empty();
        });
    }

    /**
     * @param ref   : external identifier of entry
     * @param topic : topic in TDU Database to search
     * @return identifier of database entry having specified reference as identifier, empty otherwise.
     */
    public OptionalLong getContentEntryInternalIdentifierWithReference(String ref, DbDto.Topic topic) {
        Log.trace("BulkDatabaseMiner", "getContentEntryInternalIdentifierWithReference(" + ref + ", " + topic + ")");

        Optional<DbDataDto.Entry> potentialEntry = getContentEntryFromTopicWithReference(ref, topic);
        if (potentialEntry.isPresent()) {
            return OptionalLong.of(potentialEntry.get().getId());
        }
        return OptionalLong.empty();
    }

    /**
     * @param entry     : entry containing items to be looked at
     * @param fieldRank : rank of field content item belongs to
     * @return item if it exists, empty otherwise.
     */
    public static Optional<DbDataDto.Item> getContentItemFromEntryAtFieldRank(DbDataDto.Entry entry, int fieldRank) {
        Log.trace("BulkDatabaseMiner", "getContentItemFromEntryAtFieldRank(" + entry.getId() + ", " + fieldRank + ")");

        return entry.getItems().stream()

                .filter((contentItem) -> contentItem.getFieldRank() == fieldRank)

                .findAny();
    }

    /**
     * @param topic           : topic in TDU Database to search
     * @param fieldRank       : rank of field to resolve resource
     * @param entryIdentifier : index of entry in source topic
     * @return item if it exists, empty otherwise.
     */
    public Optional<DbDataDto.Item> getContentItemWithEntryIdentifierAndFieldRank(DbDto.Topic topic, int fieldRank, long entryIdentifier) {
        String key = getCacheKey(topic.name(), Integer.valueOf(fieldRank).toString(), Long.valueOf(entryIdentifier).toString());
        return cacheManager.getValueFromKey("contentItemWithEntryIdentifierAndFieldRank", key, () -> {
            Log.trace("BulkDatabaseMiner", "getContentItemWithEntryIdentifierAndFieldRank(" + fieldRank + ", " + entryIdentifier + ", " + topic + ")");

            return getContentEntryFromTopicWithInternalIdentifier(entryIdentifier, topic)
                    .map((entry) -> getContentItemFromEntryAtFieldRank(entry, fieldRank)

                            .orElse(null));
        });
    }

    /**
     * @param reference : unique identifier of resource
     * @param topic     : topic in TDU Database to search
     * @param locale    : game language to fetch related resources
     * @return an optional value: either such a resource entry if it exists, else absent.
     */
    public Optional<DbResourceDto.Entry> getResourceEntryFromTopicAndLocaleWithReference(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {
        String key = getCacheKey(reference, topic.name(), locale.name());
        return cacheManager.getValueFromKey("resourceEntryFromTopicAndLocaleWithReference", key, () -> {
            Log.trace("BulkDatabaseMiner", "getResourceEntryFromTopicAndLocaleWithReference(" + reference + ", " + topic + ", " + locale + ")");

            return getResourceFromTopicAndLocale(topic, locale)

                    .map((resourceFromTopicAndLocale) -> resourceFromTopicAndLocale.getEntries().stream()

                            .filter((entry) -> entry.getReference().equals(reference))

                            .findAny()

                            .orElse(null));
        });
    }

    /**
     * @param sourceTopic : topic in TDU Database to search
     * @param fieldRank   : rank of field to resolve resource
     * @param entryIndex  : index of entry in source topic
     * @param locale      : language to be used when resolving resource
     * @return full resource entry targeted by specified entry field.
     */
    public Optional<DbResourceDto.Entry> getResourceEntryWithContentEntryInternalIdentifier(DbDto.Topic sourceTopic, int fieldRank, long entryIndex, DbResourceDto.Locale locale) {
        String key = getCacheKey(sourceTopic.name(), locale.name(), Integer.valueOf(fieldRank).toString(), Long.valueOf(entryIndex).toString());
        return cacheManager.getValueFromKey("resourceEntryWithContentEntryInternalIdentifier", key, () -> {
            Log.trace("BulkDatabaseMiner", "getResourceEntryWithContentEntryInternalIdentifier(" + sourceTopic + ", " + fieldRank + ", " + entryIndex + ", " + locale + ")");

            List<DbStructureDto.Field> sourceTopicStructureFields = getDatabaseTopic(sourceTopic).get().getStructure().getFields();
            return getContentEntryFromTopicWithInternalIdentifier(entryIndex, sourceTopic)

                    .map((contentEntry) -> {
                        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(contentEntry.getItemAtRank(fieldRank).get(), sourceTopicStructureFields);
                        if (structureField.isAResourceField()) {
                            DbDto.Topic finalTopic = sourceTopic;
                            if (RESOURCE_REMOTE == structureField.getFieldType()) {
                                finalTopic = getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
                            }

                            String resourceReference = getRawValueAtEntryIndexAndRank(sourceTopic, fieldRank, entryIndex);
                            return getResourceEntryFromTopicAndLocaleWithReference(resourceReference, finalTopic, locale)
                                    .orElse(null);
                        }
                        return null;
                    });
        });
    }

    /**
     * @param reference            : unique identifier of resource
     * @param topicResourceObjects : list of topic resource objects to search into
     * @return a set of corresponding values
     */
    public static Set<String> getAllResourceValuesForReference(String reference, List<DbResourceDto> topicResourceObjects) {
        Log.trace("BulkDatabaseMiner", "getAllResourceValuesForReference(" + reference + ", <topicResourceObjects>)");

        return topicResourceObjects.stream()

                .map((resource) -> getResourceValueWithReference(resource, reference))

                .filter((value) -> value != null)

                .collect(toSet());
    }

    /**
     *
     * @param entry         : contents entry to be analyzed
     * @param uidFieldRank  : rank of UID field in structure
     * @return raw value of entry reference
     */
    public static String getContentEntryReference(DbDataDto.Entry entry, int uidFieldRank) {
        Log.trace("BulkDatabaseMiner", "getContentEntryReference(" + entry.getId() + ", " + uidFieldRank + ")");

        return getContentItemFromEntryAtFieldRank(entry, uidFieldRank).get().getRawValue();
    }

    static String getCacheKey(String... items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < items.length ; i++) {
            sb.append(items[i]);
            if (i < items.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private String getRawValueAtEntryIndexAndRank(DbDto.Topic topic, int fieldRank, long entryIndex) {
        DbDataDto.Entry contentEntry = getContentEntryFromTopicWithInternalIdentifier(entryIndex, topic).get();
        return getContentItemFromEntryAtFieldRank(contentEntry, fieldRank).get().getRawValue();
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

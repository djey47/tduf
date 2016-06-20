package fr.tduf.libunlimited.high.files.db.miner;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_REMOTE;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.*;

/**
 * Class providing utility methods to request data from database objects.
 */
public class BulkDatabaseMiner {
    private static final String THIS_CLASS_NAME = BulkDatabaseMiner.class.getSimpleName();

    static {
        Log.trace(THIS_CLASS_NAME, "*** new perf session ***");
    }

    private final UUID id;

    private final List<DbDto> topicObjects;


    private BulkDatabaseMiner(List<DbDto> topicObjects) {
        this.topicObjects = topicObjects;
        id = UUID.randomUUID();
    }

    /**
     * @param topicObjects : list of per-topic database objects
     * @return a miner instance.
     */
    public static BulkDatabaseMiner load(List<DbDto> topicObjects) {
        requireNonNull(topicObjects, "A list of per-topic database objects is required.");

        return new BulkDatabaseMiner(topicObjects);
    }

    /**
     * @param topic : topic in TDU Database to search
     * @return database object related to this topic.
     */
    public Optional<DbDto> getDatabaseTopic(DbDto.Topic topic) {
        Log.trace(THIS_CLASS_NAME, "getDatabaseTopic(" + topic + ")");

        return topicObjects.stream()

                .filter(databaseObject -> databaseObject.getTopic() == topic)

                .findAny();
    }

    /**
     * @param topicReference : identifier in database structure
     * @return database object having specified reference.
     */
    public DbDto getDatabaseTopicFromReference(String topicReference) {
        Log.trace(THIS_CLASS_NAME, "getDatabaseTopicFromReference(" + topicReference + ")");

        if (topicReference == null) {
            return null;
        }

        return topicObjects.stream()

                .filter(databaseObject -> databaseObject.getStructure().getRef().equals(topicReference))

                .findAny().get();
    }

    /**
     * @param entryIdentifier : unique identifier of entry (TDUF specific)
     * @param topic           : topic in TDU Database to search
     * @return database entry having specified identifier.
     */
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithInternalIdentifier(long entryIdentifier, DbDto.Topic topic) {
        Log.trace(THIS_CLASS_NAME, "getContentEntryFromTopicWithInternalIdentifier(" + entryIdentifier + ", " + topic + ")");

        return getDatabaseTopic(topic)
                .orElseThrow(() -> new NoSuchElementException("No database object found for topic: " + topic))
                .getData().getEntryWithInternalIdentifier(entryIdentifier);
    }

    /**
     * @param ref   : external identifier of entry
     * @param topic : topic in TDU Database to search
     * @return database entry having specified reference as identifier.
     */
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithReference(String ref, DbDto.Topic topic) {
        Log.trace(THIS_CLASS_NAME, "getContentEntryFromTopicWithReference(" + ref + ", " + topic + ")");

        return getDatabaseTopic(topic)
                .orElseThrow(() -> new NoSuchElementException("No database object found for topic: " + topic))
                .getData().getEntryWithReference(ref);
    }

    /**
     * @param values    : items values to search for
     * @param topic     : topic in TDU Database to search
     * @return first entry having specified item values.
     */
    // TODO test
    public Optional<DbDataDto.Entry> getContentEntryFromTopicWithItemValues(List<String> values, DbDto.Topic topic) {
        Log.trace(THIS_CLASS_NAME, "getContentEntryFromTopicWithItemValues(" + values + ", " + topic + ")");

        int valuesHashCode = Objects.hashCode(values);

        // TODO cache hashcode by entry
        return getDatabaseTopic(topic)
                .orElseThrow(() -> new NoSuchElementException("No database object found for topic: " + topic))
                .getData().getEntries().stream()
                .parallel()
                .filter(entry -> entry.valuesHash() == valuesHashCode)
                .findFirst();
    }


    /**
     * @param criteria      : list of conditions to select content entries
     * @param topic         : topic in TDU Database to search
     * @return all database entries satisfying all conditions.
     */
    public List<DbDataDto.Entry> getContentEntriesMatchingCriteria(List<DbFieldValueDto> criteria, DbDto.Topic topic) {
        return getContentEntryStreamMatchingCriteria(criteria, topic)

                .collect(toList());
    }

    /**
     * @param condition     : unique condition to select content entries
     * @param topic         : topic in TDU Database to search
     * @return a stream of all database entries satisfying the condition.
     */
    public Stream<DbDataDto.Entry> getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto condition, DbDto.Topic topic) {
        return getContentEntryStreamMatchingCriteria(singletonList(condition), topic);
    }

    /**
     * @param criteria      : list of conditions to select content entries
     * @param topic         : topic in TDU Database to search
     * @return a stream of all database entries satisfying all conditions.
     */
    public Stream<DbDataDto.Entry> getContentEntryStreamMatchingCriteria(List<DbFieldValueDto> criteria, DbDto.Topic topic) {
        return criteria.stream()
                .flatMap(filter -> getAllContentEntriesFromTopicWithItemValueAtFieldRank(filter.getRank(), filter.getValue(), topic).stream())
                .collect(groupingBy(
                        topicEntry -> topicEntry,
                        LinkedHashMap::new,
                        counting())
                )
                .entrySet().stream()
                .filter(entry -> entry.getValue() == criteria.size())
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
        Log.trace(THIS_CLASS_NAME, "getRemoteContentEntryWithInternalIdentifier(" + sourceTopic + ", " + fieldRank + ", " + entryIndex + ", " + targetTopic + ")");

        String remoteReference = getRawValueAtEntryIndexAndRank(sourceTopic, fieldRank, entryIndex);
        return getContentEntryFromTopicWithReference(remoteReference, targetTopic);
    }

    /**
     * @param entryIdentifier : index of entry in source topic
     * @param topic           : topic in TDU Database to search
     * @return identifier of database entry having specified reference as identifier, empty otherwise.
     */
    public Optional<String> getContentEntryReferenceWithInternalIdentifier(long entryIdentifier, DbDto.Topic topic) {
        Log.trace(THIS_CLASS_NAME, "getContentEntryReferenceWithInternalIdentifier(" + entryIdentifier + ", " + topic + ")");

        List<DbStructureDto.Field> structureFields = getDatabaseTopic(topic).get().getStructure().getFields();
        OptionalInt potentialRefFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);
        if (potentialRefFieldRank.isPresent()) {
            Optional<DbDataDto.Item> potentialRefItem = getContentItemWithEntryIdentifierAndFieldRank(topic, potentialRefFieldRank.getAsInt(), entryIdentifier);
            if (potentialRefItem.isPresent()) {
                return Optional.of(potentialRefItem.get().getRawValue());
            }
        }
        return empty();
    }

    /**
     * @param ref   : external identifier of entry
     * @param topic : topic in TDU Database to search
     * @return identifier of database entry having specified reference as identifier, empty otherwise.
     */
    public OptionalLong getContentEntryInternalIdentifierWithReference(String ref, DbDto.Topic topic) {
        Log.trace(THIS_CLASS_NAME, "getContentEntryInternalIdentifierWithReference(" + ref + ", " + topic + ")");

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
        Log.trace(THIS_CLASS_NAME, "getContentItemFromEntryAtFieldRank(" + entry.getId() + ", " + fieldRank + ")");

        return entry.getItems().stream()

                .filter(contentItem -> contentItem.getFieldRank() == fieldRank)

                .findAny();
    }

    /**
     * @param topic           : topic in TDU Database to search
     * @param fieldRank       : rank of field to resolve resource
     * @param entryIdentifier : index of entry in source topic
     * @return item if it exists, empty otherwise.
     */
    public Optional<DbDataDto.Item> getContentItemWithEntryIdentifierAndFieldRank(DbDto.Topic topic, int fieldRank, long entryIdentifier) {
        Log.trace(THIS_CLASS_NAME, "getContentItemWithEntryIdentifierAndFieldRank(" + fieldRank + ", " + entryIdentifier + ", " + topic + ")");

        return getContentEntryFromTopicWithInternalIdentifier(entryIdentifier, topic)
                .map(entry -> getContentItemFromEntryAtFieldRank(entry, fieldRank)

                        .orElse(null));
    }

    /**
     * @param topic  : topic in TDU Database to search resources from
     * @return an optional value: either such a resource object if it exists, else empty.
     */
    public Optional<DbResourceDto> getResourceEnhancedFromTopic(DbDto.Topic topic) {
        return topicObjects.stream()

                .filter(databaseObject -> databaseObject.getTopic() == topic)

                .findAny()

                .map(DbDto::getResource);
    }

    /**
     * @return localized value if it exists, empty otherwise
     */
    public Optional<String> getLocalizedResourceValueFromTopicAndReference(String reference, DbDto.Topic topic, fr.tduf.libunlimited.common.game.domain.Locale locale) {
        return getResourceEntryFromTopicAndReference(topic, reference)

                .flatMap(entry -> entry.getItemForLocale(locale))

                .map(DbResourceDto.Item::getValue);
    }

    /**
     * @param sourceEntryIndex : index of entry in source topic
     * @param sourceFieldRank  : rank of field to resolve resource
     * @param sourceTopic      : topic in TDU Database to search
     * @param locale           : language to be used when resolving resource
     * @return resource value targeted by specified entry field if it exists, empty otherwise
     */
    public Optional<String> getLocalizedResourceValueFromContentEntry(long sourceEntryIndex, int sourceFieldRank, DbDto.Topic sourceTopic, Locale locale) {
        List<DbStructureDto.Field> sourceTopicStructureFields = getDatabaseTopic(sourceTopic).get().getStructure().getFields();
        return getContentEntryFromTopicWithInternalIdentifier(sourceEntryIndex, sourceTopic)

                .flatMap(contentEntry -> {
                    DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(contentEntry.getItemAtRank(sourceFieldRank).get(), sourceTopicStructureFields);
                    if (structureField.isAResourceField()) {
                        DbDto.Topic targetTopic = sourceTopic;
                        if (RESOURCE_REMOTE == structureField.getFieldType()) {
                            targetTopic = getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
                        }

                        String resourceReference = getRawValueAtEntryIndexAndRank(sourceTopic, sourceFieldRank, sourceEntryIndex);
                        return getLocalizedResourceValueFromTopicAndReference(resourceReference, targetTopic, locale);
                    }
                    return empty();
                });
    }

    /**
     * @return entry having given reference for specified topic, empty otherwise
     */
    public Optional<DbResourceDto.Entry> getResourceEntryFromTopicAndReference(DbDto.Topic topic, String reference) {
        return getResourceEnhancedFromTopic(topic)

                .flatMap(resource -> resource.getEntryByReference(reference));
    }

    /**
     * @return a set of corresponding values for exsiting entry
     */
    public static Set<String> getAllResourceValuesForReference(String reference, DbResourceDto resourceObject) {
        return resourceObject.getEntryByReference(reference)

                .map(entry -> entry.getPresentLocales().stream()
                        .map(presentLocale -> entry.getValueForLocale(presentLocale).orElse(null))
                        .filter(value -> value != null)
                        .collect(toSet()))

                .orElse(new HashSet<>());
    }

    /**
     * @param entry         : contents entry to be analyzed
     * @param uidFieldRank  : rank of UID field in structure
     * @return raw value of entry reference
     */
    public static String getContentEntryReference(DbDataDto.Entry entry, int uidFieldRank) {
        Log.trace(THIS_CLASS_NAME, "getContentEntryReference(" + entry.getId() + ", " + uidFieldRank + ")");

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

    private String getStoreName(String methodName) {
        requireNonNull(methodName, "Method name is required.");

        return String.format("%s:%s", id.toString(), methodName);
    }

    private String getRawValueAtEntryIndexAndRank(DbDto.Topic topic, int fieldRank, long entryIndex) {
        DbDataDto.Entry contentEntry = getContentEntryFromTopicWithInternalIdentifier(entryIndex, topic).get();
        return getContentItemFromEntryAtFieldRank(contentEntry, fieldRank).get().getRawValue();
    }

    private List<DbDataDto.Entry> getAllContentEntriesFromTopicWithItemValueAtFieldRank(int fieldRank, String itemValue, DbDto.Topic topic) {
        DbDto topicObject = getDatabaseTopic(topic).get();

        return topicObject.getData().getEntries().stream()

                .filter(entry -> {
                    Optional<DbDataDto.Item> contentItemFromEntryAtFieldRank = getContentItemFromEntryAtFieldRank(entry, fieldRank);

                    return contentItemFromEntryAtFieldRank.isPresent()
                            && itemValue.equals(contentItemFromEntryAtFieldRank.get().getRawValue());
                })

                .collect(toList());
    }

    private static boolean contentEntryHasForReference(DbDataDto.Entry entry, String ref, List<DbStructureDto.Field> structureFields) {
        OptionalInt potentialUidFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);
        return potentialUidFieldRank.isPresent()
                && entry.getItems().stream()

                .filter(item -> item.getFieldRank() == potentialUidFieldRank.getAsInt()
                        && item.getRawValue().equals(ref))

                .findAny().isPresent();
    }

    List<DbDto> getTopicObjects() {
        return topicObjects;
    }

    UUID getId() {
        return id;
    }
}

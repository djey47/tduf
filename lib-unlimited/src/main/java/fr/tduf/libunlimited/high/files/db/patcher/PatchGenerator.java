package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Used to generate patches from an existing database.
 */
public class PatchGenerator extends AbstractDatabaseHolder {

    private static final Set<DbDto.Topic> CAR_PHYSICS_ASSOCIATION_TOPICS = new HashSet<>(asList(CAR_RIMS, CAR_PACKS, CAR_COLORS));

    private DbDto topicObject;

    /**
     * Generates a patch based on current database objects.
     *
     * @param topic : database topic for which the patch should apply
     * @param range : range of reference values for concerned entries.
     * @return a patch object with all necessary instructions.
     */
    public DbPatchDto makePatch(DbDto.Topic topic, ReferenceRange range) {
        requireNonNull(topic, "A database topic is required.");
        requireNonNull(range, "A reference range is required.");

        return DbPatchDto.builder()
                .addChanges(makeChangesObjectsForTopic(topic, range))
                .build();
    }

    @Override
    protected void postPrepare() {}

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForTopic(DbDto.Topic topic, ReferenceRange range) {

        topicObject = checkTopic(topic);

        final Map<DbDto.Topic, Set<String>> requiredResourceReferences = new HashMap<>();
        final Map<DbDto.Topic, Set<Long>> requiredContentsIds = new HashMap<>();

        Set<DbPatchDto.DbChangeDto> changesObjects = new LinkedHashSet<>();
        List<DbStructureDto.Field> structureFields = topicObject.getStructure().getFields();

        changesObjects.addAll(makeChangesObjectsForContents(structureFields, range, requiredResourceReferences, requiredContentsIds));

        changesObjects.addAll(makeChangesObjectsForRequiredResources(requiredResourceReferences));

        // To prevent recursivity issues
        if (!CAR_PHYSICS_ASSOCIATION_TOPICS.contains(topic)) {
            changesObjects.addAll(makeChangesObjectsForRequiredContents(requiredContentsIds));
        }

        return changesObjects;
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForContents(List<DbStructureDto.Field> structureFields, ReferenceRange range, Map<DbDto.Topic, Set<String>> requiredLocalResourceReferences, Map<DbDto.Topic, Set<Long>> requiredContentsReferences) {
        OptionalInt potentialRefFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);

        return topicObject.getData().getEntries().stream()

                .filter((entry) -> isInRange(entry, potentialRefFieldRank, range))

                .map((acceptedEntry) -> createChangeObjectForEntry(topicObject.getTopic(), acceptedEntry, potentialRefFieldRank, structureFields, requiredLocalResourceReferences, requiredContentsReferences))

                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredContents(Map<DbDto.Topic, Set<Long>> requiredContentsReferences) {

        return requiredContentsReferences.entrySet().stream()

                .flatMap((topicEntry) -> {
                    Set<String> refs = topicEntry.getValue().stream()

                            .map((entryId) -> databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryId, topicEntry.getKey()).get().getItems().get(0).getRawValue())

                            .collect(toSet());

                    return makeChangesObjectsForTopic(topicEntry.getKey(), ReferenceRange.fromCollection(refs)).stream();
                })

                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredResources(Map<DbDto.Topic, Set<String>> resourceReferences) {
        return resourceReferences.entrySet().stream()

                .flatMap((resourceEntry) -> makeChangesObjectsForResourcesInTopic(resourceEntry.getKey(), resourceEntry.getValue()))

                .collect(toSet());
    }

    private Stream<DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesInTopic(DbDto.Topic topic, Set<String> resources) {
        List<DbResourceDto> allResourcesFromTopic = databaseMiner.getAllResourcesFromTopic(topic).get();

        Set<String> globalizedResourceRefs = new HashSet<>();
        Set<String> localizedResourceRefs = new HashSet<>();
        identifyGlobalizedAndLocalizedResourceReferences(resources, allResourcesFromTopic, globalizedResourceRefs, localizedResourceRefs);

        Stream<DbPatchDto.DbChangeDto> changesObjectsForGlobalizedResources = makeChangesObjectsForResourcesWithLocale(topic, Optional.<DbResourceDto.Locale>empty(), globalizedResourceRefs);
        Stream<DbPatchDto.DbChangeDto> changesObjectsForLocalizedResources = Stream.of(DbResourceDto.Locale.values())

                .flatMap((locale) -> makeChangesObjectsForResourcesWithLocale(topic, Optional.of(locale), localizedResourceRefs));

        return Stream.concat(changesObjectsForGlobalizedResources, changesObjectsForLocalizedResources);
    }

    private Stream<DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesWithLocale(DbDto.Topic topic, Optional<DbResourceDto.Locale> potentialLocale, Set<String> topicResources) {
        return topicResources.stream()

                .map((resourceRef) -> createChangeObjectForResource(topic, potentialLocale, resourceRef));
    }

    private DbDto checkTopic(DbDto.Topic topic) {
        Optional<DbDto> potentielTopicObject = databaseMiner.getDatabaseTopic(topic);

        if (!potentielTopicObject.isPresent()) {
            throw new IllegalArgumentException("Topic not found in provided database: " + topic);
        }

        return potentielTopicObject.get();
    }

    private DbPatchDto.DbChangeDto createChangeObjectForResource(DbDto.Topic topic, Optional<DbResourceDto.Locale> potentialLocale, String resourceRef) {
        Optional<DbResourceDto.Entry> potentialResourceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceRef, topic, potentialLocale.orElse(DbResourceDto.Locale.FRANCE));
        String resourceValue = DatabaseGenHelper.RESOURCE_VALUE_DEFAULT;
        if (potentialResourceEntry.isPresent()) {
            resourceValue = potentialResourceEntry.get().getValue();
        }

        return DbPatchDto.DbChangeDto.builder()
                .forTopic(topic)
                .forLocale(potentialLocale.orElse(null))
                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES)
                .asReference(resourceRef)
                .withValue(resourceValue)
                .build();
    }

    private DbPatchDto.DbChangeDto createChangeObjectForEntry(DbDto.Topic topic, DbDataDto.Entry entry, OptionalInt potentialRefFieldRank, List<DbStructureDto.Field> structureFields, Map<DbDto.Topic, Set<String>> requiredResourceReferences, Map<DbDto.Topic, Set<Long>> requiredContentsReferences) {
        List<String> entryValues = entry.getItems().stream()

                .map((entryItem) -> {
                    DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(entryItem, structureFields);
                    return fetchItemValue(topic, structureField, entryItem, requiredContentsReferences, requiredResourceReferences);
                })

                .collect(toList());

        String entryReference = null;
        if (potentialRefFieldRank.isPresent()) {
            entryReference = BulkDatabaseMiner.getContentEntryReference(entry, potentialRefFieldRank.getAsInt());
        }

        if (CAR_PHYSICS_DATA == topic) {
            searchForCarPhysicsAssociatedEntries(entryReference, requiredContentsReferences);
        }

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(topic)
                .asReference(entryReference)
                .withEntryValues(entryValues)
                .build();
    }

    private void searchForCarPhysicsAssociatedEntries(String entryReference, Map<DbDto.Topic, Set<Long>> requiredContentsIds) {
        CAR_PHYSICS_ASSOCIATION_TOPICS.stream()

                .forEach((topic) -> databaseMiner.getAllContentEntriesFromTopicWithItemValueAtFieldRank(1, entryReference, topic)

                        .forEach((associationEntry) -> updateRequiredContentsIds(topic, requiredContentsIds, associationEntry.getId()))
                );
    }

    private String fetchItemValue(DbDto.Topic topic, DbStructureDto.Field structureField, DbDataDto.Item entryItem, Map<DbDto.Topic, Set<Long>> requiredContentsReferences, Map<DbDto.Topic, Set<String>> requiredResourceReferences) {
        DbStructureDto.FieldType fieldType = structureField.getFieldType();
        if (RESOURCE_CURRENT_GLOBALIZED == fieldType
                || RESOURCE_CURRENT_LOCALIZED == fieldType) {

            updateRequiredResourceReferences(topic, requiredResourceReferences, entryItem.getRawValue());
        } else if (RESOURCE_REMOTE == fieldType
                || REFERENCE == fieldType) {

            updateRequiredRemoteReferences(requiredResourceReferences, requiredContentsReferences, entryItem.getRawValue(), structureField);
        }

        return entryItem.getRawValue();
    }

    private void updateRequiredRemoteReferences(Map<DbDto.Topic, Set<String>> requiredResourceReferences, Map<DbDto.Topic, Set<Long>> requiredContentsIds, String reference, DbStructureDto.Field structureField) {
        DbDto remoteTopicObject = databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef());
        DbDto.Topic remoteTopic = remoteTopicObject.getTopic();

        DbStructureDto.FieldType fieldType = structureField.getFieldType();

        if (RESOURCE_REMOTE == fieldType) {
            updateRequiredResourceReferences(remoteTopic, requiredResourceReferences, reference);
        } else {
            Long entryId = databaseMiner.getContentEntryInternalIdentifierWithReference(reference, remoteTopic).getAsLong();
            updateRequiredContentsIds(remoteTopic, requiredContentsIds, entryId);
        }
    }

    private static void updateRequiredContentsIds(DbDto.Topic topic, Map<DbDto.Topic, Set<Long>> requiredContentsIds, Long entryId) {
        requiredContentsIds.putIfAbsent(topic, new HashSet<>());
        requiredContentsIds.get(topic).add(entryId);
    }

    private static void updateRequiredResourceReferences(DbDto.Topic topic, Map<DbDto.Topic, Set<String>> requiredResourceReferences, String reference) {
        requiredResourceReferences.putIfAbsent(topic, new HashSet<>());
        requiredResourceReferences.get(topic).add(reference);
    }

    private static boolean isInRange(DbDataDto.Entry entry, OptionalInt potentialRefFieldRank, ReferenceRange range) {

        String entryRef;
        if (potentialRefFieldRank.isPresent()) {
            entryRef = BulkDatabaseMiner.getContentEntryReference(entry, potentialRefFieldRank.getAsInt());
        } else {
            // For topics without REF
            entryRef = entry.getItems().get(0).getRawValue();
        }
        return range.accepts(entryRef);
    }

    private static void identifyGlobalizedAndLocalizedResourceReferences(Set<String> resourceReferences, List<DbResourceDto> topicResourceObjects, Set<String> globalizedResourceRefs, Set<String> localizedResourceRefs) {
        resourceReferences.stream()

                .forEach((resourceReference) -> {

                    Set<String> resourceValuesForCurrentRef = BulkDatabaseMiner.getAllResourceValuesForReference(resourceReference, topicResourceObjects);
                    if (resourceValuesForCurrentRef.size() == 1) {

                        globalizedResourceRefs.add(resourceReference);

                    } else {

                        localizedResourceRefs.add(resourceReference);

                    }
                });
    }

    DbDto getTopicObject() {
        return topicObject;
    }
}
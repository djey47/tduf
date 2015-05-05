package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseHelper;
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
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Used to generate patches from an existing database.
 */
public class PatchGenerator extends AbstractDatabaseHolder {

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

    private List<DbPatchDto.DbChangeDto> makeChangesObjectsForTopic(DbDto.Topic topic, ReferenceRange range) {

        this.topicObject = checkTopic(topic);

        final Map<DbDto.Topic, Set<String>> requiredResourceReferences = new HashMap<>();
        final Map<DbDto.Topic, Set<String>> requiredContentsReferences = new HashMap<>();

        List<DbPatchDto.DbChangeDto> changesObjects = new ArrayList<>();
        List<DbStructureDto.Field> structureFields = this.topicObject.getStructure().getFields();

        Optional<Integer> potentialRefFieldRank = BulkDatabaseMiner.getUidFieldRank(structureFields);

        changesObjects.addAll(makeChangesObjectsForContents(potentialRefFieldRank, structureFields, range, requiredResourceReferences, requiredContentsReferences));

        changesObjects.addAll(makeChangesObjectsForRequiredResources(requiredResourceReferences));

        changesObjects.addAll(makeChangesObjectsForRequiredContents(requiredContentsReferences));

        return changesObjects;
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForContents(Optional<Integer> potentialRefFieldRank, List<DbStructureDto.Field> structureFields, ReferenceRange range, Map<DbDto.Topic, Set<String>> requiredLocalResourceReferences, Map<DbDto.Topic, Set<String>> requiredContentsReferences) {
        DbDto.Topic topic = this.topicObject.getStructure().getTopic();

        return this.topicObject.getData().getEntries().stream()

                .filter((entry) -> isInRange(entry, potentialRefFieldRank, range))

                .map((acceptedEntry) -> createChangeObjectForEntry(topic, acceptedEntry, potentialRefFieldRank, structureFields, requiredLocalResourceReferences, requiredContentsReferences))

                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredContents(Map<DbDto.Topic, Set<String>> requiredContentsReferences) {

        return requiredContentsReferences.entrySet().stream()

                .flatMap((topicEntry) -> makeChangesObjectsForTopic(topicEntry.getKey(), ReferenceRange.fromCollection(topicEntry.getValue())).stream())

                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredResources(Map<DbDto.Topic, Set<String>> resourceReferences) {
        return resourceReferences.entrySet().stream()

                .flatMap((resourceEntry) -> makeChangesObjectsForResourcesInTopic(resourceEntry.getKey(), resourceEntry.getValue()))

                .collect(toSet());
    }

    private Stream<DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesInTopic(DbDto.Topic topic, Set<String> resources) {
        List<DbResourceDto> allResourcesFromTopic = this.databaseMiner.getAllResourcesFromTopic(topic).get();

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
        String resourceValue = DatabaseHelper.RESOURCE_VALUE_DEFAULT;
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

    private DbPatchDto.DbChangeDto createChangeObjectForEntry(DbDto.Topic topic, DbDataDto.Entry entry, Optional<Integer> potentialRefFieldRank, List<DbStructureDto.Field> structureFields, Map<DbDto.Topic, Set<String>> requiredResourceReferences, Map<DbDto.Topic, Set<String>> requiredContentsReferences) {
        List<String> entryValues = entry.getItems().stream()

                .map((entryItem) -> fetchItemValue(topic, structureFields, entryItem, requiredContentsReferences, requiredResourceReferences))

                .collect(toList());

        String entryReference = null;
        if (potentialRefFieldRank.isPresent()) {
            entryReference = BulkDatabaseMiner.getEntryReference(entry, potentialRefFieldRank.get());
        }

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(topic)
                .asReference(entryReference)
                .withEntryValues(entryValues)
                .build();
    }

    private String fetchItemValue(DbDto.Topic topic, List<DbStructureDto.Field> structureFields, DbDataDto.Item entryItem, Map<DbDto.Topic, Set<String>> requiredContentsReferences, Map<DbDto.Topic, Set<String>> requiredResourceReferences) {
        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(entryItem, structureFields);
        DbStructureDto.FieldType fieldType = structureField.getFieldType();
        if (RESOURCE_CURRENT == fieldType
                || RESOURCE_CURRENT_AGAIN == fieldType) {

            updateRequiredReferences(topic, requiredResourceReferences, entryItem.getRawValue());
        } else if (RESOURCE_REMOTE == fieldType
                || REFERENCE == fieldType) {

            updateRequiredRemoteReferences(requiredResourceReferences, requiredContentsReferences, entryItem.getRawValue(), structureField);
        }

        return entryItem.getRawValue();
    }

    private void updateRequiredRemoteReferences(Map<DbDto.Topic, Set<String>> requiredResourceReferences, Map<DbDto.Topic, Set<String>> requiredContentsReferences, String reference, DbStructureDto.Field structureField) {
        DbDto remoteTopicObject = this.databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef());
        DbDto.Topic remoteTopic = remoteTopicObject.getStructure().getTopic();

        DbStructureDto.FieldType fieldType = structureField.getFieldType();
        Map<DbDto.Topic, Set<String>> requiredReferences = RESOURCE_REMOTE == fieldType ?
                requiredResourceReferences : requiredContentsReferences;

        updateRequiredReferences(remoteTopic, requiredReferences, reference);
    }

    private static void updateRequiredReferences(DbDto.Topic topic, Map<DbDto.Topic, Set<String>> requiredResourceReferences, String reference) {
        if (!requiredResourceReferences.containsKey(topic)) {
            requiredResourceReferences.put(topic, new HashSet<>());
        }
        requiredResourceReferences.get(topic).add(reference);
    }

    private static boolean isInRange(DbDataDto.Entry entry, Optional<Integer> potentialRefFieldRank, ReferenceRange range) {

        String entryRef = "whatever";
        if (potentialRefFieldRank.isPresent()) {
            entryRef = BulkDatabaseMiner.getEntryReference(entry, potentialRefFieldRank.get());
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
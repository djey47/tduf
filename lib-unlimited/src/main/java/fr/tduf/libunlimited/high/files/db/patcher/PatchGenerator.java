package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
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

        final Map<DbDto.Topic, List<String>> requiredResourceReferences = new HashMap<>();
        final Map<DbDto.Topic, List<String>> requiredContentsReferences = new HashMap<>();

        List<DbPatchDto.DbChangeDto> changesObjects = new ArrayList<>();
        List<DbStructureDto.Field> structureFields = this.topicObject.getStructure().getFields();
        BulkDatabaseMiner.getUidFieldRank(structureFields)

                .ifPresent((refFieldRank -> changesObjects.addAll(

                        makeChangesObjectsForContents(refFieldRank, structureFields, range, requiredResourceReferences, requiredContentsReferences)

                )));

        changesObjects.addAll(makeChangesObjectsForRequiredResources(requiredResourceReferences));

        changesObjects.addAll(makeChangesObjectsForRequiredContents(requiredContentsReferences));

        return changesObjects;
    }

    private List<DbPatchDto.DbChangeDto> makeChangesObjectsForContents(int refFieldRank, List<DbStructureDto.Field> structureFields, ReferenceRange range, Map<DbDto.Topic, List<String>> requiredLocalResourceReferences, Map<DbDto.Topic, List<String>> requiredContentsReferences) {
        DbDto.Topic topic = this.topicObject.getStructure().getTopic();

        return this.topicObject.getData().getEntries().stream()

            .filter((entry) -> isInRange(entry, refFieldRank, range))

            .map((acceptedEntry) -> createChangeObjectForEntry(topic, acceptedEntry, refFieldRank, structureFields, requiredLocalResourceReferences, requiredContentsReferences))

            .collect(toList());
    }

    private List<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredContents(Map<DbDto.Topic, List<String>> requiredContentsReferences) {

        return requiredContentsReferences.entrySet().stream()

                .flatMap((topicEntry) -> makeChangesObjectsForTopic(topicEntry.getKey(), ReferenceRange.fromList(topicEntry.getValue())).stream())

                .collect(toList());
    }

    // TODO when all locales have same values for a given ref, generate a single instruction for all locales (reduce instruction count)
    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredResources(Map<DbDto.Topic, List<String>> resourceReferences) {
        return resourceReferences.entrySet().stream()

                .flatMap(this::makeChangesObjectsForResourcesInTopic)

                .collect(toSet());
    }

    private Stream<? extends DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesInTopic(Map.Entry<DbDto.Topic, List<String>> topicResources) {
        return Stream.of(DbResourceDto.Locale.values())

                .flatMap((locale) -> makeChangesObjectsForResourcesWithLocale(locale, topicResources));
    }

    private Stream<? extends DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesWithLocale(DbResourceDto.Locale locale, Map.Entry<DbDto.Topic, List<String>> topicResources) {
        return topicResources.getValue().stream()

                .map((resourceRef) -> createChangeObjectForResource(topicResources.getKey(), locale, resourceRef));
    }

    private DbDto checkTopic(DbDto.Topic topic) {
        Optional<DbDto> potentielTopicObject = databaseMiner.getDatabaseTopic(topic);

        if (!potentielTopicObject.isPresent()) {
            throw new IllegalArgumentException("Topic not found in provided database: " + topic);
        }

        return potentielTopicObject.get();
    }

    private DbPatchDto.DbChangeDto createChangeObjectForResource(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceRef) {
        String resourceValue = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceRef, topic, locale).get().getValue();
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(topic)
                .forLocale(locale)
                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES)
                .asReference(resourceRef)
                .withValue(resourceValue)
                .build();
    }

    private DbPatchDto.DbChangeDto createChangeObjectForEntry(DbDto.Topic topic, DbDataDto.Entry entry, int refFieldRank, List<DbStructureDto.Field> structureFields, Map<DbDto.Topic, List<String>> requiredResourceReferences, Map<DbDto.Topic, List<String>> requiredContentsReferences) {
        List<String> entryValues = entry.getItems().stream()

                .map((entryItem) -> fetchItemValue(topic, structureFields, entryItem, requiredContentsReferences, requiredResourceReferences))

                .collect(toList());

        return DbPatchDto.DbChangeDto.builder()
                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE)
                .forTopic(topic)
                .asReference(BulkDatabaseMiner.getEntryReference(entry, refFieldRank))
                .withEntryValues(entryValues)
                .build();
    }

    private String fetchItemValue(DbDto.Topic topic, List<DbStructureDto.Field> structureFields, DbDataDto.Item entryItem, Map<DbDto.Topic, List<String>> requiredContentsReferences, Map<DbDto.Topic, List<String>> requiredResourceReferences) {
        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(entryItem, structureFields);
        DbStructureDto.FieldType fieldType = structureField.getFieldType();
        if (DbStructureDto.FieldType.RESOURCE_CURRENT == fieldType
                || DbStructureDto.FieldType.RESOURCE_CURRENT_AGAIN == fieldType) {

            updateRequiredReferences(topic, requiredResourceReferences, entryItem.getRawValue());
        } else if (DbStructureDto.FieldType.RESOURCE_REMOTE == fieldType
                || DbStructureDto.FieldType.REFERENCE == fieldType) {

            updateRequiredRemoteReferences(requiredResourceReferences, requiredContentsReferences, entryItem.getRawValue(), structureField);
        }

        return entryItem.getRawValue();
    }

    private void updateRequiredRemoteReferences(Map<DbDto.Topic, List<String>> requiredResourceReferences, Map<DbDto.Topic, List<String>> requiredContentsReferences, String reference, DbStructureDto.Field structureField) {
        DbDto remoteTopicObject = this.databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef());
        DbDto.Topic remoteTopic = remoteTopicObject.getStructure().getTopic();

        DbStructureDto.FieldType fieldType = structureField.getFieldType();
        Map<DbDto.Topic, List<String>> requiredReferences = DbStructureDto.FieldType.RESOURCE_REMOTE == fieldType ?
                requiredResourceReferences : requiredContentsReferences;

        updateRequiredReferences(remoteTopic, requiredReferences, reference);
    }

    private static void updateRequiredReferences(DbDto.Topic topic, Map<DbDto.Topic, List<String>> requiredResourceReferences, String reference) {
        if (!requiredResourceReferences.containsKey(topic)) {
            requiredResourceReferences.put(topic, new ArrayList<>());
        }
        requiredResourceReferences.get(topic).add(reference);
    }

    private static boolean isInRange(DbDataDto.Entry entry, int refFieldRank, ReferenceRange range) {
        String entryRef = BulkDatabaseMiner.getEntryReference(entry, refFieldRank);
        return range.accepts(entryRef);
    }

    DbDto getTopicObject() {
        return topicObject;
    }
}
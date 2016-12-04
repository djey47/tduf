package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto.fromCouple;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Used to generate patches for difference between database against reference one.
 */
// TODO apply code rules
public class DiffPatchesGenerator {
    private List<DbDto> databaseObjects;
    private List<DbDto> referenceDatabaseObjects;

    private BulkDatabaseMiner databaseMiner;
    private BulkDatabaseMiner referenceDatabaseMiner;

    /**
     * Unique entry point.
     *
     * @param databaseObjects          : database topics containing changes
     * @param referenceDatabaseObjects : database topics acting as reference
     * @return a generator instance.
     */
    public static DiffPatchesGenerator prepare(List<DbDto> databaseObjects, List<DbDto> referenceDatabaseObjects) throws ReflectiveOperationException {
        DiffPatchesGenerator holderInstance = new DiffPatchesGenerator();

        holderInstance.databaseObjects = requireNonNull(databaseObjects, "Database objects are required.");
        holderInstance.referenceDatabaseObjects = requireNonNull(referenceDatabaseObjects, "Reference database objects are required.");

        holderInstance.databaseMiner = BulkDatabaseMiner.load(databaseObjects);
        holderInstance.referenceDatabaseMiner = BulkDatabaseMiner.load(referenceDatabaseObjects);

        return holderInstance;
    }

    /**
     * @return patch objects containing differences between current database and reference one.
     */
    public Set<DbPatchDto> makePatches() {
        return databaseObjects.parallelStream()
                .map(this::seekForChanges)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    private Optional<DbPatchDto> seekForChanges(DbDto databaseObject) {
        DbDto.Topic currentTopic = databaseObject.getTopic();
        Optional<DbDto> referenceTopicObject = getReferenceDatabaseMiner().getDatabaseTopic(currentTopic);
        if (!referenceTopicObject.isPresent()) {
            return empty();
        }

        return getDatabaseMiner().getDatabaseTopic(currentTopic)
                .map(topicObject -> {
                    Set<DbPatchDto.DbChangeDto> resourceChanges = seekForResourcesChanges(databaseObject.getResource(), currentTopic);
                    Set<DbPatchDto.DbChangeDto> contentsChanges = seekForContentsChanges(databaseObject.getData(), databaseObject.getStructure().getFields(), currentTopic);

                    return createPatchObject(currentTopic, resourceChanges, contentsChanges);
                });
    }

    private Set<DbPatchDto.DbChangeDto> seekForResourcesChanges(DbResourceDto resourceObject, DbDto.Topic currentTopic) {
        return resourceObject.getEntries().stream()
                .flatMap(resourceEntry -> createResourceUpdates(currentTopic, resourceEntry))
                .filter(changeObject -> changeObject != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Ignore warning
    private Set<DbPatchDto.DbChangeDto> seekForContentsChanges(DbDataDto dataObjects, List<DbStructureDto.Field> structureFields, DbDto.Topic currentTopic) {
        final OptionalInt potentialRefFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);

        return dataObjects.getEntries().stream()
                .map(entry -> potentialRefFieldRank.isPresent() ?
                        handleTopicWithREF(currentTopic, entry, potentialRefFieldRank.getAsInt())
                        :
                        handleTopicWithoutREF(currentTopic, entry))
                .filter(changeObject -> changeObject != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private DbPatchDto.DbChangeDto handleTopicWithREF(DbDto.Topic currentTopic, ContentEntryDto entry, int refFieldRank) {
        String entryRef = BulkDatabaseMiner.getContentEntryReference(entry, refFieldRank);
        final Optional<ContentEntryDto> potentialReferenceEntry = getReferenceDatabaseMiner().getContentEntryFromTopicWithReference(entryRef, currentTopic);

        if (potentialReferenceEntry.isPresent()) {
            return createPartialContentsUpdate(currentTopic, entryRef, entry, potentialReferenceEntry.get());
        } else {
            return createFullContentsUpdate(currentTopic, entry, entryRef);
        }
    }

    private DbPatchDto.DbChangeDto handleTopicWithoutREF(DbDto.Topic currentTopic, ContentEntryDto entry) {
        List<DbFieldValueDto> criteria = entry.getItems().stream()
                .map(item -> fromCouple(item.getFieldRank(), item.getRawValue()))
                .collect(toList());

        List<ContentEntryDto> existingEntries = getReferenceDatabaseMiner().getContentEntriesMatchingCriteria(criteria, currentTopic);
        if (!existingEntries.isEmpty()) {
            return null;
        }

        return createFullContentsUpdate(currentTopic, entry, null);
    }

    private DbPatchDto.DbChangeDto createPartialContentsUpdate(DbDto.Topic currentTopic, String entryReference, ContentEntryDto entry, ContentEntryDto referenceEntry) {
        List<DbFieldValueDto> partialEntryValues = entry.getItems().stream()
                .map(entryItem -> {
                    int fieldRank = entryItem.getFieldRank();
                    String currentValue = entryItem.getRawValue();
                    String referenceValue = referenceEntry.getItemAtRank(fieldRank)
                            .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No content item at rank: " + fieldRank + " in reference entry: " + entry.getId()))
                            .getRawValue();

                    return currentValue.equals(referenceValue) ?
                            null
                            :
                            fromCouple(fieldRank, currentValue);
                })
                .filter(partialValue -> partialValue != null)
                .collect(toList());

        if (partialEntryValues.isEmpty()) {
            return null;
        }

        return DbPatchDto.DbChangeDto.builder()
                .forTopic(currentTopic)
                .withType(UPDATE)
                .asReference(entryReference)
                .withPartialEntryValues(partialEntryValues)
                .build();
    }

    private DbPatchDto.DbChangeDto createFullContentsUpdate(DbDto.Topic currentTopic, ContentEntryDto entry, String entryRef) {
        List<String> entryValues = entry.getItems().stream()
                .map(ContentItemDto::getRawValue)
                .collect(toList());

        return DbPatchDto.DbChangeDto.builder()
                .forTopic(currentTopic)
                .withType(UPDATE)
                .enableStrictMode(entryRef != null)
                .asReference(entryRef)
                .withEntryValues(entryValues)
                .build();
    }

    private Stream<? extends DbPatchDto.DbChangeDto> createResourceUpdates(DbDto.Topic currentTopic, ResourceEntryDto resourceEntry) {
        Optional<ResourceEntryDto> potentialResourceEntry = getReferenceDatabaseMiner().getResourceEntryFromTopicAndReference(currentTopic, resourceEntry.getReference());
        if (potentialResourceEntry.isPresent()) {
            // Already exists => do nothing
            return null;
        }

        if (resourceEntry.isGlobalized()
                || isLocalizedResourceWithUniqueValue(resourceEntry)) {
            return createGlobalizedResourceUpdate(currentTopic, resourceEntry);
        }

        return createLocalizedResourceUpdates(currentTopic, resourceEntry);
    }

    private Stream<? extends DbPatchDto.DbChangeDto> createLocalizedResourceUpdates(DbDto.Topic currentTopic, ResourceEntryDto resourceEntry) {
        return Locale.valuesAsStream()
                .map(locale -> DbPatchDto.DbChangeDto.builder()
                        .withType(UPDATE_RES)
                        .enableStrictMode(true)
                        .asReference(resourceEntry.getReference())
                        .forLocale(locale)
                        .withValue(resourceEntry.getValueForLocale(locale).orElse("??"))
                        .forTopic(currentTopic)
                        .build());
    }

    private Stream<? extends DbPatchDto.DbChangeDto> createGlobalizedResourceUpdate(DbDto.Topic currentTopic, ResourceEntryDto resourceEntry) {
        return Stream.of(DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .enableStrictMode(true)
                .asReference(resourceEntry.getReference())
                .withValue(resourceEntry.pickValue()
                        .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No resource value in entry for REF: " + resourceEntry.getReference() + " in topic: " + currentTopic)))
                .forTopic(currentTopic)
                .forLocale(Locale.DEFAULT)
                .build());
    }

    private DbPatchDto createPatchObject(DbDto.Topic currentTopic, Set<DbPatchDto.DbChangeDto> resourceChanges, Set<DbPatchDto.DbChangeDto> contentsChanges) {
        if (resourceChanges.isEmpty() && contentsChanges.isEmpty()) {
            return null;
        }

        return DbPatchDto.builder()
                .addChanges(resourceChanges)
                .addChanges(contentsChanges)
                .withComment(currentTopic.name())
                .build();
    }

    private static boolean isLocalizedResourceWithUniqueValue(ResourceEntryDto resourceEntry) {
        return 1 == resourceEntry.getPresentLocales().stream()
                .map(resourceEntry::getValueForLocale)
                .map(Optional::get)
                .collect(toSet()).size();
    }

    List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }

    List<DbDto> getReferenceDatabaseObjects() {
        return referenceDatabaseObjects;
    }

    BulkDatabaseMiner getDatabaseMiner() {
        return databaseMiner;
    }

    BulkDatabaseMiner getReferenceDatabaseMiner() {
        return referenceDatabaseMiner;
    }
}

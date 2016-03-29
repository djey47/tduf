package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
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

                .map((topicObject) -> {

                    Set<DbPatchDto.DbChangeDto> resourceChanges = seekForResourcesChanges(databaseObject.getResource(), currentTopic);
                    Set<DbPatchDto.DbChangeDto> contentsChanges = seekForContentsChanges(databaseObject.getData(), databaseObject.getStructure().getFields(), currentTopic);

                    return createPatchObject(currentTopic, resourceChanges, contentsChanges);
                });
    }

    private Set<DbPatchDto.DbChangeDto> seekForResourcesChanges(DbResourceDto resourceObject, DbDto.Topic currentTopic) {
        return resourceObject.getEntries().stream()

                .flatMap((resourceEntry) -> {

                    String ref = resourceEntry.getReference();
                    if (getReferenceDatabaseMiner().getResourceEntryFromTopicAndReference(currentTopic, ref).isPresent()) {
                        // Already exists => do nothing
                        return null;
                    }

                    return isGlobalizedResource(resourceEntry) ?
                            createGlobalizedResourceUpdate(currentTopic, resourceEntry)
                            :
                            createLocalizedResourceUpdates(currentTopic, resourceEntry);
                })

                .filter((changeObject) -> changeObject != null)

                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> seekForContentsChanges(DbDataDto dataObjects, List<DbStructureDto.Field> structureFields, DbDto.Topic currentTopic) {
        final OptionalInt potentialRefFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields);

        return dataObjects.getEntries().stream()

                .map((entry) -> potentialRefFieldRank.isPresent() ?
                        handleTopicWithREF(currentTopic, entry, potentialRefFieldRank.getAsInt())
                        :
                        handleTopicWithoutREF(currentTopic, entry))

                .filter((changeObject) -> changeObject != null)

                .collect(toSet());
    }

    private DbPatchDto.DbChangeDto handleTopicWithREF(DbDto.Topic currentTopic, DbDataDto.Entry entry, int refFieldRank) {
        String entryRef = BulkDatabaseMiner.getContentEntryReference(entry, refFieldRank);
        final Optional<DbDataDto.Entry> potentialReferenceEntry = getReferenceDatabaseMiner().getContentEntryFromTopicWithReference(entryRef, currentTopic);

        if (potentialReferenceEntry.isPresent()) {
            return createPartialContentsUpdate(currentTopic, entryRef, entry, potentialReferenceEntry.get());
        } else {
            return createFullContentsUpdate(currentTopic, entry, entryRef);
        }
    }

    private DbPatchDto.DbChangeDto handleTopicWithoutREF(DbDto.Topic currentTopic, DbDataDto.Entry entry) {
        List<DbFieldValueDto> criteria = entry.getItems().stream()

                .map((item) -> fromCouple(item.getFieldRank(), item.getRawValue()))

                .collect(toList());

        List<DbDataDto.Entry> existingEntries = getReferenceDatabaseMiner().getContentEntriesMatchingCriteria(criteria, currentTopic);
        if (!existingEntries.isEmpty()) {
            return null;
        }

        return createFullContentsUpdate(currentTopic, entry, null);
    }

    private DbPatchDto.DbChangeDto createPartialContentsUpdate(DbDto.Topic currentTopic, String entryReference, DbDataDto.Entry entry, DbDataDto.Entry referenceEntry) {
        List<DbFieldValueDto> partialEntryValues = entry.getItems().stream()

                .map((entryItem) -> {
                    int fieldRank = entryItem.getFieldRank();
                    String currentValue = entryItem.getRawValue();
                    String referenceValue = referenceEntry.getItemAtRank(fieldRank).get().getRawValue();

                    return currentValue.equals(referenceValue) ?
                            null
                            :
                            fromCouple(fieldRank, currentValue);
                })

                .filter((partialValue) -> partialValue != null)

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

    private DbPatchDto.DbChangeDto createFullContentsUpdate(DbDto.Topic currentTopic, DbDataDto.Entry entry, String entryRef) {
        List<String> entryValues = entry.getItems().stream()
                .map(DbDataDto.Item::getRawValue)
                .collect(toList());

        return DbPatchDto.DbChangeDto.builder()
                .forTopic(currentTopic)
                .withType(UPDATE)
                .enableStrictMode(entryRef != null)
                .asReference(entryRef)
                .withEntryValues(entryValues)
                .build();
    }

    private Stream<? extends DbPatchDto.DbChangeDto> createLocalizedResourceUpdates(DbDto.Topic currentTopic, DbResourceDto.Entry resourceEntry) {
        return DbResourceDto.Locale.valuesAsStream()
                .map((locale) -> DbPatchDto.DbChangeDto.builder()
                        .withType(UPDATE_RES)
                        .enableStrictMode(true)
                        .asReference(resourceEntry.getReference())
                        .forLocale(locale)
                        .withValue(resourceEntry.getValueForLocale(locale).orElse("??"))
                        .forTopic(currentTopic)
                        .build());
    }

    private Stream<? extends DbPatchDto.DbChangeDto> createGlobalizedResourceUpdate(DbDto.Topic currentTopic, DbResourceDto.Entry resourceEntry) {
        return Stream.of(DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .enableStrictMode(true)
                .asReference(resourceEntry.getReference())
                .withValue(resourceEntry.pickValue().get())
                .forTopic(currentTopic)
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

    private static boolean isGlobalizedResource(DbResourceDto.Entry resourceEntry) {
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

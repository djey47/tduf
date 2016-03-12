package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static java.util.Objects.requireNonNull;
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
     * @param databaseObjects           : database topics containing changes
     * @param referenceDatabaseObjects  : database topics acting as reference
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

                .map(this::createPatchObject)

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toSet());
    }

    private Optional<DbPatchDto> createPatchObject(DbDto databaseObject) {

        DbDto.Topic currentTopic = databaseObject.getTopic();
        return getDatabaseMiner().getDatabaseTopic(currentTopic)

                .map((topicObject) -> {

                    Optional<DbDto> referenceTopicObject = getReferenceDatabaseMiner().getDatabaseTopic(currentTopic);
                    if (!referenceTopicObject.isPresent()) {
                        return null;
                    }

                    Set<DbPatchDto.DbChangeDto> changes = databaseObject.getData().getEntries().stream()

                            .map((entry) -> {
                                final OptionalInt refFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(databaseObject.getStructure().getFields());
                                if (!refFieldRank.isPresent()) {
                                    // Topic without REF: full update if entry does not exist


                                    List<DbFieldValueDto> criteria = entry.getItems().stream()

                                            .map((item) -> DbFieldValueDto.fromCouple(item.getFieldRank(), item.getRawValue()))

                                            .collect(toList());

                                    List<DbDataDto.Entry> existingEntries = getReferenceDatabaseMiner().getContentEntriesMatchingCriteria(criteria, currentTopic);
                                    if (!existingEntries.isEmpty()) {
                                        return null;
                                    }

                                    List<String> entryValues = entry.getItems().stream()
                                            .map(DbDataDto.Item::getRawValue)
                                            .collect(toList());

                                    return DbPatchDto.DbChangeDto.builder()
                                            .forTopic(currentTopic)
                                            .withType(UPDATE)
                                            .asReference(null)
                                            .withEntryValues(entryValues)
                                            .build();
                                }

                                String entryRef = BulkDatabaseMiner.getContentEntryReference(entry, refFieldRank.getAsInt());
                                final Optional<DbDataDto.Entry> potentialReferenceEntry = getReferenceDatabaseMiner().getContentEntryFromTopicWithReference(entryRef, currentTopic);

                                if (potentialReferenceEntry.isPresent()) {
                                    // Existing: partial update
                                    DbDataDto.Entry referenceEntry = potentialReferenceEntry.get();

                                    // TODO use stream and collect
                                    List<DbFieldValueDto> partialEntryValues = new ArrayList<>();
                                    for (int i = 0 ; i < entry.getItems().size() ; i++) {
                                        String currentValue = entry.getItems().get(i).getRawValue();
                                        String referenceValue = referenceEntry.getItems().get(i).getRawValue();

                                        if (!currentValue.equals(referenceValue)) {
                                            partialEntryValues.add(DbFieldValueDto.fromCouple(i + 1, currentValue));
                                        }
                                    }

                                    if (partialEntryValues.isEmpty()) {
                                        return null;
                                    }

                                    return DbPatchDto.DbChangeDto.builder()
                                            .forTopic(currentTopic)
                                            .withType(UPDATE)
                                            .asReference(entryRef)
                                            .withPartialEntryValues(partialEntryValues)
                                            .build();
                                } else {
                                    // Full update
                                    List<String> entryValues = entry.getItems().stream()
                                            .map(DbDataDto.Item::getRawValue)
                                            .collect(toList());

                                    return DbPatchDto.DbChangeDto.builder()
                                            .forTopic(currentTopic)
                                            .withType(UPDATE)
                                            .asReference(entryRef)
                                            .withEntryValues(entryValues)
                                            .build();
                                }
                            })

                            .filter((changeObject) -> changeObject != null)

                            .collect(toSet());

                    if (changes.isEmpty()) {
                        return null;
                    }

                    return DbPatchDto.builder()
                            .addChanges(changes)
                            .withComment(currentTopic.name())
                            .build();
                });
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

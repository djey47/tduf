package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
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

        return getDatabaseMiner().getDatabaseTopic(databaseObject.getTopic())

                .map((topicObject) -> {

                    Set<DbPatchDto.DbChangeDto> changes = databaseObject.getData().getEntries().stream()

                            .filter((entry) -> {
                                final OptionalInt refFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(databaseObject.getStructure().getFields());
                                if (!refFieldRank.isPresent()) {
                                    return false;
                                }

                                final Optional<DbDataDto.Entry> potentialReferenceEntry = getReferenceDatabaseMiner().getContentEntryFromTopicWithReference(entry.getItemAtRank(refFieldRank.getAsInt()).get().getRawValue(), databaseObject.getTopic());

                                // TODO differentiate cache per miner
                                BulkDatabaseMiner.clearAllCaches();

                                return !potentialReferenceEntry.isPresent();
                            })

                            .map((newEntry) -> {
                                final int refFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(databaseObject.getStructure().getFields()).getAsInt();
                                String newEntryRef = BulkDatabaseMiner.getContentEntryReference(newEntry, refFieldRank);
                                List<String> entryValues = newEntry.getItems().stream()
                                        .map(DbDataDto.Item::getRawValue)
                                        .collect(toList());

                                return DbPatchDto.DbChangeDto.builder()
                                        .forTopic(databaseObject.getTopic())
                                        .withType(UPDATE)
                                        .asReference(newEntryRef)
                                        .withEntryValues(entryValues)
                                        .build();
                            })

                            .collect(toSet());

                    if (changes.isEmpty()) {
                        return null;
                    }

                    return DbPatchDto.builder()
                            .addChanges(changes)
                            .withComment(databaseObject.getTopic().name())
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

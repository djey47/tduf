package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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
        holderInstance.referenceDatabaseObjects = requireNonNull(databaseObjects, "Reference database objects are required.");

        holderInstance.databaseMiner = BulkDatabaseMiner.load(databaseObjects);
        holderInstance.referenceDatabaseMiner = BulkDatabaseMiner.load(referenceDatabaseObjects);

        return holderInstance;
    }

    /**
     * @return patch objects containing differences between current database and reference one.
     */
    public Set<DbPatchDto> makePatches() {
        Set<DbPatchDto> patchObjects = Collections.synchronizedSet(new HashSet<>());

        // TODO

        return patchObjects;
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

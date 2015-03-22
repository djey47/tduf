package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import static java.util.Objects.requireNonNull;

/**
 * Used to apply patchs to an existing database.
 */
public class DatabasePatcher {

    private final DbDto databaseObject;

    private DatabasePatcher(DbDto databaseObject) {
        this.databaseObject = databaseObject;
    }

    /**
     * Unique entry point.
     * @param databaseObject    : database to be patched.
     * @return a patcher instance.
     */
    public static DatabasePatcher prepare(DbDto databaseObject) {
        return new DatabasePatcher(requireNonNull(databaseObject, "A database object is required."));
    }

    /**
     * Execute provided patch onto current database.
     */
    public void apply(DbPatchDto patchObject) {

    }

    DbDto getDatabaseObject() {
        return databaseObject;
    }
}
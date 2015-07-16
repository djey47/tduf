package fr.tduf.libunlimited.high.files.db.common;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Components allowing processing onto existing database objects.
 */
public abstract class AbstractDatabaseHolder {

    protected List<DbDto> databaseObjects;

    protected BulkDatabaseMiner databaseMiner;

    /**
     * Unique entry point.
     * @param holderClass       : class of holder object to generate (DatabasePatcher...)
     * @param databaseObjects   : database topics to be patched.
     * @return a patcher instance.
     */
    public static <T extends AbstractDatabaseHolder> T prepare(Class<T> holderClass, List<DbDto> databaseObjects) throws ReflectiveOperationException {
        T holderInstance = holderClass.newInstance();

        holderInstance.databaseObjects = requireNonNull(databaseObjects, "Database objects are required.");
        holderInstance.databaseMiner = BulkDatabaseMiner.load(databaseObjects);

        holderInstance.postPrepare();

        return holderInstance;
    }

    /**
     *
     */
    protected abstract void postPrepare();

    public List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }
}

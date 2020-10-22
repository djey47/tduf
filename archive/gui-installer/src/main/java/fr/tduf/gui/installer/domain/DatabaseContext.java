package fr.tduf.gui.installer.domain;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Stores all information about loaded database.
 */
public class DatabaseContext {
    private final List<DbDto> topicObjects;

    private final String jsonDatabaseDirectory;

    private final BulkDatabaseMiner miner;

    private DbPatchDto patchObject;

    private DatabasePatchProperties patchProperties = new DatabasePatchProperties();

    private String backupDatabaseDirectory;

    private final UserSelection userSelection;

    public DatabaseContext(List<DbDto> topicObjects, String jsonDatabaseDirectory) {
        this.topicObjects = requireNonNull(topicObjects, "A list of database objects is required.");
        this.jsonDatabaseDirectory = requireNonNull(jsonDatabaseDirectory, "A directory in which JSON database exists is required.");
        this.miner = BulkDatabaseMiner.load(topicObjects);
        this.userSelection = UserSelection.none();
    }

    public List<DbDto> getTopicObjects() {
        return topicObjects;
    }

    public String getJsonDatabaseDirectory() {
        return jsonDatabaseDirectory;
    }

    public BulkDatabaseMiner getMiner() {
        return miner;
    }

    public DbPatchDto getPatchObject() {
        return patchObject;
    }

    public DatabasePatchProperties getPatchProperties() {
        return patchProperties;
    }

    public void setPatch(DbPatchDto patchObject, DatabasePatchProperties patchProperties) {
        this.patchObject = requireNonNull(patchObject, "A patch object is required.");
        this.patchProperties = requireNonNull(patchProperties, "Patch properties are required");
    }

    public String getBackupDatabaseDirectory() {
        return backupDatabaseDirectory;
    }

    public void setBackupDatabaseDirectory(String backupDatabaseDirectory) {
        this.backupDatabaseDirectory = backupDatabaseDirectory;
    }

    public UserSelection getUserSelection() {
        return userSelection;
    }
}

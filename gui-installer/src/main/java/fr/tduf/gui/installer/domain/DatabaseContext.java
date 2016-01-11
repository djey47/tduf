package fr.tduf.gui.installer.domain;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
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

    public DatabaseContext(List<DbDto> topicObjects, String jsonDatabaseDirectory) {
        this.topicObjects = requireNonNull(topicObjects, "A list of database objects is required.");
        this.jsonDatabaseDirectory = requireNonNull(jsonDatabaseDirectory, "A directory in which JSON database exists is required.");
        this.miner = BulkDatabaseMiner.load(topicObjects);
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
}

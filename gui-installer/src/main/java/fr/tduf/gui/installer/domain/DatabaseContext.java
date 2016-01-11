package fr.tduf.gui.installer.domain;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.List;

/**
 * Stores all information about loaded database.
 */
public class DatabaseContext {
    private final List<DbDto> topicObjects;

    private final String jsonDatabaseDirectory;

    public DatabaseContext(List<DbDto> topicObjects, String jsonDatabaseDirectory) {
        this.topicObjects = topicObjects;
        this.jsonDatabaseDirectory = jsonDatabaseDirectory;
    }

    public List<DbDto> getTopicObjects() {
        return topicObjects;
    }

    public String getJsonDatabaseDirectory() {
        return jsonDatabaseDirectory;
    }
}

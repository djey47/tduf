package fr.tduf.libtesting.common.helper.game;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;

/**
 * Provides common features for testing with database objects
 */
public class DatabaseHelper {
    private static final Class<DatabaseHelper> thisClass = DatabaseHelper.class;
    private static final String THIS_CLASS_NAME = DatabaseHelper.class.getSimpleName();

    private DatabaseHelper() {}

    /**
     * Copies JSON resource files to specified directory
     * @return database objects from current JSON resources
     */
    public static List<DbDto> createDatabaseFromResources(String jsonDatabaseDirectory) throws IOException {
        Files.walk(getJsonDatabasePath(), 1)
                .filter(Files::isRegularFile)
                .filter(path -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                .forEach(path -> {
                    try {
                        Files.copy(path, Paths.get(jsonDatabaseDirectory).resolve(path.getFileName()));
                    } catch (IOException ioe) {
                        Log.error(THIS_CLASS_NAME, "Unable to copy JSON database from resources", ioe);
                    }
                });

        return DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);
    }

    /**
     * Uses JSON resource files to create a read-only database.
     * @return database objects from current JSON resources
     */
    public static List<DbDto> createDatabaseForReadOnly() {
        return DatabaseReadWriteHelper.readFullDatabaseFromJson(getJsonDatabasePath().toString());
    }

    /**
     * Uses JSON resource files to create a read-only database topic.
     * @return database object from current JSON resources
     */
    public static DbDto createDatabaseTopicForReadOnly(DbDto.Topic topic) {
        return DatabaseReadWriteHelper.readFullDatabaseFromJson(getJsonDatabasePath().toString()).stream()
                .filter(databaseObject -> topic == databaseObject.getStructure().getTopic())
                .findAny().get();
    }

    private static Path getJsonDatabasePath() {
        return Paths.get(thisClass.getResource("/db-json").getFile());
    }
}

package fr.tduf.libtesting.common.helper.game;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Provides common features to load database for testing
 */
public class DatabaseHelper {
    private static final Class<DatabaseHelper> thisClass = DatabaseHelper.class;

    private static final String EXTENSION_JSON = "json";

    private DatabaseHelper() {}

    /**
     */
    public static List<DbDto> createDatabaseFromResources(String jsonDatabaseDirectory) throws IOException {
        Files.walk(getJsonDatabasePath(), 1)
                .filter(Files::isRegularFile)
                .filter(path -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                .forEach(path -> {
                    try {
                        Files.copy(path, Paths.get(jsonDatabaseDirectory).resolve(path.getFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);
    }

    /**
     *
     * @return
     */
    public static List<DbDto> createDatabaseForReadOnly() {
        return DatabaseReadWriteHelper.readFullDatabaseFromJson(getJsonDatabasePath().toString());
    }

    private static Path getJsonDatabasePath() {
        return Paths.get(thisClass.getResource("/db-json").getFile());
    }
}

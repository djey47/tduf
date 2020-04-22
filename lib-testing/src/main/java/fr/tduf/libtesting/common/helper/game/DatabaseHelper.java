package fr.tduf.libtesting.common.helper.game;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;

/**
 * Provides common features for testing with database objects
 */
public class DatabaseHelper {
    private static final Class<DatabaseHelper> thisClass = DatabaseHelper.class;
    private static final String THIS_CLASS_NAME = DatabaseHelper.class.getSimpleName();

    private static final String RESOURCE_JSON_DATABASE = "/db-json";
    private static final String PATH_FRAGMENT_JAR = ".jar!";

    private DatabaseHelper() {}

    /**
     * Copies JSON resource files to specified directory
     * @return database objects from current JSON resources
     */
    public static List<DbDto> createDatabaseFromResources(String jsonDatabaseDirectory) throws IOException {
        String effectiveDirectory = jsonDatabaseDirectory;
        if (effectiveDirectory == null) {
            effectiveDirectory = TestingFilesHelper.createTempDirectoryForLibrary();
        }

        final Path jsonDatabasePath = getJsonDatabasePath();
        final String destDir = effectiveDirectory;
        if (jsonDatabasePath.toString().contains(PATH_FRAGMENT_JAR)) {
            extractDatabaseResourceFilesFromJar(destDir);
        } else {
            copyDatabaseResourceFilesFromClasspath(jsonDatabasePath, destDir);
        }

        return DatabaseReadWriteHelper.readFullDatabaseFromJson(effectiveDirectory);
    }

    /**
     * @return database objects from current JSON resources
     */
    public static List<DbDto> createDatabase() {
        try {
            return createDatabaseFromResources(null);
        } catch (IOException ioe) {
            Log.warn(THIS_CLASS_NAME, "Unable to create readonly JSON database from resources", ioe);
            return new ArrayList<>(0);
        }
    }

    /**
     * Uses JSON resource files to create a read-only database topic.
     * @return database object from current JSON resources
     */
    public static DbDto createDatabaseTopicForReadOnly(DbDto.Topic topic) {
        return createDatabase().stream()
                .filter(databaseObject -> topic == databaseObject.getStructure().getTopic())
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Topic not found in database: " + topic));
    }

    private static void copyDatabaseResourceFilesFromClasspath(Path jsonDatabasePath, String destDir) throws IOException {
        Files.walk(jsonDatabasePath, 1)
                .filter(Files::isRegularFile)
                .filter(path -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                .forEach(path -> {
                    try {
                        Files.copy(path, Paths.get(destDir).resolve(path.getFileName()));
                    } catch (IOException ioe) {
                        Log.warn(THIS_CLASS_NAME, "Unable to copy JSON database from resources", ioe);
                    }
                });
    }

    private static void extractDatabaseResourceFilesFromJar(String destDir) {
        DbDto.Topic.valuesAsStream()
                .forEach(topic -> {
                    URL inputDataUrl = thisClass.getResource(RESOURCE_JSON_DATABASE + "/" + topic.getLabel() + ".data.json");
                    URL inputStructureUrl = thisClass.getResource(RESOURCE_JSON_DATABASE + "/" + topic.getLabel() + ".structure.json");
                    URL inputResourcesUrl = thisClass.getResource(RESOURCE_JSON_DATABASE + "/" + topic.getLabel() + ".resources.json");

                    if (inputDataUrl == null || inputStructureUrl == null || inputResourcesUrl == null) {
                        Log.warn(THIS_CLASS_NAME, "Unable to locate JSON database files for topic: " + topic);
                    } else {
                        File destDataFile = new File(destDir, topic.getLabel() + ".data.json");
                        File destStructureFile = new File(destDir, topic.getLabel() + ".structure.json");
                        File destResourcesFile = new File(destDir, topic.getLabel() + ".resources.json");
                        try {
                            FileUtils.copyURLToFile(inputDataUrl, destDataFile);
                            FileUtils.copyURLToFile(inputStructureUrl, destStructureFile);
                            FileUtils.copyURLToFile(inputResourcesUrl, destResourcesFile);
                        } catch (IOException ioe) {
                            Log.warn(THIS_CLASS_NAME, "Unable to copy JSON database from resources", ioe);
                        }
                    }
                });
    }

    private static Path getJsonDatabasePath() {
        return Paths.get(thisClass.getResource(RESOURCE_JSON_DATABASE).getFile());
    }
}

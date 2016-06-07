package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.DatabaseContext;
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
 * Static class providing useful methods for testing.
 */
public class InstallerTestsHelper {

    private static final Class<InstallerTestsHelper> thisClass = InstallerTestsHelper.class;

    public static String createTempDirectory() throws IOException {
        return fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForInstaller();
    }

    public static DatabaseContext createJsonDatabase() throws IOException {
        String jsonDatabaseDirectory = InstallerTestsHelper.createTempDirectory();

        Path jsonDatabasePath = Paths.get(thisClass.getResource("/db-json").getFile());
        Files.walk(jsonDatabasePath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        Files.copy(path, Paths.get(jsonDatabaseDirectory).resolve(path.getFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        List<DbDto> topicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);

        return new DatabaseContext(topicObjects, jsonDatabaseDirectory);
    }
}

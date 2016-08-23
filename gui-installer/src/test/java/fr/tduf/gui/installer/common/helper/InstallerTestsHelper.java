package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.util.List;

/**
 * Static class providing useful methods for testing.
 */
public class InstallerTestsHelper {
    public static String createTempDirectory() throws IOException {
        return fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForInstaller();
    }

    public static DatabaseContext createDatabaseContext() throws IOException {
        String jsonDatabaseDirectory = InstallerTestsHelper.createTempDirectory();

        List<DbDto> topicObjects = DatabaseHelper.createDatabaseFromResources(jsonDatabaseDirectory);

        return new DatabaseContext(topicObjects, jsonDatabaseDirectory);
    }
}

package fr.tduf.cli.common.helper;

import java.io.File;

/**
 * Utility class providing common methods to handle file system operations.
 */
public class FilesHelper {

    /**
     * @param directoryToCreate : path to directory to be created. Intermediate folders will be created when necessary.
     */
    public static void createDirectoryIfNotExists(String directoryToCreate) {
        File outputDirectory = new File(directoryToCreate);

        if (!outputDirectory.exists()) {
            boolean isCreated = outputDirectory.mkdirs();
            assert isCreated;
        }
    }
}
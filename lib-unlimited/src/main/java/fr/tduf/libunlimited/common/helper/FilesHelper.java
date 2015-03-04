package fr.tduf.libunlimited.common.helper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class to provide common file operations.
 */
public class FilesHelper {

    private static Class<FilesHelper> thisClass = FilesHelper.class;

    /**
     * Reads text file at provided resource location.
     * @param resourcePath  : path of resource
     * @return a String with resource file contents.
     */
    public static String readTextFromResourceFile(String resourcePath) throws URISyntaxException, IOException {
        URI fileUri = thisClass.getResource(resourcePath).toURI();
        return new String(Files.readAllBytes(Paths.get(fileUri)), Charset.defaultCharset());
    }

    /**
     * Reads binary file at provided resource location.
     * @param resourcePath  : path of resource
     * @return an array of bytes with resource file contents.
     */
    public static byte[] readBytesFromResourceFile(String resourcePath) throws URISyntaxException, IOException {
        URI fileURI = thisClass.getResource(resourcePath).toURI();
        return Files.readAllBytes(Paths.get(fileURI));
    }

    /**
     * Silently creates directory(ies).
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
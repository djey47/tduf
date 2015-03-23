package fr.tduf.libunlimited.common.helper;

import org.codehaus.jackson.map.ObjectMapper;

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
        URI fileUri = getUriFromResourcePath(resourcePath);
        return new String(Files.readAllBytes(Paths.get(fileUri)), Charset.defaultCharset());
    }

    /**
     * Reads binary file at provided resource location.
     * @param resourcePath  : path of resource
     * @return an array of bytes with resource file contents.
     */
    public static byte[] readBytesFromResourceFile(String resourcePath) throws URISyntaxException, IOException {
        URI fileURI = getUriFromResourcePath(resourcePath);
        return Files.readAllBytes(Paths.get(fileURI));
    }

    /**
     * Reads json file at provided resource location and generate corresponding Java object.
     * @param resourcePath  : path of resource
     * @param objectClass   : type of object to generate
     * @param <T>           : type of object to generate
     * @return contents of read file as generated object instance.
     */
    public static <T> T readObjectFromJsonResourceFile(Class<T> objectClass, String resourcePath) throws URISyntaxException, IOException {
        URI fileURI = getUriFromResourcePath(resourcePath);
        return new ObjectMapper().readValue(new File(fileURI), objectClass);
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

    private static URI getUriFromResourcePath(String resourcePath) throws URISyntaxException {
        return thisClass.getResource(resourcePath).toURI();
    }
}
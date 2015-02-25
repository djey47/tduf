package fr.tduf.libunlimited.common.helper;

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

    /**
     * Reads text file at provided resource location.
     * @param resourcePath  : path of resource
     * @return a String with resource file contents.
     */
    public static String readTextFromResourceFile(String resourcePath) throws URISyntaxException, IOException {
        URI uri = FilesHelper.class.getResource(resourcePath).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), Charset.defaultCharset());
    }
}

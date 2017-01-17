package fr.tduf.libunlimited.common.helper;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to provide common file operations.
 */
public class FilesHelper {

    public static final Charset CHARSET_DEFAULT = Charset.defaultCharset();
    public static final Charset CHARSET_UNICODE_8 = Charset.forName("UTF-8");

    private static final Class<FilesHelper> thisClass = FilesHelper.class;

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

    private FilesHelper() {}

    /**
     * Reads text file at provided resource location. Used charset is the default one.
     * @param resourcePath  : path of resource
     * @return a String with resource file contents.
     */
    public static String readTextFromResourceFile(String resourcePath) throws URISyntaxException, IOException {
        return readTextFromResourceFile(resourcePath, CHARSET_DEFAULT);
    }

    /**
     * Reads text file at provided resource location with a given charset.
     * @param resourcePath  : path of resource
     * @param charset       : charset to use
     * @return a String with resource file contents.
     */
    public static String readTextFromResourceFile(String resourcePath, Charset charset) throws URISyntaxException, IOException {
        requireNonNull(charset, "A valid charset is required");

        byte[] bytes = readBytesFromResourceFile(resourcePath);
        return new String(bytes, charset);
    }

    /**
     * Reads binary file at provided resource location.
     * @param resourcePath  : path of resource
     * @return an array of bytes with resource file contents.
     */
    public static byte[] readBytesFromResourceFile(String resourcePath) throws URISyntaxException, IOException {
        InputStream resourceAsStream = thisClass.getResourceAsStream(resourcePath);
        return IOUtils.toByteArray(resourceAsStream);
    }

    /**
     * Reads json file at provided resource location and generate corresponding Java object.
     * @param resourcePath  : path of resource
     * @param objectClass   : type of object to generate
     * @param <T>           : type of object to generate
     * @return contents of read file as generated object instance.
     */
    public static <T> T readObjectFromJsonResourceFile(Class<T> objectClass, String resourcePath) throws URISyntaxException, IOException {
        InputStream resourceAsStream = thisClass.getResourceAsStream(resourcePath);
        return jsonMapper.readValue(resourceAsStream, objectClass);
    }

    /**
     * @return XML Document at specified location
     */
    public static Document readXMLDocumentFromFile(String fileName) throws IOException {
        try {
            DocumentBuilder docBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(fileName);
        } catch (ParserConfigurationException pce) {
            throw new IllegalStateException("Invalid XML parser configuration!", pce);
        } catch (SAXException se) {
            throw new IOException("Invalid XML document!", se);
        }
    }

    /**
     * Silently creates directory(ies).
     * @param directoryToCreate : path to directory to be created. Intermediate folders will be created when necessary.
     */
    public static void createDirectoryIfNotExists(String directoryToCreate) throws IOException {
        Files.createDirectories(Paths.get(directoryToCreate));
    }

    /**
     * Silently create file.
     * @param fileToCreate  : path to file to be created.
     */
    public static void createFileIfNotExists(String fileToCreate) throws IOException {
        Path pathToCreate = Paths.get(fileToCreate);

        if (Files.exists(pathToCreate)) {
            return;
        }

        Files.createFile(pathToCreate);
    }

    /**
     * Only applies to extracted files (test via ide) - not valid if inside a jar.
     * @param resourcePath  : path of resource
     * @return the absolute file name.
     */
    public static String getFileNameFromResourcePath(String resourcePath) throws URISyntaxException {
        URI uri = getUriFromResourcePath(resourcePath);
        return new File(uri).getAbsolutePath();
    }

    /**
     * @param object    : instance to be written to json format
     * @param fileName  : path of file to be created
     */
    public static void writeJsonObjectToFile(Object object, String fileName) throws IOException {
        Path patchFilePath = Paths.get(fileName);
        Files.createDirectories(patchFilePath.getParent());
        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(patchFilePath, StandardCharsets.UTF_8)) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(bufferedWriter, object);
        }
    }

    /**
     * @return Returns the file extension for the given file name, or the empty string if the file has
     * no extension.  The result does not include the '{@code .}'.
     */
    public static String getExtension(String fullName) {
        requireNonNull(fullName, "Full name is required.");

        String fileName = Paths.get(fullName).getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * @return The file name without its path or extension.
     */
    public static String getNameWithoutExtension(String fullName) {
        requireNonNull(fullName, "Full name is required.");

        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    /* Only applies to extracted files (test via ide) - not valid if inside a jar. */
    private static URI getUriFromResourcePath(String resourcePath) throws URISyntaxException {
        return thisClass.getResource(resourcePath).toURI();
    }
}

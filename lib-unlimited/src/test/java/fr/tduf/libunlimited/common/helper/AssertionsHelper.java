package fr.tduf.libunlimited.common.helper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helper class to provide advanced assertions.
 */
public class AssertionsHelper {

    private static Class<AssertionsHelper> thisClass = AssertionsHelper.class;

    /**
     * Checks if specified file exists or not and return {@link java.io.File} instance if OK.
     * @param fileName  : path and file name
     */
    public static File assertFileExistAndGet(String fileName) {
        File actualContentsFile = new File(fileName);
        assertThat(actualContentsFile.exists()).describedAs("File must exist: " + actualContentsFile.getPath()).isTrue();
        return actualContentsFile;
    }

    /**
     * Checks if specified file contents are the same as the one from the same name in resources.
     * @param fileName          : path and file name
     * @param resourceDirectory : directory in app resources
     * @throws URISyntaxException
     */
    public static void assertFileMatchesReference(String fileName, String resourceDirectory) throws URISyntaxException {
        File actualContentsFile = assertFileExistAndGet(fileName);
        File expectedContentsFile = new File(thisClass.getResource(resourceDirectory + actualContentsFile.getName()).toURI());

        assertThat(actualContentsFile).describedAs("File must match reference one: " + expectedContentsFile.getPath()).hasContentEqualTo(expectedContentsFile);
    }

    /**
     * Checks if specified JSON file contents are the same as the one from the same name in resources.
     * @param fileName          : path and file name
     * @param resourceDirectory : directory in app resources
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void assertJsonFileMatchesReference(String fileName, String resourceDirectory) throws URISyntaxException, IOException {
        File actualContentsFile = assertFileExistAndGet(fileName);
        byte[] actualEncoded = Files.readAllBytes(actualContentsFile.toPath());
        String actualJson = new String(actualEncoded, Charset.forName("UTF-8"));

        File expectedContentsFile = new File(thisClass.getResource(resourceDirectory + actualContentsFile.getName()).toURI());
        byte[] expectedEncoded = Files.readAllBytes(expectedContentsFile.toPath());
        String expectedJson = new String(expectedEncoded, Charset.forName("UTF-8"));

        assertJsonEquals("File must match reference one: " + expectedContentsFile.getPath(), expectedJson, actualJson);
    }

    /**
     * Ensuires that specified file contents are not the same as the one from the same name in resources.
     * @param fileName          : path and file name
     * @param resourceDirectory : directory in app resources
     * @throws URISyntaxException
     */
    public static void assertFileDoesNotMatchReference(String fileName, String resourceDirectory) throws URISyntaxException, IOException {
        File actualContentsFile = assertFileExistAndGet(fileName);
        File unexpectedContentsFile = new File(thisClass.getResource(resourceDirectory + actualContentsFile.getName()).toURI());

        byte[] actualBytes = Files.readAllBytes(actualContentsFile.toPath());
        byte[] unexpectedBytes = Files.readAllBytes(unexpectedContentsFile.toPath());

        assertThat(actualBytes).isNotEqualTo(unexpectedBytes);
    }
}
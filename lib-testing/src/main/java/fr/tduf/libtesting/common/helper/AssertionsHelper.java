package fr.tduf.libtesting.common.helper;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

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
    public static void assertJsonFileMatchesReference(String fileName, String resourceDirectory) throws URISyntaxException, IOException, JSONException {
        File actualContentsFile = assertFileExistAndGet(fileName);
        String actualJson = assertAndReadJsonFileContents(fileName);

        File expectedContentsFile = new File(thisClass.getResource(resourceDirectory + actualContentsFile.getName()).toURI());
        byte[] expectedEncoded = Files.readAllBytes(expectedContentsFile.toPath());
        String expectedJson = new String(expectedEncoded, Charset.forName("UTF-8"));

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    /**
     * Checks if specified JSON files contents are the same.
     * @param fileName1 : path and file name
     * @param fileName2 : path and file name
     */
    public static void assertJsonFilesMatch(String fileName1, String fileName2) throws IOException, JSONException {
        String json1 = assertAndReadJsonFileContents(fileName1);
        String json2 = assertAndReadJsonFileContents(fileName2);

        JSONAssert.assertEquals(json1, json2, JSONCompareMode.NON_EXTENSIBLE);
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


    /**
     *
     * @param outputStream
     * @param expected
     * @throws IOException
     */
    public static void assertOutputStreamContainsJsonExactly(OutputStream outputStream, String expected) throws IOException, JSONException {
        finalizeOutputStream(outputStream);
        assertEquals(expected, outputStream.toString(), JSONCompareMode.STRICT);
    }

    /**
     *
     * @param outputStream
     * @param expected
     * @throws IOException
     */
    public static void assertOutputStreamContainsExactly(OutputStream outputStream, String expected) throws IOException {
        finalizeOutputStream(outputStream);
        assertThat(outputStream.toString()).isEqualTo(expected);
    }

    /**
     *
     * @param outputStream
     * @param expectedItems
     * @throws IOException
     */
    public static void assertOutputStreamContainsSequence(OutputStream outputStream, String... expectedItems) throws IOException {
        finalizeOutputStream(outputStream);
        assertThat(outputStream.toString()).containsSequence(expectedItems);
    }

    private static String assertAndReadJsonFileContents(String fileName1) throws IOException {
        File contentsFile1 = assertFileExistAndGet(fileName1);
        byte[] encoded1 = Files.readAllBytes(contentsFile1.toPath());
        return new String(encoded1, Charset.forName("UTF-8"));
    }

    private static void finalizeOutputStream(OutputStream outputStream) throws IOException {
        outputStream.flush();
        outputStream.close();
    }
}
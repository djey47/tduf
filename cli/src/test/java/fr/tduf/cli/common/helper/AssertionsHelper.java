package fr.tduf.cli.common.helper;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
// TODO Merge with AssertionsHelper from lib-unlimited module
public class AssertionsHelper {

    /**
     *
     * @param outputStream
     * @param expected
     * @throws IOException
     */
    public static void assertOutputStreamContainsJsonExactly(OutputStream outputStream, String expected) throws IOException {
        finalizeOutputStream(outputStream);
        assertJsonEquals(expected, outputStream.toString());
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

    /**
     * Checks if specified JSON files contents are the same.
     * @param fileName1 : path and file name
     * @param fileName2 : path and file name
     */
    public static void assertJsonFilesMatch(String fileName1, String fileName2) throws IOException, JSONException {
        String json1 = assertAndReadJsonFileContents(fileName1);
        String json2 = assertAndReadJsonFileContents(fileName2);

        JSONAssert.assertEquals(json1, json2, false);

//        assertJsonEquals("Files " + fileName1 + " and " + fileName2 + " must match: ", json1, json2);
    }

    /**
     * Checks if specified file exists or not and return {@link java.io.File} instance if OK.
     * @param fileName  : path and file name
     */
    public static File assertFileExistAndGet(String fileName) {
        File actualContentsFile = new File(fileName);
        assertThat(actualContentsFile.exists()).describedAs("File must exist: " + actualContentsFile.getPath()).isTrue();
        return actualContentsFile;
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

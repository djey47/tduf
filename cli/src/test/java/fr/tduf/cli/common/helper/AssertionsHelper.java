package fr.tduf.cli.common.helper;

import java.io.IOException;
import java.io.OutputStream;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
// TODO move to lib-unlimited module
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

    private static void finalizeOutputStream(OutputStream outputStream) throws IOException {
        outputStream.flush();
        outputStream.close();
    }
}

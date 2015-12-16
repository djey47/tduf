package fr.tduf.cli.common.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Utility class to provide useful testing abilities over console
 */
public class ConsoleHelper {

    /**
     * Redirects standard output to an output stream for use of printed data
     */
    public static OutputStream hijackStandardOutput() {
        System.out.println("WARNING! System standard output is redirected to print stream for testing's sake :)");

        OutputStream outContents = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContents));
        return outContents;
    }

    /**
     * Closes output stream and return contents as a String
     * @throws IOException
     */
    public static String finalizeAndGetContents(OutputStream outputStream) throws IOException {
        outputStream.flush();
        outputStream.close();
        return outputStream.toString();
    }
}

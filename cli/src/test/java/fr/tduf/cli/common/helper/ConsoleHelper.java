package fr.tduf.cli.common.helper;

import java.io.*;

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
     * Redirects error output to an output stream for use of printed data
     */
    public static OutputStream hijackErrorOutput() {
        System.out.println("WARNING! System error output is redirected to print stream for testing's sake :)");

        OutputStream errContents = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContents));
        return errContents;
    }

    /**
     * Redirects standard and error outputs to System.out or System.err file descriptors
     */
    public static void restoreOutput() {
        PrintStream standardSystemOutput = new PrintStream(new FileOutputStream(FileDescriptor.out));
        System.setOut(standardSystemOutput);
        PrintStream errorSystemOutput = new PrintStream(new FileOutputStream(FileDescriptor.err));
        System.setErr(errorSystemOutput);

        System.out.println("All cleared! System output is redirected to console again :)");
    }

    /**
     * Closes output stream and return contents as a String
     *
     * @throws IOException
     */
    public static String finalizeAndGetContents(OutputStream outputStream) throws IOException {
        outputStream.flush();
        outputStream.close();
        return outputStream.toString();
    }
}

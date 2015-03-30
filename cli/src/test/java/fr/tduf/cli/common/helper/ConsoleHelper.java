package fr.tduf.cli.common.helper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 */
public class ConsoleHelper {

    /**
     *
     * @return
     */
    public static OutputStream hijackStandardOutput() {
        System.out.println("WARNING! System standard output is redirected to print stream for testing's sake :)");

        OutputStream outContents = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContents));
        return outContents;
    }

}
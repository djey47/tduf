package fr.tduf.cli.tools;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import sun.security.action.GetPropertyAction;

import java.io.*;
import java.security.AccessController;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericToolTest {

    private static final String LINE_SEPARATOR = AccessController.doPrivileged(new GetPropertyAction("line.separator"));

    @Rule
    public final ExpectedSystemExit exitRule = ExpectedSystemExit.none();

    private TestingTool testingTool = new TestingTool();

    @After
    public void tearDown() {
        PrintStream standardSystemOutput = new PrintStream(new FileOutputStream(FileDescriptor.out));
        System.setOut(standardSystemOutput);
    }

    @Test
    public void checkArgumentsAndOptions_whenNoArgs_shouldReturnFalse() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[0])).isFalse();
    }

    @Test
    public void checkArgumentsAndOptions_whenKnownCommand_andSuppliedParameter_shouldReturnTrue() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[]{"test", "-p", "value"})).isTrue();
    }

    @Test
    public void checkArgumentsAndOptions_whenKnownCommand_andNoParameter_shouldReturnFalse() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[]{"test"})).isFalse();
    }

    @Test
    public void checkArgumentsAndOptions_whenInvalidDefaultParameter_shouldReturnFalse() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[]{"test_u"})).isFalse();
    }

    @Test
    public void checkArgumentsAndOptions_whenUnknownCommand_shouldReturnFalse() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[]{"info"})).isFalse();
    }

    @Test
    public void checkArgumentsAndOptions_whenKnownCommandButInvalidParameter_shouldReturnFalse() {
        // GIVEN
        String[] args = new String[] {
                "test", "--lparm"};

        // -WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(args)).isFalse();
    }

    @Test
    public void printUsage_shouldNotThrowException() {
        // GIVEN
        CmdLineException e = new CmdLineException(new CmdLineParser(testingTool), "", null);

        // WHEN-THEN
        testingTool.printUsage(e);
    }

    @Test
    public void doMain_whenKnownCommand_andSuppliedParameter_shouldEndNormally() throws IOException {
        // GIVEN-WHEN-THEN
        testingTool.doMain(new String[]{"test", "-p", "value"});
    }

    @Test
    public void doMain_whenKnownCommand_andExceptionInOperation_shouldEndAbnormally() throws IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testingTool.doMain(new String[]{"test_fail", "-p", "value"});
    }

    @Test(expected = SecurityException.class)
    public void doMain_whenKnownCommand_andNoParameter_shouldEndAbnormally() throws IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testingTool.doMain(new String[]{"test"});
    }

    @Test
    public void doMain_whenOutlineCommand_andStandardOutputMode_shouldWriteToConsole() throws IOException {
        // GIVEN
        OutputStream outContents = hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_outline", "-p", "This is a message"});

        // THEN
        assertOutputStreamContainsExactly(outContents, "This is a message" + LINE_SEPARATOR);
    }

    @Test
    public void doMain_whenFailCommand_andNormalizedOutputMode_shouldWriteProperErrorJsonToConsole() throws IOException {
        // GIVEN
        final OutputStream outContents = hijackStandardOutput();
        exitRule.expectSystemExitWithStatus(1);
        exitRule.checkAssertionAfterwards(() -> assertOutputStreamContainsSequence(outContents, "{", "errorMessage", "Exception", "stackTrace", "}"));

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_fail", "-n", "-p", ""});

        // THEN: asserted by exitRule
    }

    @Test
    public void doMain_whenKnownCommand_andNormalizedOutputMode_andVoidResult_shouldWriteProperEmptyJsonToConsole() throws IOException {
        // GIVEN
        OutputStream outContents = hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test", "-n", "-p", ""});

        // THEN
        assertOutputStreamContainsJsonExactly(outContents, "{}");
    }

    @Test
    public void doMain_whenKnownCommand_andNormalizedOutputMode_andResult_shouldWriteProperJsonToConsole() throws IOException {
        // GIVEN
        OutputStream outContents = hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_result", "-n", "-p", ""});

        // THEN
        assertOutputStreamContainsJsonExactly(outContents, "{\"result\":\"ok\"}");
    }

    private static OutputStream hijackStandardOutput() {
        System.out.println("WARNING! System standard output is redirected to print stream for testing's sake :)");

        OutputStream outContents = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContents));
        return outContents;
    }

    private static void assertOutputStreamContainsJsonExactly(OutputStream outputStream, String expected) throws IOException {
        finalizeOutputStream(outputStream);
        assertJsonEquals(expected, outputStream.toString());
    }

    private static void assertOutputStreamContainsExactly(OutputStream outputStream, String expected) throws IOException {
        finalizeOutputStream(outputStream);
        assertThat(outputStream.toString()).isEqualTo(expected);
    }

    private static void assertOutputStreamContainsSequence(OutputStream outputStream, String... expectedItems) throws IOException {
        finalizeOutputStream(outputStream);
        assertThat(outputStream.toString()).containsSequence(expectedItems);
    }

    private static void finalizeOutputStream(OutputStream outputStream) throws IOException {
        outputStream.flush();
        outputStream.close();
    }
}
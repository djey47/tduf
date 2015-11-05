package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.ConsoleHelper;
import fr.tduf.libtesting.common.helper.AssertionsHelper;
import org.json.JSONException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericToolTest {

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
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_outline", "-p", "This is a message"});

        // THEN
        AssertionsHelper.assertOutputStreamContainsExactly(outContents, "This is a message" + System.lineSeparator()
                + "> All done!" + System.lineSeparator());
    }

    @Test
    public void doMain_whenFailCommand_andNormalizedOutputMode_shouldWriteProperErrorJsonToConsole() throws IOException {
        // GIVEN
        final OutputStream outContents = ConsoleHelper.hijackStandardOutput();
        exitRule.expectSystemExitWithStatus(1);
        exitRule.checkAssertionAfterwards(() -> AssertionsHelper.assertOutputStreamContainsSequence(outContents, "{", "errorMessage", "Exception", "stackTrace", "}"));

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_fail", "-n", "-p", ""});

        // THEN: asserted by exitRule
    }

    @Test
    public void doMain_whenKnownCommand_andNormalizedOutputMode_andVoidResult_shouldWriteProperEmptyJsonToConsole() throws IOException, JSONException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test", "-n", "-p", ""});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outContents, "{}");
    }

    @Test
    public void doMain_whenKnownCommand_andNormalizedOutputMode_andResult_shouldWriteProperJsonToConsole() throws IOException, JSONException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_result", "-n", "-p", ""});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outContents, "{\"result\":\"ok\"}");
    }

    @Test
    public void doMain_whenKnownCommand_andVerboseModeEnabled_shouldWriteDebugLogsToConsole() throws IOException, JSONException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_result", "-v", "-p", ""});

        // THEN
        AssertionsHelper.assertOutputStreamContainsSequence(outContents, "INFO: [TestingTool] ", "DEBUG: [TestingTool] This is for sake of verbosity.", "> All done!");
    }
}

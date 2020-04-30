package fr.tduf.cli.tools;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import fr.tduf.libtesting.common.helper.ConsoleHelper;
import fr.tduf.libtesting.common.helper.AssertionsHelper;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericToolTest {

    private TestingTool testingTool = new TestingTool();

    @AfterAll
    static void tearDown() {
        ConsoleHelper.restoreOutput();
    }

    @Test
    void checkArgumentsAndOptions_whenNoArgs_shouldReturnFalse() {
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
    @ExpectSystemExitWithStatus(1)
    void doMain_whenKnownCommand_andExceptionInOperation_shouldEndAbnormally() throws IOException {
        // GIVEN-WHEN-THEN
        testingTool.doMain(new String[]{"test_fail", "-p", "value"});
    }

    @Test(/*expected = SecurityException.class*/)
    @ExpectSystemExitWithStatus(1)
    void doMain_whenKnownCommand_andNoParameter_shouldEndAbnormally() throws IOException {
        // GIVEN-WHEN-THEN
        testingTool.doMain(new String[]{"test"});
    }

    @Test
    void doMain_whenOutlineCommand_andStandardOutputMode_shouldWriteToConsole() throws IOException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_outline", "-p", "This is a message"});

        // THEN
        AssertionsHelper.assertOutputStreamContainsExactly(outContents, "This is a message" + System.lineSeparator()
                + "> All done!" + System.lineSeparator());
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void doMain_whenFailCommand_andNormalizedOutputMode_shouldWriteProperErrorJsonToConsole() throws IOException {
        // GIVEN
        // TODO [2.0] See to implement assertions
//        final OutputStream errContents = ConsoleHelper.hijackErrorOutput();
//        exitRule.checkAssertionAfterwards(() -> AssertionsHelper.assertOutputStreamContainsSequence(errContents, "{", "errorMessage", "Exception", "stackTrace", "}"));

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_fail", "-n", "-p", ""});

        // THEN: asserted by exitRule
    }

    @Test
    void doMain_whenKnownCommand_andNormalizedOutputMode_andVoidResult_shouldWriteProperEmptyJsonToConsole() throws IOException, JSONException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test", "-n", "-p", ""});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outContents, "{}");
    }

    @Test
    void doMain_whenKnownCommand_andNormalizedOutputMode_andResult_shouldWriteProperJsonToConsole() throws IOException, JSONException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_result", "-n", "-p", ""});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outContents, "{\"result\":\"ok\"}");
    }

    @Test
    void doMain_whenKnownCommand_andVerboseModeEnabled_shouldWriteDebugLogsToConsole() throws IOException {
        // GIVEN
        OutputStream outContents = ConsoleHelper.hijackStandardOutput();

        // WHEN-THEN
        testingTool.doMain(new String[]{"test_result", "-v", "-p", ""});

        // THEN
        AssertionsHelper.assertOutputStreamContainsSubsequence(outContents, "00:00  INFO: [TestingTool]", "00:00 DEBUG: [TestingTool] This is for sake of verbosity.", "> All done!");
    }
}

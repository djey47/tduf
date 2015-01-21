package fr.tduf.cli.tools;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericToolTest {

    private TestingTool testingTool = new TestingTool();

    @Test
    public void checkArgumentsAndOptions_whenNoArgs_shouldReturnFalse() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[0])).isFalse();
    }

    @Test
    public void checkArgumentsAndOptions_whenKnownCommand_shouldReturnTrue() {
        // GIVEN-WHEN-THEN
        assertThat(testingTool.checkArgumentsAndOptions(new String[]{"test"})).isTrue();
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
    public void doMain_whenKnownCommand_shouldEndNormally() throws IOException {
        // GIVEN-WHEN-THEN
        testingTool.doMain(new String[]{"test"});
    }
}
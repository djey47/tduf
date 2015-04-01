package fr.tduf.libunlimited.common.helper;

import fr.tduf.libunlimited.common.domain.ProcessResult;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandLineHelperTest {

    private CommandLineHelper commandLineHelper = new CommandLineHelper();

    @Test(expected = NullPointerException.class)
    public void runCliCommand_whenNullCommand_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        commandLineHelper.runCliCommand(null);

        // THEN: NPE
    }

    @Test(expected = IOException.class)
    public void runCliCommand_whenInvalidCommand_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        commandLineHelper.runCliCommand("aaa");

        // THEN: IOE
    }

    @Test
    public void runCliCommand_whenValidCommand_shouldReturnPositiveProcessResult() throws IOException {
        // GIVEN-WHEN
        ProcessResult actualProcessResult = commandLineHelper.runCliCommand("date");
        actualProcessResult.printOut();
        actualProcessResult.printErr();


        // THEN
        assertThat(actualProcessResult).isNotNull();
        assertThat(actualProcessResult.getReturnCode()).isNotEqualTo(-1);

        assertThat(actualProcessResult.getOut()).isNotEmpty();
        assertThat(actualProcessResult.getErr()).isEmpty();
    }

    @Test
    public void runCliCommand_whenValidCommand_andOneInvalidArgument_shouldReturnNegativeProcessResult() throws IOException {
        // GIVEN-WHEN
        ProcessResult actualProcessResult = commandLineHelper.runCliCommand("sort", "/JUKE");
        actualProcessResult.printOut();
        actualProcessResult.printErr();

        // THEN
        assertThat(actualProcessResult).isNotNull();
        assertThat(actualProcessResult.getReturnCode()).isNotZero();

        assertThat(actualProcessResult.getOut()).isEmpty();
        assertThat(actualProcessResult.getErr()).isNotEmpty();
    }
}
package fr.tduf.libunlimited.common.helper;

import fr.tduf.libunlimited.high.files.banks.interop.domain.ProcessResult;
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
    public void runCliCommand_whenValidCommand_shouldReturnProcessResult() throws IOException {
        // GIVEN-WHEN
        ProcessResult actualProcessResult = commandLineHelper.runCliCommand("date");
        actualProcessResult.printOut();
        actualProcessResult.printErr();


        // THEN
        assertThat(actualProcessResult).isNotNull();
        assertThat(actualProcessResult.getReturnCode()).isNotEqualTo(-1);

        assertThat(actualProcessResult.getOut()).isNotNull();
        assertThat(actualProcessResult.getErr()).isNotNull();
    }

    @Test
    public void runCliCommand_whenValidCommand_andOneInvalidArgument_shouldReturnProcessInstance() throws IOException {
        // GIVEN-WHEN
        ProcessResult actualProcessResult = commandLineHelper.runCliCommand("sort", "/JUKE");
        actualProcessResult.printOut();
        actualProcessResult.printErr();

        // THEN
        assertThat(actualProcessResult).isNotNull();
    }
}
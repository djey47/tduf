package fr.tduf.libunlimited.common.helper;

import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineHelperTest {

    private CommandLineHelper commandLineHelper = new CommandLineHelper();

    @Test
    void runCliCommand_whenNullCommand_shouldThrowException() throws IOException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> commandLineHelper.runCliCommand(null));
    }

    @Test
    void runCliCommand_whenInvalidCommand_shouldThrowException() throws IOException {
        // GIVEN-WHEN-THEN
        assertThrows(IOException.class,
                () -> commandLineHelper.runCliCommand("aaa"));
    }

    @Test
    void runCliCommand_whenValidCommand_shouldReturnPositiveProcessResult() throws IOException, URISyntaxException {
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
    void runCliCommand_whenValidCommand_andOneInvalidArgument_shouldReturnNegativeProcessResult() throws IOException {
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
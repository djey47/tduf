package fr.tduf.libunlimited.common.helper;

import fr.tduf.libunlimited.common.domain.ProcessResult;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Utility class to help with CLI operations.
 */
public class CommandLineHelper {

    /**
     * Executes Command Line Interpreter operations. Blocks current thread until operation ends.
     * @param command   : operation to execute, generally the first parameter to call
     * @param args      : other parameters.
     * @return a process result, giving access to exit code and stdout/stderr contents.
     * @throws IOException
     */
    public ProcessResult runCliCommand(String command, String... args) throws IOException {
        requireNonNull(command, "A CLI command is required.");

        List<String> processCommands = new ArrayList<>();
        processCommands.add(command);
        processCommands.addAll(asList(args));

        ProcessBuilder builder = new ProcessBuilder(processCommands);

        try {
            Process process = builder.start();

            String stdout = IOUtils.toString(process.getInputStream());
            String stderr = IOUtils.toString(process.getErrorStream());
            int returnCode = process.waitFor();

            ProcessResult processResult = new ProcessResult(
                    command,
                    returnCode,
                    stdout,
                    stderr);

            handleErrors(processResult);

            return processResult;
        } catch (InterruptedException ie) {
            throw new IOException("Process was interrupted: " + command, ie);
        }
    }

    private static void handleErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() == 1) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }
}
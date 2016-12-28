package fr.tduf.libunlimited.common.helper;

import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Utility class to help with CLI operations.
 */
public class CommandLineHelper {

    public static final int EXIT_CODE_SUCCESS = 0;

    /**
     * Diagnosis entry point.
     * @param args  : command line arguments. First one is expected to be command name.
     */
    public static void main(String[] args) throws IOException {
        CommandLineHelper helper = new CommandLineHelper();

        String[] realArgs = new String[args.length - 1];
        System.arraycopy(args, 1, realArgs, 0, realArgs.length);

        ProcessResult processResult = helper.runCliCommand(args[0], realArgs);

        System.out.println("-> stdout");
        processResult.printOut();
        System.err.println("-> stderr");
        processResult.printErr();
    }

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

            String stdout = IOUtils.toString(process.getInputStream(), defaultCharset());
            String stderr = IOUtils.toString(process.getErrorStream(), defaultCharset());
            int returnCode = process.waitFor();

            return new ProcessResult(
                    command,
                    returnCode,
                    stdout,
                    stderr);
        } catch (InterruptedException ie) {
            throw new IOException("Process was interrupted: " + command, ie);
        }
    }
}
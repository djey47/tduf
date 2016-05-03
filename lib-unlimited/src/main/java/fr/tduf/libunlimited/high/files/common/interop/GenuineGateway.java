package fr.tduf.libunlimited.high.files.common.interop;

import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.common.helper.CommandLineHelper.EXIT_CODE_SUCCESS;
import static java.util.Arrays.asList;

/**
 * Uses TDUMT-CLI application to provide genuine services.
 */
public abstract class GenuineGateway {
    public static final String EXE_TDUMT_CLI = Paths.get(".", "tools", "tdumt-cli", "tdumt-cli.exe").toString();

    /**
     * Describes all available operations.
     */
    public enum CommandLineOperation {
        CAM_LIST("CAM-L"),
        CAM_CUSTOMIZE("CAM-C"),
        CAM_RESET("CAM-R");

        private final String command;

        CommandLineOperation(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }

    private CommandLineHelper commandLineHelper;

    protected GenuineGateway(CommandLineHelper commandLineHelper) {
        this.commandLineHelper = commandLineHelper;
    }

    /**
     * Also processes errors.
     * @param operation : op to execute
     * @param args      : other arguments
     * @return result of execution as standard output.
     */
    protected String callCommandLineInterface(CommandLineOperation operation, String... args) throws IOException {
        List<String> allArguments = new ArrayList<>(args.length + 1);
        allArguments.add(operation.command);
        allArguments.addAll(asList(args));

        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, allArguments.toArray(new String[allArguments.size()]));
        handleCommandLineErrors(processResult);

        return processResult.getOut();
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }
}

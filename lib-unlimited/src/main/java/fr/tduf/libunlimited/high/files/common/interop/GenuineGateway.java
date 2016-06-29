package fr.tduf.libunlimited.high.files.common.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.common.helper.CommandLineHelper.EXIT_CODE_SUCCESS;
import static java.util.Arrays.asList;

/**
 * Uses TDUMT-CLI application to provide genuine services.
 */
public abstract class GenuineGateway {
    private static final String THIS_CLASS_NAME = GenuineBnkGateway.class.getSimpleName();

    private static final Path EXE_TDUMT_CLI = Paths.get("tools", "tdumt-cli", "tdumt-cli.exe");

    /**
     * Describes all available operations.
     */
    public enum CommandLineOperation {
        BANK_INFO("BANK-I"),
        BANK_UNPACK("BANK-U"),
        BANK_REPACK("BANK-R"),
        BANK_BATCH_UNPACK("BANK-UX"),
        BANK_BATCH_REPLACE("BANK-RX"),
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

        String binaryPath = getExecutableDirectory().resolve(EXE_TDUMT_CLI).toString();

        Log.debug(THIS_CLASS_NAME, "TDUMT-CLI Binary path: " + binaryPath);

        ProcessResult processResult = commandLineHelper.runCliCommand(binaryPath, allArguments.toArray(new String[allArguments.size()]));
        handleCommandLineErrors(processResult);

        return processResult.getOut();
    }

    static Path getExecutableDirectory() throws IOException {
        CodeSource codeSource = GenuineGateway.class.getProtectionDomain().getCodeSource();

        File sourceLocation;
        try {
            sourceLocation = new File(codeSource.getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            throw new IOException("Unable to resolve executable directory", e);
        }

        final Path sourcePath = sourceLocation.toPath();
        Log.debug(THIS_CLASS_NAME, "Source location: " + sourcePath);

        if (sourcePath.endsWith(Paths.get("build", "classes", "main"))) {
            // Run from IDE
            return sourcePath.getParent().getParent().getParent().getParent();
        } else {
            // Run from JAR
            return sourcePath.getParent().getParent().getParent();
        }
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }
}

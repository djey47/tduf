package fr.tduf.libunlimited.high.files.common.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.helper.JsonHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import org.apache.commons.lang3.SystemUtils;

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
 * Uses TDUMT-CLI application to provide genuine services written with .net framework.
 */
public abstract class GenuineGateway {
    private static final String THIS_CLASS_NAME = GenuineGateway.class.getSimpleName();

    private static final String INTERPRETER_BINARY = "mono";
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

    @SuppressWarnings("FieldMayBeFinal")
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
        final String interpreterCommand = getInterpreterCommand();
        final String cliCommand = getRootDirectory().resolve(EXE_TDUMT_CLI).toString();
        final String binaryPath = interpreterCommand.isEmpty() ? cliCommand : interpreterCommand;

        Log.debug(THIS_CLASS_NAME, "TDUMT-CLI Binary or interpreter path: " + binaryPath);

        List<String> allArguments = new ArrayList<>(args.length + 2);
        if (!interpreterCommand.isEmpty()) {
            allArguments.add(cliCommand);
        }
        allArguments.add(operation.command);
        allArguments.addAll(asList(args));

        ProcessResult processResult = commandLineHelper.runCliCommand(binaryPath, allArguments.toArray(new String[0]));
        handleCommandLineErrors(processResult);

        String processOutput = processResult.getOut();
        Log.debug(THIS_CLASS_NAME, String.format("Genuine CLI command: '%s' > %s", processResult.getCommandName(), processOutput));

        return cleanCommandOutput(processOutput);
    }

    /**
     * @return Application root path in dev mode or prod mode, detecting current source path automatically
     * @throws IOException when file system error occurs
     */
    static Path getRootDirectory() throws IOException {
        CodeSource codeSource = GenuineGateway.class.getProtectionDomain().getCodeSource();

        File sourceLocation;
        try {
            sourceLocation = new File(codeSource.getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            throw new IOException("Unable to resolve executable directory", e);
        }

        final Path sourcePath = sourceLocation.toPath();
        Log.debug(THIS_CLASS_NAME, "Source location: " + sourcePath);

        return getRootDirectory(sourcePath);
    }

    /**
     * @return Application root path in dev mode or prod mode, knowing current source path
     */
    static Path getRootDirectory(Path sourcePath) {
        // Run from dev build or JAR?
        final Path devSrcBuildSubPath = Paths.get("lib-unlimited","build", "classes", "java", "main");
        final Path devJarBuildSubPath = Paths.get("lib-unlimited","build", "libs", "lib-unlimited-x.y.z-SNAPSHOT.jar");
        final Path prodBuildSubPath = Paths.get("tools","lib", "tduf.jar");

        Path effectiveSubPath;
        if (sourcePath.endsWith(prodBuildSubPath)) {
            effectiveSubPath = prodBuildSubPath;
        } else {
            effectiveSubPath = FilesHelper.isPathContained(devSrcBuildSubPath, sourcePath) ? devSrcBuildSubPath : devJarBuildSubPath;
        }
        final Path rootPath = sourcePath.getRoot().resolve(sourcePath.subpath(0, sourcePath.getNameCount() - effectiveSubPath.getNameCount()));

        Log.debug(THIS_CLASS_NAME, "Executable location: " + rootPath);
        return rootPath;
    }

    private static String getInterpreterCommand() {
        return SystemUtils.IS_OS_WINDOWS ? "" : INTERPRETER_BINARY;
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            String errorMessage = String.format("Unable to execute genuine CLI command: '%s' > (%d) %s", processResult.getCommandName(), processResult.getReturnCode(), processResult.getErr());
            Log.error(THIS_CLASS_NAME, errorMessage);
            throw new IOException(errorMessage);
        }
    }

    private static String cleanCommandOutput(String processOutput) throws IOException {
        try {
            int jsonStartIndex = processOutput.indexOf("{");
            int jsonEndIndex = processOutput.lastIndexOf("}");
            if (jsonStartIndex == -1 || jsonEndIndex == -1) {
                throw new IOException();
            }

            String cleanedOutput = processOutput.substring(jsonStartIndex, jsonEndIndex + 1);

            if (!JsonHelper.isValid(cleanedOutput)) {
                throw new IOException();
            }

            return cleanedOutput;
        } catch (IOException ioe) {
            String errorMessage = String.format("CLI command output is not valid JSON: %s", processOutput);
            Log.error(THIS_CLASS_NAME, errorMessage);
            throw new IOException(errorMessage, ioe);
        }
    }
}

package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.cli.tools.CameraTool.Command.COPY_ALL_SETS;
import static fr.tduf.cli.tools.CameraTool.Command.COPY_SET;
import static java.util.Arrays.asList;

/**
 * Command line interface for handling TDU vehicle cameras.
 */
public class CameraTool extends GenericTool {

    @Option(name="-i", aliases = "--inputCameraFile", usage = "Cameras.bin file to process, required.", required = true)
    private String inputCameraFile;

    @Option(name="-o", aliases = "--outputCameraFile", usage = "Modified Cameras.bin file to create.")
    private String outputCameraFile;

    @Option(name="-t", aliases = "--targetId", usage = "Base value of new camera identifier (required for copy-set/copy-all-sets operations).")
    private Integer targetIdentifier;

    @Option(name="-s", aliases = "--sourceId", usage = "Identifier of camera set to copy (required for copy-set operation).")
    private Integer sourceIdentifier;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        COPY_SET("copy-set", "Duplicate given camera set to a new identifier. Will erase existing."),
        COPY_ALL_SETS("copy-all-sets", "Duplicates all cameras having identifier between 1 and 10000.");

        final String label;
        final String description;

        Command(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public CommandHelper.CommandEnum[] getValues() {
            return values();
        }
    }

    /**
     * Utility entry point
     */
    public static void main(String[] args) throws IOException {
        new CameraTool().doMain(args);
    }

    @Override
    protected boolean commandDispatch() throws Exception {
        switch (command) {
            case COPY_SET:
                commandResult = copySet(inputCameraFile, outputCameraFile);
                return true;
            case COPY_ALL_SETS:
                commandResult = copyAllSets(inputCameraFile, outputCameraFile);
                return true;
            default:
                commandResult = null;
                return false;
        }
    }

    @Override
    protected void assignCommand(String commandArgument) {
        command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        // Output file: defaulted to input file.extended
        if (outputCameraFile == null) {
            outputCameraFile = inputCameraFile + ".extended";
        }

        // Target identifier: mandatory with copy-set/copy-all-sets
        if (targetIdentifier == null
                && ( command == COPY_ALL_SETS || command == COPY_SET)) {
            throw new CmdLineException(parser, "Error: target identifier is required.", null);
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return COPY_ALL_SETS;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                COPY_SET.label + " -i \"C:\\Users\\Bill\\Desktop\\Cameras.bin\" -s 208 -t 209",
                COPY_ALL_SETS.label + " -i \"C:\\Users\\Bill\\Desktop\\Cameras.bin\" -t 10000 -o \"C:\\Users\\Bill\\Desktop\\NewCameras.bin\"");
    }

    private Map<String, ?> copySet(String sourceCameraFile, String targetCameraFile) throws IOException {
        outLine("> Will use Cameras file: " + sourceCameraFile);

        CamerasParser parser = loadAndParseCameras(sourceCameraFile);

        outLine("> Done reading cameras.");

        CamerasHelper.duplicateCameraSet(sourceIdentifier, targetIdentifier, parser);

        outLine("> Done copying camera sets.");

        writeModifiedCameras(parser, targetCameraFile);

        return makeCommandResultForCopy(targetCameraFile);
    }

    private Map<String, ?> copyAllSets(String sourceCameraFile, String targetCameraFile) throws IOException {
        outLine("> Will use Cameras file: " + sourceCameraFile);

        CamerasParser parser = loadAndParseCameras(sourceCameraFile);

        outLine("> Done reading cameras.");

        CamerasHelper.duplicateAllCameraSets(targetIdentifier, parser);

        outLine("> Done copying camera sets.");

        writeModifiedCameras(parser, targetCameraFile);

        return makeCommandResultForCopy(targetCameraFile);
    }

    private CamerasParser loadAndParseCameras(String cameraFile) throws IOException {
        CamerasParser parser = CamerasParser.load(getCamerasInputStream(cameraFile));
        parser.parse();
        return parser;
    }

    private ByteArrayInputStream getCamerasInputStream(String sourceCameraFile) throws IOException {
        return new ByteArrayInputStream(Files.readAllBytes(Paths.get(sourceCameraFile)));
    }

    private void writeModifiedCameras(CamerasParser parser, String targetCameraFile) throws IOException {
        ByteArrayOutputStream outputStream = CamerasWriter.load(parser.getDataStore()).write();
        Files.write(Paths.get(targetCameraFile), outputStream.toByteArray(), StandardOpenOption.CREATE);
    }

    private Map<String, Object> makeCommandResultForCopy(String fileName) {
        String absolutePath = new File(fileName).getAbsolutePath();

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("cameraFileCreated", absolutePath);

        return resultInfo;
    }
}

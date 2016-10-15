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

import static fr.tduf.cli.tools.CameraTool.Command.COPY_SET;
import static fr.tduf.cli.tools.CameraTool.Command.COPY_SETS;
import static java.util.Collections.singletonList;

/**
 * Command line interface for handling TDU vehicle cameras.
 */
public class CameraTool extends GenericTool {

    @Option(name="-i", aliases = "--inputCameraFile", usage = "Cameras.bin file to process, required.", required = true)
    private String inputCameraFile;

    @Option(name="-o", aliases = "--outputCameraFile", usage = "Modified Cameras.bin file to create.")
    private String outputCameraFile;

    @Option(name="-t", aliases = "--targetId", usage = "Base value of new camera identifier (required for copy-set operation).")
    private Integer targetIdentifier;

    @Option(name="-s", aliases = "--sourceId", usage = "Identifier of camera set to copy (required for copy-set operation).")
    private Integer sourceIdentifier;

    @Option(name="-b", aliases = "--batchFile", usage = "CSV File containing all identifiers of camera sets to copy (required for copy-sets operation).")
    private String batchIdentifiersFile;


    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        COPY_SET("copy-set", "Duplicate given camera set to a new identifier. Will not erase existing."),
        COPY_SETS("copy-sets", "Duplicate given camera sets to new identifiers. Will not erase existing.");

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
            case COPY_SETS:
                commandResult = copySets(inputCameraFile, outputCameraFile);
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

        // Identifiers: mandatory with copy-set
        if ((targetIdentifier == null || sourceIdentifier == null)
                && command == COPY_SET) {
            throw new CmdLineException(parser, "Error: target and source identifiers are required.", null);
        }

        // Batch file: mandatory with copy-sets
        if (batchIdentifiersFile == null
                && command == COPY_SETS) {
            throw new CmdLineException(parser, "Error: batch file is required.", null);
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return COPY_SET;
    }

    @Override
    protected List<String> getExamples() {
        return singletonList(
                COPY_SET.label + " -i \"C:\\Users\\Bill\\Desktop\\Cameras.bin\" -s 208 -t 209");
    }

    private Map<String, ?> copySet(String sourceCameraFile, String targetCameraFile) throws IOException {
        CamerasParser parser = loadAndParseCameras(sourceCameraFile);

        CamerasHelper.duplicateCameraSet(sourceIdentifier, targetIdentifier, parser);

        outLine("> Done copying camera set.");

        writeModifiedCameras(parser, targetCameraFile);

        return makeCommandResultForCopy(targetCameraFile);
    }

    private Map<String, ?> copySets(String sourceCameraFile, String targetCameraFile) throws IOException {
        CamerasParser parser = loadAndParseCameras(sourceCameraFile);

        List<String> instructions = readInstructions(batchIdentifiersFile);
        CamerasHelper.batchDuplicateCameraSets(instructions, parser);

        outLine("> Done copying camera sets.");

        writeModifiedCameras(parser, targetCameraFile);

        return makeCommandResultForCopy(targetCameraFile);
    }

    private List<String> readInstructions(String batchIdentifiersFile) throws IOException {
        outLine("> Will use batch identifiers file: " + batchIdentifiersFile);

        List<String> lines = Files.readAllLines(Paths.get(batchIdentifiersFile));

        outLine("> Done reading identifiers.");

        return lines;
    }

    private CamerasParser loadAndParseCameras(String cameraFile) throws IOException {
        outLine("> Will use Cameras file: " + cameraFile);

        CamerasParser parser = CamerasParser.load(getCamerasInputStream(cameraFile));
        parser.parse();

        outLine("> Done reading cameras.");

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

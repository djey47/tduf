package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.dto.SetConfigurationDto;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.cli.tools.CameraTool.Command.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

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

    @Option(name="-b", aliases = "--batchFile", usage = "CSV File containing all identifiers of camera sets to copy (required for copy-sets and delete-sets operations).")
    private String batchIdentifiersFile;

    @Option(name="-c", aliases = "--configurationFile", usage = "JSON File containing all view properties to modify (required for customize-set operation).")
    private String configurationFile;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        LIST("list", "Returns all camera identifiers in provided file."),
        COPY_SET("copy-set", "Duplicate given camera set to a new identifier. Will not erase existing."),
        COPY_SETS("copy-sets", "Duplicate given camera sets (in a CSV file) to new identifiers. Will not erase existing."),
        DELETE_SETS("delete-sets", "Remove given camera sets (in a CSV file)."),
        VIEW_SET("view-set", "Returns all set properties of a given camera identifier."),
        CUSTOMIZE_SET("customize-set", "Defines set properties of a given camera identifier."),
        USE_VIEWS("use-views", "Make a particular camera set re-use views from other cameras (modifies provided camera file).");

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
            case LIST:
                commandResult = listCameras(inputCameraFile);
                return true;
            case COPY_SET:
                commandResult = copySet(inputCameraFile, outputCameraFile);
                return true;
            case COPY_SETS:
                commandResult = copySets(inputCameraFile, outputCameraFile);
            case DELETE_SETS:
                commandResult = deleteSets(inputCameraFile, outputCameraFile);
                return true;
            case VIEW_SET:
                commandResult = viewCameraSet(inputCameraFile, sourceIdentifier);
                return true;
            case CUSTOMIZE_SET:
                commandResult = customizeCameraSet(inputCameraFile, outputCameraFile, configurationFile);
                return true;
            case USE_VIEWS:
                commandResult = useViews(inputCameraFile, configurationFile);
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
        // Output file: defaulted to input file.extended or .customized
        if (outputCameraFile == null) {
            outputCameraFile = inputCameraFile
                    + (COPY_SET == command || COPY_SETS == command ? ".extended" : ".customized");
        }

        // Source identifier: mandatory with view-set
        if (sourceIdentifier == null
                && VIEW_SET == command) {
            throw new CmdLineException(parser, "Error: source identifier is required.", null);
        }

        // Identifiers: mandatory with copy-set
        if ((targetIdentifier == null || sourceIdentifier == null)
                && COPY_SET == command) {
            throw new CmdLineException(parser, "Error: target and source identifiers are required.", null);
        }

        // Batch file: mandatory with copy-sets
        if (batchIdentifiersFile == null
                && COPY_SETS == command) {
            throw new CmdLineException(parser, "Error: batch file is required.", null);
        }

        // Config file: mandatory with customize-set or use-views
        if (configurationFile == null
                && (CUSTOMIZE_SET == command || USE_VIEWS == command)) {
            throw new CmdLineException(parser, "Error: JSON configuration file is required.", null);
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return COPY_SET;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                LIST.label + " -i \"C:\\Desktop\\Cameras.bin\"",
                COPY_SET.label + " -i \"C:\\Desktop\\Cameras.bin\" -s 208 -t 209",
                COPY_SETS.label + " -i \"C:\\Desktop\\Cameras.bin\" -b \"instructions.csv\"",
                VIEW_SET.label + " -i \"C:\\Desktop\\Cameras.bin\" -s 208",
                CUSTOMIZE_SET.label + " -i \"C:\\Desktop\\Cameras.bin\" -c \"C:\\Desktop\\views-properties.json\"",
                USE_VIEWS.label + " -i \"C:\\Desktop\\Cameras.bin\" -c \"C:\\Desktop\\views-properties.json\"");
    }

    private Map<String, ?> useViews(String sourceCameraFile, String configurationFile) throws IOException {
        SetConfigurationDto configurationObject = readConfiguration(configurationFile);
        CameraSetInfo updatedCameraInfo = CamerasHelper.useViews(configurationObject, sourceCameraFile);

        outLine("> Done using views.");

        return makeCommandResultForViewDetails(updatedCameraInfo);
    }

    private Map<String, ?> customizeCameraSet(String sourceCameraFile, String targetCameraFile, String configurationFile) throws IOException {
        CamerasDatabase camerasDatabase = loadCameras(sourceCameraFile);
        SetConfigurationDto configurationObject = readConfiguration(configurationFile);
        CamerasHelper.updateViews(configurationObject, camerasDatabase);

        outLine("> Done customizing camera set.");

        CamerasHelper.saveCamerasDatabase(camerasDatabase, targetCameraFile);

        return makeCommandResultForViewDetails(camerasDatabase, configurationObject.getSetIdentifier());
    }

    private Map<String, ?> viewCameraSet(String cameraFile, int cameraIdentifier) throws IOException {
        CamerasDatabase camerasDatabase = loadCameras(cameraFile);
        return makeCommandResultForViewDetails(camerasDatabase, cameraIdentifier);
    }

    private Map<String, ?> listCameras(String cameraFile) throws IOException {
        CamerasDatabase camerasDatabase = loadCameras(cameraFile);

        Map<String, Object> resultInfo = new HashMap<>();

        List<Integer> cameraIdentifiers = camerasDatabase.getAllSetIdentifiers().stream()
                .sorted(Integer::compareTo)
                .collect(toList());
        resultInfo.put("cameraCount", cameraIdentifiers.size());
        resultInfo.put("cameraIdentifiers", cameraIdentifiers);

        return resultInfo;
    }

    private Map<String, ?> copySet(String sourceCameraFile, String targetCameraFile) throws IOException {
        CamerasDatabase camerasDatabase = loadCameras(sourceCameraFile);

        CamerasHelper.duplicateCameraSet(sourceIdentifier, targetIdentifier, camerasDatabase);

        outLine("> Done copying camera set.");

        CamerasHelper.saveCamerasDatabase(camerasDatabase, targetCameraFile);

        return makeCommandResultForCopyOrDeletion(targetCameraFile);
    }

    private Map<String, ?> copySets(String sourceCameraFile, String targetCameraFile) throws IOException {
        CamerasDatabase camerasDatabase = loadCameras(sourceCameraFile);

        List<String> instructions = readInstructions(batchIdentifiersFile);
        CamerasHelper.batchDuplicateCameraSets(instructions, camerasDatabase);

        outLine("> Done copying camera sets.");

        CamerasHelper.saveCamerasDatabase(camerasDatabase, targetCameraFile);

        return makeCommandResultForCopyOrDeletion(targetCameraFile);
    }

    private Map<String, ?> deleteSets(String sourceCameraFile, String targetCameraFile) throws IOException {
        CamerasDatabase camerasDatabase = loadCameras(sourceCameraFile);

        List<String> instructions = readInstructions(batchIdentifiersFile);
        CamerasHelper.batchDeleteCameraSets(instructions, camerasDatabase);

        outLine("> Done removing camera sets.");

        CamerasHelper.saveCamerasDatabase(camerasDatabase, targetCameraFile);

        return makeCommandResultForCopyOrDeletion(targetCameraFile);
    }

    private List<String> readInstructions(String batchIdentifiersFile) throws IOException {
        outLine("> Will use batch identifiers file: " + batchIdentifiersFile);

        List<String> lines = Files.readAllLines(Paths.get(batchIdentifiersFile));

        outLine("> Done reading identifiers.");

        return lines;
    }

    private SetConfigurationDto readConfiguration(String configurationFile) throws IOException {
        outLine("> Will use configuration file: " + configurationFile);

        SetConfigurationDto readInfo = jsonMapper.readValue(new File(configurationFile), SetConfigurationDto.class);

        outLine("> Done reading configuration.");

        return readInfo;
    }

    private CamerasDatabase loadCameras(String cameraFile) throws IOException {
        outLine("> Will use Cameras file: " + cameraFile);

        CamerasDatabase camerasDatabase = CamerasHelper.loadAndParseCamerasDatabase(cameraFile);

        outLine("> Done reading cameras.");

        return camerasDatabase;
    }

    private Map<String, Object> makeCommandResultForCopyOrDeletion(String fileName) {
        String absolutePath = new File(fileName).getAbsolutePath();

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("cameraFileCreated", absolutePath);

        return resultInfo;
    }

    private Map<String, ?> makeCommandResultForViewDetails(CameraSetInfo cameraSetInfo) {
        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("cameraSet", cameraSetInfo);

        return resultInfo;
    }

    private Map<String, ?> makeCommandResultForViewDetails(CamerasDatabase camerasDatabase, int setIdentifier) {
        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("cameraIdentifier", setIdentifier);
        resultInfo.put("views", camerasDatabase.getViewsForCameraSet(setIdentifier));

        return resultInfo;
    }
}

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static fr.tduf.cli.tools.CameraTool.Command.COPY_ALL_SETS;
import static java.util.Arrays.asList;

/**
 * Command line interface for handling TDU vehicle cameras.
 */
public class CameraTool extends GenericTool {

    @Option(name="-i", aliases = "--inputCameraFile", usage = "Cameras.bin file to process, required.", required = true)
    private String inputCameraFile;

    @Option(name="-o", aliases = "--outputCameraFile", usage = "Cameras.bin file to create.")
    private String outputCameraFile;

    @Option(name="-b", aliases = "--base", usage = "Base value of new camera identifiers (required for copy-all-sets operation) .")
    private Integer baseIdentifier;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
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
            case COPY_ALL_SETS:
                copyAllSets();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void assignCommand(String commandArgument) {
        this.command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        // Output file: defaulted to input file.extension
        if (outputCameraFile == null) {
            outputCameraFile = inputCameraFile + ".extended";
        }

        // Encryption mode: mandatory with decrypt/encrypt
        if (baseIdentifier == null
                && command == COPY_ALL_SETS) {
            throw new CmdLineException(parser, "Error: base is required.", null);
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return COPY_ALL_SETS;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                COPY_ALL_SETS.label + "-c \"C:\\Users\\Bill\\Desktop\\Cameras.bin\" -b 10000");
    }

    private void copyAllSets() throws IOException {
        outLine("> Will use Cameras file: " + this.inputCameraFile);

        CamerasParser parser = CamerasParser.load(getInputStream());
        parser.parse();

        outLine("> Done reading cameras.");

        new HashSet<>(parser.getCameraViews().keySet())

                .forEach((cameraId) -> CamerasHelper.duplicateCameraSet(cameraId, cameraId + baseIdentifier, parser));

        outLine("> Done copying camera sets.");

        ByteArrayOutputStream outputStream = CamerasWriter.load(parser.getDataStore()).write();
        Files.write(Paths.get(outputCameraFile), outputStream.toByteArray(), StandardOpenOption.CREATE);

        outLine("> All done: " + outputCameraFile);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("cameraFileCreated", this.outputCameraFile);
        commandResult = resultInfo;
    }

    private ByteArrayInputStream getInputStream() throws IOException {
        Path inputFilePath = new File(this.inputCameraFile).toPath();
        return new ByteArrayInputStream(Files.readAllBytes(inputFilePath));
    }
}
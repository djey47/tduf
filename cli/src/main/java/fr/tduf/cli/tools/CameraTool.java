package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

import static fr.tduf.cli.tools.CameraTool.Command.COPY_ALL_SETS;
import static java.util.Arrays.asList;

/**
 * Command line interface for handling TDU vehicle cameras.
 */
public class CameraTool extends GenericTool {

    @Option(name="-c", aliases = "--cameraFile", usage = "Cameras.bin file to process, required.", required = true)
    private String cameraFile;

    @Option(name="-b", aliases = "--base", usage = "Base value of new camera identifiers (required for copy-all-sets operation) .", required = true)
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

    private void copyAllSets() {

    }
}
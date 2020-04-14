package fr.tduf.cli.tools;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.cli.tools.dto.ErrorOutputDto;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parent class of all CLI tools
 */
public abstract class GenericTool {

    @Argument
    private List<String> arguments = new ArrayList<>();

    @Option(name = "-n", aliases = "--normalized", usage = "Not mandatory. Produces output as JSON instead of natural language.")
    private boolean withNormalizedOutput = false;

    @Option(name = "-v", aliases = "--verbose", usage = "Not mandatory. Also displays DEBUG messages.")
    private boolean withVerboseOutput = false;

    Map<String, ?> commandResult = null;

    protected ObjectMapper jsonMapper = new ObjectMapper();
    protected ObjectWriter jsonWriter = jsonMapper.writerWithDefaultPrettyPrinter();

    /**
     * All-instance entry point.
     * @param args  : command line arguments
     */
    protected void doMain(String[] args) throws IOException {
        if (!checkArgumentsAndOptions(args)) {
            System.exit(1);
        }

        Log.set(withVerboseOutput ? Log.LEVEL_DEBUG : Log.LEVEL_NONE);

        try {
            if (!commandDispatch()) {
                errLine("Command is not implemented, yet.");
                System.exit(1);
            }
        } catch (Exception e) {
            errLine(ExceptionUtils.getStackTrace(e));

            processNormalizedErrorOutput(e);
            processNormalizedOutput();

            System.exit(1);
        }

        processNormalizedOutput();

        outLine("> All done!");
    }

    /**
     * Displays text on standard output, with a new line. Takes normalization setting into account.
     * @param message : message to display
     */
    protected void outLine(String message) {
        if (!withNormalizedOutput) {
            System.out.println(message);
        }
    }

    /**
     * Adds a new line on standard output. Takes normalization setting into account.
     */
    protected void outLine() {
        if (!withNormalizedOutput) {
            System.out.println();
        }
    }

    /**
     * Should process according to provided command.
     * @return true if command has been correctly dispatched, false otherwise.
     */
    protected abstract boolean commandDispatch() throws Exception;

    /**
     * Should assign current command.
     * @param commandArgument   : value of provided command argument
     */
    protected abstract void assignCommand(String commandArgument);

    /**
     * Should check parameter validity and assign default ones, eventually.
     * @param parser    : command line parser instance to use
     * @throws CmdLineException when syntax error occurs
     */
    protected abstract void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException;

    /**
     * Should return one command enum instance.
     */
    protected abstract CommandHelper.CommandEnum  getCommand();

    /**
     * Should return some usage examples.
     */
    // Provide default implementation based on enum examples
    protected abstract List<String> getExamples();

    boolean checkArgumentsAndOptions(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            if (arguments.isEmpty()) {
                throw new CmdLineException(parser, "Error: No command is given.", null);
            }

            String commandArgument = arguments.get(0);

            if (!CommandHelper.getLabels(getCommand()).contains(commandArgument)) {
                throw new CmdLineException(parser, "Error: An unsupported command is given.", null);
            }

            assignCommand(commandArgument);

            checkAndAssignDefaultParameters(parser);
        } catch (CmdLineException e) {

            System.err.println(e.getMessage());
            System.err.println();

            printUsage(e);

            return false;
        }

        return true;
    }

    void printUsage(CmdLineException e) {
        String displayedClassName = this.getClass().getSimpleName();

        System.err.println("Usage: " + displayedClassName + " command [-options]");
        System.err.println();

        System.err.println("  .Commands:");
        CommandHelper.getValuesAsMap(getCommand())
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach((entry) -> {
                    System.err.println();
                    System.err.println(" " + entry.getKey() + " : " + entry.getValue());
                });
        System.err.println();
        System.err.println();

        System.err.println("  .Options:");
        System.err.println();
        e.getParser().printUsage(System.err);
        System.err.println();
        System.err.println();

        System.err.println("  .Examples:");
        getExamples().stream()
                .sorted(String::compareTo)
                .forEach((example) -> {
                    System.err.println();
                    System.err.println(" " + displayedClassName + " " + example);
                });
    }

    private void errLine(String message) {
        if (withNormalizedOutput) {
            return;
        }
        System.err.println(message);
    }

    private void processNormalizedErrorOutput(Exception exception) throws IOException {
        if (!withNormalizedOutput) {
            return;
        }

        ErrorOutputDto errorOutputObject = ErrorOutputDto.fromException(exception);
        System.err.println(jsonWriter.writeValueAsString(errorOutputObject));
    }

    private void processNormalizedOutput() throws IOException {
        if (!withNormalizedOutput) {
            return;
        }

        if (commandResult == null) {
            System.out.println("{}");
        } else {
            System.out.println(jsonWriter.writeValueAsString(commandResult));
        }
    }
}

package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent class of all CLI tools
 */
public abstract class GenericTool {

    @Argument
    protected List<String> arguments = new ArrayList<>();

    /**
     * All-instance entry point.
     * @param args  : command line arguments
     */
    protected void doMain(String[] args) throws IOException {

        if (!checkArgumentsAndOptions(args)) {
            System.exit(1);
        }

        if (!commandDispatch()) {
            System.err.println("Error: command is not implemented, yet.");
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Should process according to provided command.
     * @return true if command has been correctly dispatched, false otherwise.
     */
    protected abstract boolean commandDispatch() throws IOException;

    /**
     * Should assign current command.
     * @param commandArgument   : value of provided command argument
     */
    protected abstract void assignCommand(String commandArgument);

    /**
     * Should check parameter validity and assign default ones, eventually.
     * @param parser    : command line parser instance to use
     * @throws CmdLineException
     */
    protected abstract void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException;

    /**
     * Should return one command enum instance.
     */
    protected abstract CommandHelper.CommandEnum getCommand();

    /**
     * Should return some usage examples.
     */
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

            printUsage(e);
            System.err.println();

            System.err.println("Error: invalid arguments are given.");
            System.err.println(e.getMessage());

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
                .forEach((entry) -> System.err.println(" " + entry.getKey() + " : " + entry.getValue() ));
        System.err.println();

        System.err.println("  .Options:");
        e.getParser().printUsage(System.err);
        System.err.println();

        System.err.println("  .Examples:");
        getExamples()
                .forEach((example) -> System.err.println(" " + displayedClassName + " " + example));
    }
}
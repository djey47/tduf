package fr.tduf.cli.tools;

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
    }

    /**
     * Should process according to provided command.
     * @return true if command has been correctly dispatched, false otherwise.
     */
    protected abstract boolean commandDispatch() throws IOException;

    /**
     * Should check command validity and assign current command if it is ok.
     * @param commandArgument   : value of provided command argument
     * @return true if command has been recognized and assigned, false otherwise.
     * @throws CmdLineException
     */
    protected abstract boolean checkAndAssignCommand(String commandArgument) throws CmdLineException;

    /**
     * Should check parameter validity and assign default ones, eventually.
     * @param parser    : command line parser instance to use
     * @throws CmdLineException
     */
    protected abstract void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException;

    /**
     * Should display all available commands to STDERR.
     * @param displayedClassName    : class name to use
     */
    protected abstract void printCommands(String displayedClassName);

    /**
     * Should display usage examples to STDERR.
     * @param displayedClassName    : class name to use
     */
    protected abstract void printExamples(String displayedClassName);

    private boolean checkArgumentsAndOptions(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            if (arguments.isEmpty()) {
                throw new CmdLineException(parser, "Error: No command is given.", null);
            }

            if (!checkAndAssignCommand(arguments.get(0))) {
                throw new CmdLineException(parser, "Error: An unsupported command is given.", null);
            }

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

    private void printUsage(CmdLineException e) {
        String displayedClassName = this.getClass().getSimpleName();

        System.err.println("Usage: " + displayedClassName + " command [-options]");
        System.err.println();

        System.err.println("  .Commands:");
        printCommands(displayedClassName);
        System.err.println();

        System.err.println("  .Options:");
        e.getParser().printUsage(System.err);
        System.err.println();

        System.err.println("  .Examples:");
        printExamples(displayedClassName);
    }
}
package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.low.files.db.DatabaseReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.DatabaseTool.Command.DUMP;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "UNPACKED TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-o", aliases = "--outputDir", usage = "Target directory for generated JSON files, defaults to current directory\\tdu-database-dump.")
    private String outputDirectory;

    @Argument
    private List<String> arguments = new ArrayList<>();

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        DUMP("dump", "Writes full database contents to JSON files.");

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
        new DatabaseTool().doMain(args);
    }

    private void doMain(String[] args) throws FileNotFoundException {
        if (!checkArgumentsAndOptions(args)) {
            System.exit(1);
        }

        switch (command) {
            case DUMP:
                dump();
                break;
            default:
                System.err.println("Error: command is not implemented, yet.");
                System.exit(1);
                break;
        }
    }

    private boolean checkArgumentsAndOptions(String[] args) {
        try {
            CmdLineParser parser = new CmdLineParser(this);
            parser.parseArgument(args);

            checkCommand(parser);

            // Database directory: defaulted to current
            if (databaseDirectory == null) {
                databaseDirectory = ".";
            }

            // Map file: defaulted to current directory\tdu-database-dump
            if (outputDirectory == null) {
                outputDirectory = "." + File.separator + "tdu-database-dump";
            }
        } catch (CmdLineException e) {
            String displayedName = this.getClass().getSimpleName();

            System.err.println(e.getMessage());
            System.err.println("Syntax: " + displayedName + " command [-options]");
            System.err.println();

            System.err.println("  Commands:");
            CommandHelper.getValuesAsMap(DUMP)
                    .forEach((label, description) -> System.err.println(label + " : " + description));
            System.err.println();

            System.err.println("  Options:");
            e.getParser().printUsage(System.err);
            System.err.println();

            System.err.println("  Example:");
            System.err.println(displayedName + " " + DUMP.label + " --databaseDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\"");
            return false;
        }
        return true;
    }

    private void checkCommand(CmdLineParser parser) throws CmdLineException {

        if (arguments.isEmpty()) {
            throw new CmdLineException(parser, "Error: No command is given.", null);
        }

        String commandArgument = arguments.get(0);

        if (!CommandHelper.getLabels(DUMP).contains(commandArgument)) {
            throw new CmdLineException(parser, "Error: An unsupported command is given.", null);
        }

        this.command = (Command) CommandHelper.fromLabel(DUMP, commandArgument);
    }

    private void dump() throws FileNotFoundException {
        File outputDirectory = new File(this.outputDirectory);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory);

            DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, outputDirectory.getAbsolutePath());

            System.out.println("Writing done for topic: " + currentTopic);
            System.out.println("Location: " + outputDirectory + File.separator + currentTopic.getLabel() + ".json");
        }
    }
}
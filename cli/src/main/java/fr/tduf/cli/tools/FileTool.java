package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.low.files.research.parser.GenericParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.FileTool.Command.JSONIFY;

/**
 * Command line interface for handling TDU files.
 */
//TODO extends GenericTool
public class FileTool {

    @Option(name="-i", aliases = "--inputFile", usage = "File to process, required.", required = true)
    private String inputFile;

    @Option(name="-o", aliases = "--outputFile", usage = "File to generate, defaults to inputFile.extension according to context." )
    private String outputFile;

    @Option(name="-s", aliases = "--structureFile", usage = "File describing input file structure, as JSON (required for jsonify operation)." )
    private String structureFile;

    @Argument
    private List<String> arguments = new ArrayList<>();

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        JSONIFY("jsonify", "Converts TDU file with structure to JSON file.");

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
        new FileTool().doMain(args);
    }

    private void doMain(String[] args) throws IOException {
        if (!checkArgumentsAndOptions(args)) {
            System.exit(1);
        }

        switch (command) {
            case JSONIFY:
                jsonify();
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

            // Output file: defaulted to input file.extension
            if (outputFile == null) {
                String extension;

                switch(command) {
                    case JSONIFY:
                        extension = ".json";
                        break;
                    default:
                        extension = ".";
                        break;
                }

                outputFile = inputFile + extension;
            }

            // Structure file :mandatory with jsonify
            if (structureFile == null && command == JSONIFY) {
                throw new CmdLineException(parser, "Error: structureFile is required.", null);
            }
        } catch (CmdLineException e) {
            String displayedName = this.getClass().getSimpleName();

            System.err.println(e.getMessage());
            System.err.println("Syntax: " + displayedName + " command [-options]");
            System.err.println();

            System.err.println("  Commands:");
            CommandHelper.getValuesAsMap(JSONIFY)
                    .forEach((label, description) -> System.err.println(label + " : " + description));
            System.err.println();

            System.err.println("  Options:");
            e.getParser().printUsage(System.err);
            System.err.println();

            System.err.println("  Example:");
            System.err.println(displayedName + " " + JSONIFY.label + " -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -s \"C:\\Users\\Bill\\Desktop\\BTRQ-map.json\"");
            return false;
        }
        return true;
    }

    private void checkCommand(CmdLineParser parser) throws CmdLineException {

        if (arguments.isEmpty()) {
            throw new CmdLineException(parser, "Error: No command is given.", null);
        }

        String commandArgument = arguments.get(0);

        if (!CommandHelper.getLabels(JSONIFY).contains(commandArgument)) {
            throw new CmdLineException(parser, "Error: An unsupported command is given.", null);
        }

        this.command = (Command) CommandHelper.fromLabel(JSONIFY, commandArgument);
    }

    private void jsonify() throws IOException {
        System.out.println("Will use structure in file: " + this.structureFile);

        byte[] fileContents = Files.readAllBytes(Paths.get(inputFile));
        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileContents);


        GenericParser<String> genericParser = new GenericParser<String>(fileInputStream) {
            @Override
            protected String generate() {
                return "BTRQ";
            }

            @Override
            protected String getStructureResource() {
                return structureFile;
            }
        };

        genericParser.parse();

        System.out.println("\t-> Provided file dump:\n" + genericParser.dump());

        String jsonOutput = genericParser.getDataStore().toJsonString();

        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonOutput);
        } catch (IOException e) {
            throw e;
        }

        System.out.println("JSON conversion done: " + this.inputFile + " to " + this.outputFile);
    }
}
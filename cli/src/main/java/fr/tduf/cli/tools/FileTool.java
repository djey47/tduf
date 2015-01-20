package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.low.files.research.parser.GenericParser;
import fr.tduf.libunlimited.low.files.research.writer.GenericWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.tduf.cli.tools.FileTool.Command.APPLYJSON;
import static fr.tduf.cli.tools.FileTool.Command.JSONIFY;

/**
 * Command line interface for handling TDU files.
 */
public class FileTool extends GenericTool {

    @Option(name="-i", aliases = "--inputFile", usage = "File to process, required.", required = true)
    private String inputFile;

    @Option(name="-o", aliases = "--outputFile", usage = "File to generate, defaults to inputFile.extension according to context." )
    private String outputFile;

    @Option(name="-s", aliases = "--structureFile", usage = "File describing input file structure, as JSON (required for jsonify and applyjson operations)." )
    private String structureFile;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        JSONIFY("jsonify", "Converts TDU file with structure to JSON file."),
        APPLYJSON("applyjson", "Rewrites TDU file from JSON file with structure.");

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

    @Override
    protected boolean checkAndAssignCommand(String commandArgument) {

        if (!CommandHelper.getLabels(JSONIFY).contains(commandArgument)) {
            return false;
        }

        this.command = (Command) CommandHelper.fromLabel(JSONIFY, commandArgument);

        return true;
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        // Output file: defaulted to input file.extension
        if (outputFile == null) {
            String extension;

            switch(command) {
                case JSONIFY:
                    extension = ".json";
                    break;
                case APPLYJSON:
                    extension = ".tdu";
                    break;
                default:
                    extension = ".";
                    break;
            }

            outputFile = inputFile + extension;
        }

        // Structure file: mandatory with jsonify/applyjson
        if (structureFile == null
                &&  (command == JSONIFY || command == APPLYJSON)) {
            throw new CmdLineException(parser, "Error: structureFile is required.", null);
        }
    }

    // TODO no stderr usage - return String list
    @Override
    protected void printCommands(String displayedClassName) {
        CommandHelper.getValuesAsMap(JSONIFY)
                .forEach((label, description) -> System.err.println(" " + label + " : " + description));
    }

    // TODO no stderr usage - return String list
    @Override
    protected void printExamples(String displayedClassName) {
        System.err.println(" " + displayedClassName + " " + JSONIFY.label + " -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -s \"C:\\Users\\Bill\\Desktop\\BTRQ-map.json\"");
        System.err.println(" " + displayedClassName + " " + APPLYJSON.label + " -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq.json\" -o \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -s \"C:\\Users\\Bill\\Desktop\\BTRQ-map.json\"");
    }

    @Override
    protected boolean commandDispatch() throws IOException {

        switch (command) {
            case JSONIFY:
                jsonify();
                break;
            case APPLYJSON:
                applyjson();
                break;
            default:
                return false;
        }

        return true;
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
        }

        System.out.println("TDU to JSON conversion done: " + this.inputFile + " to " + this.outputFile);
    }

    private void applyjson() throws IOException {
        System.out.println("Will use structure in file: " + this.structureFile);

        GenericWriter<String> genericWriter = new GenericWriter<String>("BTRQ") {
            @Override
            protected void fillStore() {}

            @Override
            protected String getStructureResource() {
                return structureFile;
            }
        };

        byte[] fileContents = Files.readAllBytes(Paths.get(inputFile));
        String jsonContents = new String(fileContents, StandardCharsets.UTF_8);

        genericWriter.getDataStore().fromJsonString(jsonContents);

        ByteArrayOutputStream outputStream = genericWriter.write();
        Files.write(Paths.get(outputFile), outputStream.toByteArray());

        System.out.println("JSON to TDU conversion done: " + this.inputFile + " to " + this.outputFile);
    }
}
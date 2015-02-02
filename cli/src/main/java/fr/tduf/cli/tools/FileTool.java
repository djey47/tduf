package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.low.files.common.crypto.CryptoHelper;
import fr.tduf.libunlimited.low.files.research.parser.GenericParser;
import fr.tduf.libunlimited.low.files.research.writer.GenericWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.List;

import static fr.tduf.cli.tools.FileTool.Command.*;
import static java.util.Arrays.asList;

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

    @Option(name="-c", aliases = "--cryptoMode", usage = "Value indicating encrypting method used (required for decrypt and encrypt operations). VAL:  0=savegames, 1=database/btrq..." )
    private String cryptoMode;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        DECRYPT("decrypt", "Makes protected TDU file readable by humans or other software."),
        ENCRYPT("encrypt", "Allows protected TDU file to be read by game engine."),
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
    protected void assignCommand(String commandArgument) {
        this.command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
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
                case DECRYPT:
                    extension = ".dec";
                    break;
                case ENCRYPT:
                    extension = ".enc";
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

        // Encryption mode: mandatory with decrypt/encrypt
        if (cryptoMode == null
                &&  (command == DECRYPT || command == ENCRYPT)) {
            throw new CmdLineException(parser, "Error: cryptoMode is required.", null);
        }

    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return JSONIFY;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                DECRYPT.label + " -c 1 -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -o \"C:\\Users\\Bill\\Desktop\\Brutal.btrq.ok\"",
                ENCRYPT.label + " -c 1 -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq.ok\" -o \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\"",
                JSONIFY.label + " -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -s \"C:\\Users\\Bill\\Desktop\\BTRQ-map.json\"",
                APPLYJSON.label + " -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq.json\" -o \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -s \"C:\\Users\\Bill\\Desktop\\BTRQ-map.json\"");
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
            case DECRYPT:
                decrypt();
                break;
            case ENCRYPT:
                encrypt();
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

    // TODO factorize
    private void decrypt() throws IOException {
        System.out.println("Now decrypting: " + this.inputFile + " with encryption mode " + this.cryptoMode);

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(Integer.valueOf(this.cryptoMode));
        Path inputFilePath = new File(this.inputFile).toPath();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFilePath));

        ByteArrayOutputStream outputStream;
        try {
            outputStream = CryptoHelper.decryptXTEA(inputStream, encryptionModeEnum);
        } catch (InvalidKeyException e) {
            throw new IOException("Should never happen.", e);
        }

        Files.write(Paths.get(this.outputFile), outputStream.toByteArray());

        System.out.println("Done: " + this.inputFile + " to " + this.outputFile);
    }

    // TODO factorize
    private void encrypt() throws IOException {
        System.out.println("Now encrypting: " + this.inputFile + " with encryption mode " + this.cryptoMode);

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(Integer.valueOf(this.cryptoMode));
        Path inputFilePath = new File(this.inputFile).toPath();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFilePath));

        ByteArrayOutputStream outputStream;
        try {
            outputStream = CryptoHelper.encryptXTEA(inputStream, encryptionModeEnum);
        } catch (InvalidKeyException e) {
            throw new IOException("Should never happen.", e);
        }

        Files.write(Paths.get(this.outputFile), outputStream.toByteArray());

        System.out.println("Done: " + this.inputFile + " to " + this.outputFile);
    }
}
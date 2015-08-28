package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.common.crypto.helper.CryptoHelper;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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

    private BankSupport bankSupport;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        DECRYPT("decrypt", "Makes protected TDU file readable by humans or other software."),
        ENCRYPT("encrypt", "Allows protected TDU file to be read by game engine."),
        JSONIFY("jsonify", "Converts TDU file with structure to JSON file."),
        APPLYJSON("applyjson", "Rewrites TDU file from JSON file with structure."),
        BANKINFO("bankinfo", "Gives details about a TDU Bank file."),
        UNPACK("unpack", "Extracts all contents from TDU Bank."),
        REPACK("repack", "Creates a BNK file, packing all contents in directory.");

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

    public FileTool() {
        this.bankSupport = new GenuineBnkGateway(new CommandLineHelper());
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
                case UNPACK:
                    extension = "-unpacked";
                    break;
                case REPACK:
                    extension = "-repacked.bnk";
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
                APPLYJSON.label + " -i \"C:\\Users\\Bill\\Desktop\\Brutal.btrq.json\" -o \"C:\\Users\\Bill\\Desktop\\Brutal.btrq\" -s \"C:\\Users\\Bill\\Desktop\\BTRQ-map.json\"",
                BANKINFO.label + " -i \"C:\\Users\\Bill\\Desktop\\DB.bnk\"",
                UNPACK.label + " -i \"C:\\Users\\Bill\\Desktop\\DB.bnk\" -o \"C:\\Users\\Bill\\Desktop\\DB_extracted\"",
                REPACK.label + " -i \"C:\\Users\\Bill\\Desktop\\DB.bnk.unpacked\"");
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
            case BANKINFO:
                bankInfo();
                break;
            case UNPACK:
                unpack();
                break;
            case REPACK:
                repack();
                break;
            default:
                return false;
        }

        return true;
    }

    private void repack() throws IOException {
        outLine("Will pack contents from directory: " + this.inputFile);

        bankSupport.packAll(this.inputFile, this.outputFile);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("contentsDirectory", this.inputFile);
        resultInfo.put("bankFileCreated", this.outputFile);
        commandResult = resultInfo;
    }

    private void unpack() throws IOException {
        outLine("Will use Bank file: " + this.inputFile);

        FilesHelper.createDirectoryIfNotExists(this.outputFile);

        bankSupport.extractAll(this.inputFile, this.outputFile);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("extractedContentsDirectory", this.outputFile);
        resultInfo.put("bankFile", this.inputFile);
        commandResult = resultInfo;
    }

    private void bankInfo() throws IOException {
        outLine("Will use Bank file: " + this.inputFile);

        BankInfoDto bankInfoObject = bankSupport.getBankInfo(this.inputFile);

        outLine("Done reading Bank:");
        outLine("\t-> Year: " + bankInfoObject.getYear());
        outLine("\t-> Size: " + bankInfoObject.getFileSize() + " bytes");
        outLine("\t-> Packed files (" + bankInfoObject.getPackedFiles().size() + "):");

        bankInfoObject.getPackedFiles()
                .forEach((packedFileInfoObject) -> outLine(
                        String.format("\t\t. %s (%s => %s) : %d bytes - %s",
                                packedFileInfoObject.getReference(),
                                packedFileInfoObject.getFullName(),
                                packedFileInfoObject.getShortName(),
                                packedFileInfoObject.getSize(),
                                packedFileInfoObject.getType())
                        ));

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("bankFile", this.inputFile);
        resultInfo.put("bankInfo", bankInfoObject);
        commandResult = resultInfo;
    }

    private void jsonify() throws IOException {
        outLine("Will use structure in file: " + this.structureFile);

        GenericParser<String> genericParser = getFileParser();
        genericParser.parse();

        outLine("\t-> Provided file dump:\n" + genericParser.dump());

        parserToJsonFile(genericParser);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("tduFile", this.inputFile);
        resultInfo.put("jsonFile", this.outputFile);
        commandResult = resultInfo;
    }

    private void applyjson() throws IOException {
        outLine("Will use structure in file: " + this.structureFile);

        writerToBinaryFile(getFileWriter(), readJsonInputFileContents());

        outLine("JSON to TDU conversion done: " + this.inputFile + " to " + this.outputFile);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("tduFile", this.outputFile);
        resultInfo.put("jsonFile", this.inputFile);
        commandResult = resultInfo;
    }

    private void decrypt() throws IOException {
        outLine("Now decrypting: " + this.inputFile + " with encryption mode " + this.cryptoMode);

        Files.write(Paths.get(this.outputFile), processInputStream(false));

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("encryptedFile", this.inputFile);
        resultInfo.put("clearFile", this.outputFile);
        commandResult = resultInfo;
    }

    private void encrypt() throws IOException {
        outLine("Now encrypting: " + this.inputFile + " with encryption mode " + this.cryptoMode);

        Files.write(Paths.get(this.outputFile), processInputStream(true));

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("clearFile", this.inputFile);
        resultInfo.put("encryptedFile", this.outputFile);
        commandResult = resultInfo;
    }

    private GenericParser<String> getFileParser() throws IOException {
        return new GenericParser<String>(getInputStreamForInputFile()) {
            @Override
            protected String generate() {
                return null;
            }

            @Override
            public String getStructureResource() {
                return structureFile;
            }
        };
    }

    private GenericWriter<String> getFileWriter() throws IOException {
        return new GenericWriter<String>("") {
                @Override
                protected void fillStore() {}

                @Override
                public String getStructureResource() {
                    return structureFile;
                }
            };
    }

    private void writerToBinaryFile(GenericWriter<String> genericWriter, String jsonContents) throws IOException {
        genericWriter.getDataStore().fromJsonString(jsonContents);

        Files.write(Paths.get(outputFile), genericWriter.write().toByteArray());
    }

    private void parserToJsonFile(GenericParser<String> genericParser) throws IOException {
        String jsonOutput = genericParser.getDataStore().toJsonString();

        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonOutput);
        }
    }

    private String readJsonInputFileContents() throws IOException {
        byte[] fileContents = Files.readAllBytes(Paths.get(inputFile));
        return new String(fileContents, StandardCharsets.UTF_8);
    }

    private byte[] processInputStream(boolean withEncryption) throws IOException {

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(Integer.valueOf(this.cryptoMode));

        ByteArrayInputStream inputStream = getInputStreamForInputFile();
        ByteArrayOutputStream outputStream;
        if (withEncryption) {
            outputStream = CryptoHelper.encryptXTEA(inputStream, encryptionModeEnum);
        } else {
            outputStream = CryptoHelper.decryptXTEA(inputStream, encryptionModeEnum);
        }
        return outputStream.toByteArray();
    }

    private ByteArrayInputStream getInputStreamForInputFile() throws IOException {
        Path inputFilePath = new File(this.inputFile).toPath();
        return new ByteArrayInputStream(Files.readAllBytes(inputFilePath));
    }
}
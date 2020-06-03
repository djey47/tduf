package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.helper.JsonHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.common.crypto.helper.CryptoHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.JsonAdapter;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;
import fr.tduf.libunlimited.low.files.research.rw.StructureBasedProcessor;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.cli.tools.FileTool.Command.*;
import static java.util.Arrays.asList;

/**
 * Command line interface for handling TDU files.
 */
public class FileTool extends GenericTool {

    @Option(name = "-i", aliases = "--inputFile", usage = "File to process, required.", required = true)
    private String inputFile;

    @Option(name = "-o", aliases = "--outputFile", usage = "File to generate, defaults to inputFile.extension according to context.")
    private String outputFile;

    @SuppressWarnings("unused")
    @Option(name = "-s", aliases = "--structureFile", usage = "File describing input file structure, as JSON (used for jsonify and applyjson operations). Optional, TDUF will try to fetch compatible structure automatically, if it exists.")
    private String structureFile;

    @SuppressWarnings("unused")
    @Option(name = "-c", aliases = "--cryptoMode", usage = "Value indicating encrypting method used (required for decrypt and encrypt operations). VAL:  0=savegames, 1=database/btrq...")
    private String cryptoMode;

    private Command command;

    @SuppressWarnings("FieldMayBeFinal")
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

    FileTool() {
        bankSupport = new GenuineBnkGateway(new CommandLineHelper());
    }

    @Override
    protected void assignCommand(String commandArgument) {
        command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        // Input file: trailing separator to be removed
        inputFile = StringUtils.removeEnd(inputFile, File.separator);

        // Output file: defaulted to input file.extension
        if (outputFile == null) {
            String extension;

            switch (command) {
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

        // Encryption mode: mandatory with decrypt/encrypt
        if (cryptoMode == null
                && (command == DECRYPT || command == ENCRYPT)) {
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
                JSONIFY.label + " -i \"C:\\Users\\Bill\\Desktop\\Bnk1.map\"",
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
                commandResult = jsonify(structureFile, inputFile, outputFile);
                break;
            case APPLYJSON:
                commandResult = applyjson(structureFile, inputFile, outputFile);
                break;
            case DECRYPT:
                commandResult = decrypt(inputFile, outputFile);
                break;
            case ENCRYPT:
                commandResult = encrypt(inputFile, outputFile);
                break;
            case BANKINFO:
                commandResult = bankInfo(inputFile);
                break;
            case UNPACK:
                commandResult = unpack(inputFile, outputFile);
                break;
            case REPACK:
                commandResult = repack(inputFile, outputFile);
                break;
            default:
                commandResult = null;
                return false;
        }

        return true;
    }

    private Map<String, Object> repack(String sourceDirectory, String targetBankFile) throws IOException {
        outLine("Will pack contents from directory: " + sourceDirectory);

        bankSupport.packAll(sourceDirectory, targetBankFile);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("contentsDirectory", sourceDirectory);
        resultInfo.put("bankFileCreated", targetBankFile);

        return resultInfo;
    }

    private Map<String, ?> unpack(String sourceBankFile, String targetDirectory) throws IOException {
        outLine("Will use Bank file: " + sourceBankFile);

        FilesHelper.createDirectoryIfNotExists(targetDirectory);

        bankSupport.extractAll(sourceBankFile, targetDirectory);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("extractedContentsDirectory", targetDirectory);
        resultInfo.put("bankFile", sourceBankFile);

        return resultInfo;
    }

    private Map<String, ?> bankInfo(String sourceBankFile) throws IOException {
        outLine("Will use Bank file: " + sourceBankFile);

        BankInfoDto bankInfoObject = bankSupport.getBankInfo(sourceBankFile);

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

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("bankFile", sourceBankFile);
        resultInfo.put("bankInfo", bankInfoObject);

        return resultInfo;
    }

    private Map<String, ?> jsonify(String structureFile, String sourceFile, String targetJsonFile) throws IOException {
        GenericParser<String> genericParser = getFileParser(sourceFile, structureFile);
        genericParser.parse();

        logStructureInfo(structureFile, genericParser);

        outLine("\t-> Dump of provided file:\n" + genericParser.dump());

        parserToJsonFile(targetJsonFile, genericParser);

        outLine("\t-> Written JSON file:\n" + targetJsonFile);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("tduFile", sourceFile);
        resultInfo.put("jsonFile", targetJsonFile);
        resultInfo.put("structureJsonFile", structureFile);
        resultInfo.put("structure", genericParser.getStructure().getName());

        return resultInfo;
    }

    private Map<String, Object> applyjson(String structureFile, String sourceJsonFile, String targetFile) throws IOException {
        GenericWriter<String> genericWriter = getFileWriter(targetFile, structureFile);

        logStructureInfo(structureFile, genericWriter);

        writerToBinaryFile(genericWriter, readJsonInputFileContents());

        outLine("JSON to TDU conversion done: " + sourceJsonFile + " to " + targetFile);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("tduFile", targetFile);
        resultInfo.put("jsonFile", sourceJsonFile);
        resultInfo.put("structureJsonFile", structureFile);
        resultInfo.put("structure", genericWriter.getStructure().getName());

        return resultInfo;
    }

    private Map<String, Object> decrypt(String sourceEncryptedFile, String targetFile) throws IOException {
        outLine("Now decrypting: " + sourceEncryptedFile + " with encryption mode " + cryptoMode);

        Files.write(Paths.get(targetFile), processInputStream(sourceEncryptedFile, false));

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("encryptedFile", sourceEncryptedFile);
        resultInfo.put("clearFile", targetFile);

        return resultInfo;
    }

    private Map<String, Object> encrypt(String sourceFile, String targetEncryptedFile) throws IOException {
        outLine("Now encrypting: " + sourceFile + " with encryption mode " + cryptoMode);

        Files.write(Paths.get(targetEncryptedFile), processInputStream(sourceFile, true));

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("clearFile", sourceFile);
        resultInfo.put("encryptedFile", targetEncryptedFile);

        return resultInfo;
    }

    private void logStructureInfo(String structureFile, StructureBasedProcessor genericProcessor) {
        if (structureFile == null) {
            outLine("-> Will use default structure: " + genericProcessor.getStructure().getName());
        } else {
            outLine("-> Will use structure in file: " + structureFile);
        }
    }

    private GenericParser<String> getFileParser(final String sourceFile, final String structureFile) throws IOException {
        return new GenericParser<String>(getInputStreamForFile(sourceFile)) {
            @Override
            protected String generate() {
                return null;
            }

            @Override
            public String getStructureResource() {
                return structureFile;
            }

            @Override
            public FileStructureDto getStructure() {
                return searchDefaultStructure(sourceFile);
            }
        };
    }

    private GenericWriter<String> getFileWriter(final String targetFile, final String structureFile) throws IOException {
        return new GenericWriter<String>("") {
            @Override
            protected void fillStore() {
            }

            @Override
            public String getStructureResource() {
                return structureFile;
            }

            @Override
            public FileStructureDto getStructure() {
                return searchDefaultStructure(targetFile);
            }
        };
    }

    private void writerToBinaryFile(GenericWriter<String> genericWriter, String jsonContents) throws IOException {
        new JsonAdapter(genericWriter.getDataStore()).fromJsonString(jsonContents);

        Files.write(Paths.get(outputFile), genericWriter.write().toByteArray());
    }

    private void parserToJsonFile(String targetJsonFile, GenericParser<String> genericParser) throws IOException {
        String rawJsonOutput = new JsonAdapter(genericParser.getDataStore()).toJsonString();
        String formattedJsonOutput = JsonHelper.prettify(rawJsonOutput);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(targetJsonFile), StandardCharsets.UTF_8)) {
            bufferedWriter.write(formattedJsonOutput);
        }
    }

    private String readJsonInputFileContents() throws IOException {
        byte[] fileContents = Files.readAllBytes(Paths.get(inputFile));
        return new String(fileContents, StandardCharsets.UTF_8);
    }

    private byte[] processInputStream(String sourceFile, boolean withEncryption) throws IOException {

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(Integer.parseInt(cryptoMode));

        XByteArrayInputStream inputStream = getInputStreamForFile(sourceFile);
        ByteArrayOutputStream outputStream;
        if (withEncryption) {
            outputStream = CryptoHelper.encryptXTEA(inputStream, encryptionModeEnum);
        } else {
            outputStream = CryptoHelper.decryptXTEA(inputStream, encryptionModeEnum);
        }
        return outputStream.toByteArray();
    }

    private XByteArrayInputStream getInputStreamForFile(String sourceFile) throws IOException {
        Path inputFilePath = new File(sourceFile).toPath();
        return new XByteArrayInputStream(Files.readAllBytes(inputFilePath));
    }

    private static FileStructureDto searchDefaultStructure(String fileName)  {
        try {
            return StructureHelper.retrieveStructureFromSupportedFileName(fileName)
                    .orElseThrow(() -> new IllegalArgumentException("No default structure found for " + fileName));
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to find default structure for " + fileName);
        }
    }
}

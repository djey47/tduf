package fr.tduf.libunlimited.high.files.banks.interop;

import com.google.common.base.Joiner;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBankInfoOutputDto;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static fr.tduf.libunlimited.common.helper.CommandLineHelper.EXIT_CODE_SUCCESS;
import static java.util.stream.Collectors.toList;

/**
 * Bnk support, implementation relying on TDUMT-cli application.
 */
public class GenuineBnkGateway implements BankSupport {

    private static final String EXTENSION_BANKS = "bnk";

    private static final String PATH_SEPARATOR_REGEX = "\\\\";

    static final String ORIGINAL_BANK_NAME = "originalBank.bnk";
    static final String EXE_TDUMT_CLI = ".\\tools\\tdumt-cli\\tdumt-cli.exe";
    static final String CLI_COMMAND_BANK_INFO = "BANK-I";
    static final String CLI_COMMAND_BANK_UNPACK = "BANK-U";
    static final String CLI_COMMAND_BANK_REPLACE = "BANK-R";
    public static final String PREFIX_PACKED_FILE_PATH = "D:\\Eden-Prog\\Games\\TestDrive\\Resources\\";
    public static final int PREFIX_PACKED_FILE_COMPOUNDS_SIZE = 5;

    private CommandLineHelper commandLineHelper;

    public GenuineBnkGateway(CommandLineHelper commandLineHelper) {
        this.commandLineHelper = commandLineHelper;
    }

    /**
     * tdumt-cli syntax: BANK-I <bankFileName>
     */
    @Override
    public BankInfoDto getBankInfo(String bankFileName) throws IOException {

        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_INFO, bankFileName);
        handleCommandLineErrors(processResult);

        GenuineBankInfoOutputDto outputObject = new ObjectMapper().readValue(processResult.getOut(), GenuineBankInfoOutputDto.class);

        List<PackedFileInfoDto> packedFilesInfos = outputObject.getPackedFiles().stream()

                .map((packedFileInfo) -> PackedFileInfoDto.builder()
                        .forReference(generatePackedFileReference(packedFileInfo.getName()))
                        .withSize(packedFileInfo.getFileSize())
                        .withFullName(packedFileInfo.getName())
                        .build())

                .collect(toList());

        return BankInfoDto.builder()
                .fromYear(outputObject.getYear())
                .withFileSize(outputObject.getFileSize())
                .addPackedFiles(packedFilesInfos)
                .build();
    }

    /**
     * tdumt-cli syntax: BANK-U <bankFileName> <packedFilePath> <outputDirectory>
     */
    @Override
    public void extractAll(String bankFileName, String outputDirectory) throws IOException {

        Files.copy(Paths.get(bankFileName), Paths.get(outputDirectory, ORIGINAL_BANK_NAME), StandardCopyOption.REPLACE_EXISTING);

        BankInfoDto bankInfoObject = getBankInfo(bankFileName);

        bankInfoObject.getPackedFiles().stream()

                .map(PackedFileInfoDto::getFullName)

                .forEach((fileName) -> {
                    try {
                        extractPackedFileWithFullPath(bankFileName, fileName, outputDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * tdumt-cli syntax: BANK-R <bankFileName> <packedFilePath> <sourceFilePath>
     */
    @Override
    public void packAll(String inputDirectory, String outputBankFileName) throws IOException {

        Path originalBankFilePath = Paths.get(inputDirectory, ORIGINAL_BANK_NAME);

        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        Path inputPath = Paths.get(inputDirectory);

        Files.walk(inputPath)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> !EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    String packedFilePath = getInternalPackedFilePath(path, inputPath);
                    try {
                        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_REPLACE, outputBankFileName, packedFilePath, path.toString());
                        handleCommandLineErrors(processResult);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Error while repacking file: " + packedFilePath, ioe);
                    }
                });
    }

    static String getInternalPackedFilePath(Path packedFilePath, Path basePath) {
        Path pathRelative = basePath.relativize(packedFilePath);

        String[] pathCompounds = pathRelative.toString().replace('/', '\\').split("\\\\");
        String[] pathElements = new String[pathCompounds.length + 1];

        System.arraycopy(pathCompounds, 0, pathElements, 0, pathCompounds.length);

        String name = pathElements[pathElements.length - 2].split("\\.")[0];
        String extension = "." + pathElements[pathElements.length - 2].split("\\.")[1];
        pathElements[pathElements.length - 2] = extension;
        pathElements[pathElements.length - 1] = name;

        return PREFIX_PACKED_FILE_PATH + Joiner.on('\\').join(pathElements);
    }

    /**
     * Output example:  'mybank.bnk/A3_V6.3DD'
     */
    static String getTargetFileNameFromPathCompounds(String bankFileName, String[] filePathCompounds) {
        String[] pathElements = new String[filePathCompounds.length - 1 - PREFIX_PACKED_FILE_COMPOUNDS_SIZE];

        System.arraycopy(filePathCompounds, PREFIX_PACKED_FILE_COMPOUNDS_SIZE, pathElements, 0, pathElements.length);
        pathElements[pathElements.length - 1] = getFileNameFromPathCompounds(filePathCompounds);

        return Paths.get(bankFileName, pathElements).toString();
    }

    /**
     * Output example:  {'D:', 'Eden-Prog', 'Games', ..., '.3DD', 'A3_V6'} -> 'A3_V6.3DD'
     */
    static String getFileNameFromPathCompounds(String[] filePathCompounds) {
        return filePathCompounds[filePathCompounds.length-1] + filePathCompounds[filePathCompounds.length-2];
    }

    static String generatePackedFileReference(String fileName) {
        long hash = fileName.hashCode();

        if (hash >= 0) {
            return Long.valueOf(hash).toString();
        } else {
            return Long.valueOf(Integer.MAX_VALUE + Math.abs(hash)).toString();
        }
    }

    private void extractPackedFileWithFullPath(String bankFileName, String filePath, String outputDirectory) throws IOException {
        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_UNPACK, bankFileName, filePath, outputDirectory);
        handleCommandLineErrors(processResult);

        String[] filePathCompounds = filePath.split(PATH_SEPARATOR_REGEX);

        File bank = new File(bankFileName);
        File extractedFile = new File(outputDirectory, getFileNameFromPathCompounds(filePathCompounds));
        File targetFile = new File(outputDirectory, getTargetFileNameFromPathCompounds(bank.getName(), filePathCompounds));

        Files.move(extractedFile.toPath(), targetFile.toPath());
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }
}
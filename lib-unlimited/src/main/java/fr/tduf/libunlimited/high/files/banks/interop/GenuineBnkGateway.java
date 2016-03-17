package fr.tduf.libunlimited.high.files.banks.interop;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Joiner;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBankInfoOutputDto;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBatchInputDto;
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
import java.util.Set;

import static fr.tduf.libunlimited.common.helper.CommandLineHelper.EXIT_CODE_SUCCESS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Bnk support, implementation relying on TDUMT-cli application.
 */
public class GenuineBnkGateway implements BankSupport {

    private static final Class<GenuineBnkGateway> thisClass = GenuineBnkGateway.class;
    // TODO use this constant instead
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    public static final String EXTENSION_BANKS = "bnk";
    public static final String PREFIX_ORIGINAL_BANK_FILE = "original-";

    static final String EXE_TDUMT_CLI = Paths.get(".", "tools", "tdumt-cli", "tdumt-cli.exe").toString();
    static final String CLI_COMMAND_BANK_INFO = "BANK-I";
    static final String CLI_COMMAND_BANK_UNPACK = "BANK-U";
    static final String CLI_COMMAND_BANK_REPLACE = "BANK-R";
    static final String CLI_COMMAND_BANK_BATCH_UNPACK = "BANK-UX";
    static final String CLI_COMMAND_BANK_BATCH_REPLACE = "BANK-RX";

    private static final String PREFIX_PACKED_FILE_PATH = "D:\\Eden-Prog\\Games\\TestDrive\\Resources\\";

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

        return mapGenuineBankInfoToBankInfoObject(outputObject);
    }

    /**
     * tdumt-cli syntax: BANK-UX <bankFileName> <batchInputPath>
     */
    @Override
    public void extractAll(String bankFileName, String outputDirectory) throws IOException {

        Path bankFilePath = Paths.get(bankFileName);
        Files.copy(bankFilePath, Paths.get(outputDirectory, PREFIX_ORIGINAL_BANK_FILE + bankFilePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);

        Set<GenuineBatchInputDto.Item> batchItems = createBatchItemsToExtract(bankFileName, outputDirectory);
        GenuineBatchInputDto batchInputObject = GenuineBatchInputDto.builder()
                .addItems(batchItems)
                .build();

        batchExtractPackedFilesWithFullPath(bankFileName, batchInputObject);
    }

    /**
     * tdumt-cli syntax: BANK-R <bankFileName> <packedFilePath> <sourceFilePath>
     */
    @Override
    public void packAll(String inputDirectory, String outputBankFileName) throws IOException {

        Log.debug(thisClass.getSimpleName(), "inputDirectory: " + inputDirectory);
        Log.debug(thisClass.getSimpleName(), "outputBankFileName: " + outputBankFileName);

        Path originalBankFilePath = searchOriginalBankPath(inputDirectory);
        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        Log.debug(thisClass.getSimpleName(), "originalBankFilePath: " + originalBankFilePath);

        getBankInfo(originalBankFilePath.toString()).getPackedFiles()

                .forEach((infoObject) -> repackFileWithFullPath(infoObject.getFullName(), outputBankFileName, Paths.get(inputDirectory)));
    }

    static Path searchOriginalBankPath(String inputDirectory) throws IOException {
        return Files.walk(Paths.get(inputDirectory), 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .findAny().get();
    }

    static String getInternalPathFromRealPath(Path realPath, Path basePath) {
        Path pathRelative = basePath.relativize(realPath);

        String[] pathCompounds = pathRelative.toString().replace('/', '\\').split("\\\\");
        String[] pathElements = new String[pathCompounds.length + 1];

        System.arraycopy(pathCompounds, 0, pathElements, 0, pathCompounds.length);

        String name = pathElements[pathElements.length - 2].split("\\.")[0];
        String extension = "." + pathElements[pathElements.length - 2].split("\\.")[1];
        pathElements[pathElements.length - 2] = extension;
        pathElements[pathElements.length - 1] = name;

        return PREFIX_PACKED_FILE_PATH + Joiner.on('\\').join(pathElements);
    }

    static Path getRealFilePathFromInternalPath(String internalPath, Path basePath) {

        Path filePath = Paths.get(internalPath
                .replace(PREFIX_PACKED_FILE_PATH, "")
                .replace("\\", File.separator));

        int shortNameComponentIndex = filePath.getNameCount() - 1;
        int extensionComponentIndex = filePath.getNameCount() - 2;
        String shortFileName = filePath.subpath(shortNameComponentIndex, filePath.getNameCount()).toString();
        String extension = filePath.subpath(extensionComponentIndex, shortNameComponentIndex).toString();

        Path realFilePath = filePath.subpath(0, extensionComponentIndex).resolve(shortFileName + extension);

        return basePath.resolve(realFilePath);
    }

    static String generatePackedFileReference(String fileName) {
        long hash = fileName.hashCode();

        if (hash >= 0) {
            return Long.valueOf(hash).toString();
        } else {
            return Long.valueOf(Integer.MAX_VALUE + Math.abs(hash)).toString();
        }
    }

    private Set<GenuineBatchInputDto.Item> createBatchItemsToExtract(String bankFileName, String outputDirectory) throws IOException {
        return getBankInfo(bankFileName).getPackedFiles().stream()
                .map(PackedFileInfoDto::getFullName)
                .map((packedPath) -> {
                    String externalFile = getRealFilePathFromInternalPath(packedPath, Paths.get(outputDirectory)).toString();

                    return GenuineBatchInputDto.Item.builder()
                            .forPackedPath(packedPath)
                            .withExternalFileName(externalFile)
                            .build();
                })
                .collect(toSet());
    }

    private void batchExtractPackedFilesWithFullPath(String bankFile, GenuineBatchInputDto batchInputObject) {
        try {
            Log.debug(thisClass.getSimpleName(), "bankFile: " + bankFile);
            Log.debug(thisClass.getSimpleName(), "batchInputObject: " + batchInputObject);

            batchInputObject.getItems()
                    .forEach((item) -> {
                        try {
                            Files.createDirectories(Paths.get(item.getExternalFile()).getParent());
                        } catch (IOException e) {
                            // Do not fail here.
                            e.printStackTrace();
                        }
                    });

            File batchInputFile = Files.createTempDirectory("libUnlimited-banks").resolve("BatchUnpackInput.json").toFile();
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(batchInputFile, batchInputObject);

            String batchInputFileName = batchInputFile.getAbsolutePath();
            Log.debug(thisClass.getSimpleName(), "batchInputFileName: " + batchInputFileName);

            ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_BATCH_UNPACK, bankFile, batchInputFileName);
            handleCommandLineErrors(processResult);
        } catch (IOException e) {
            // Do not fail here.
            e.printStackTrace();
        }
    }

    private void repackFileWithFullPath(String packedFilePath, String outputBankFile, Path basePath) {
        try {
            Path filePath = getRealFilePathFromInternalPath(packedFilePath, basePath);

            Log.debug(thisClass.getSimpleName(), "packedFilePath: " + packedFilePath);
            Log.debug(thisClass.getSimpleName(), "filePath: " + filePath.toString());
            Log.debug(thisClass.getSimpleName(), "basePath: " + basePath.toString());

            ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_REPLACE, outputBankFile, packedFilePath, filePath.toString());
            handleCommandLineErrors(processResult);
        } catch (IOException ioe) {
            throw new RuntimeException("Error while repacking file: " + packedFilePath, ioe);
        }
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }

    private static BankInfoDto mapGenuineBankInfoToBankInfoObject(GenuineBankInfoOutputDto outputObject) {
        List<PackedFileInfoDto> packedFilesInfos = outputObject.getPackedFiles().stream()

                .map((packedFileInfo) -> PackedFileInfoDto.builder()
                        .forReference(generatePackedFileReference(packedFileInfo.getName()))
                        .withSize(packedFileInfo.getFileSize())
                        .withFullName(packedFileInfo.getName())
                        .withShortName(packedFileInfo.getShortName())
                        .withTypeDescription(packedFileInfo.getType())
                        .build())

                .collect(toList());

        return BankInfoDto.builder()
                .fromYear(outputObject.getYear())
                .withFileSize(outputObject.getFileSize())
                .addPackedFiles(packedFilesInfos)
                .build();
    }
}

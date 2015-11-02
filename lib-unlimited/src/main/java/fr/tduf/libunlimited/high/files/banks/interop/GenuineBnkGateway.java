package fr.tduf.libunlimited.high.files.banks.interop;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Joiner;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBankInfoOutputDto;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import org.codehaus.jackson.map.ObjectMapper;

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

    private static final Class<GenuineBnkGateway> thisClass = GenuineBnkGateway.class;

    public static final String EXTENSION_BANKS = "bnk";

    static final String EXE_TDUMT_CLI = ".\\tools\\tdumt-cli\\tdumt-cli.exe";
    static final String CLI_COMMAND_BANK_INFO = "BANK-I";
    static final String CLI_COMMAND_BANK_UNPACK = "BANK-U";
    static final String CLI_COMMAND_BANK_REPLACE = "BANK-R";

    static final String PREFIX_ORIGINAL_BANK_FILE = "original-";

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

    /**
     * tdumt-cli syntax: BANK-U <bankFileName> <packedFilePath> <outputDirectory>
     */
    @Override
    public void extractAll(String bankFileName, String outputDirectory) throws IOException {

        Path bankFilePath = Paths.get(bankFileName);
        Files.copy(bankFilePath, Paths.get(outputDirectory, PREFIX_ORIGINAL_BANK_FILE + bankFilePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);

        BankInfoDto bankInfoObject = getBankInfo(bankFileName);
        bankInfoObject.getPackedFiles()

                .forEach((infoObject) -> {
                    try {
                        extractPackedFileWithFullPath(bankFilePath, infoObject, outputDirectory);
                    } catch (IOException e) {
                        // Do not fail here.
                        e.printStackTrace();
                    }
                });
    }

    /**
     * tdumt-cli syntax: BANK-R <bankFileName> <packedFilePath> <sourceFilePath>
     */
    @Override
    public void packAll(String inputDirectory, String outputBankFileName) throws IOException {

        Log.debug(thisClass.getSimpleName(), "inputDirectory: " + inputDirectory);
        Log.debug(thisClass.getSimpleName(), "outputBankFileName: " + outputBankFileName);

        String originalBankFileName = searchOriginalBankFileName(inputDirectory);
        Path originalBankFilePath = Paths.get(inputDirectory, originalBankFileName);
        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        Log.debug(thisClass.getSimpleName(), "originalBankFilePath: " + originalBankFilePath.toString());

        Path inputPath = Paths.get(inputDirectory);
        Files.walk(inputPath)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> !EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    String packedFilePath = getInternalPackedFilePath(path, inputPath);
                    try {
                        Log.debug(thisClass.getSimpleName(), "packedFilePath: " + packedFilePath);
                        Log.debug(thisClass.getSimpleName(), "path: " + path.toString());

                        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_REPLACE, outputBankFileName, packedFilePath, path.toString());
                        handleCommandLineErrors(processResult);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Error while repacking file: " + packedFilePath, ioe);
                    }
                });
    }

    /**
     * To be used only with database repacking ! (file layout does not need packed folders)
     * Copies original-bnk files to bank name subdirectory.
     */
    @Override
    public void preparePackAll(String sourceDirectory, String targetBankFileName) throws IOException {
        String originalBankFileName = GenuineBnkGateway.PREFIX_ORIGINAL_BANK_FILE + targetBankFileName;
        Path originalBankFilePath = Paths.get(sourceDirectory, originalBankFileName);
        Files.copy(originalBankFilePath, Paths.get(sourceDirectory, targetBankFileName, originalBankFileName));
    }

    static String searchOriginalBankFileName(String inputDirectory) throws IOException {
        return Files.walk(Paths.get(inputDirectory))

                    .filter((path) -> !Files.isDirectory(path))

                    .filter((path) -> EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                    .findAny().get().getFileName().toString();
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

    static Path getUnpackedFilePath(String fullPackedFileName, Path basePath) {
        String[] nameCompounds = fullPackedFileName.split("\\\\");
        String shortName = nameCompounds[nameCompounds.length - 1];
        String extension = nameCompounds[nameCompounds.length - 2];
        String shortFileName = shortName + extension;

        String[] prefixCompounds = PREFIX_PACKED_FILE_PATH.split("\\\\");

        Path fullPackedFilePath = Paths.get("", nameCompounds);
        Path followingPath = Paths.get("", prefixCompounds).relativize(fullPackedFilePath);
        Path followingPathWithoutFileName = followingPath.getParent().getParent();

        return basePath.resolve(followingPathWithoutFileName).resolve(shortFileName);
    }

    static String generatePackedFileReference(String fileName) {
        long hash = fileName.hashCode();

        if (hash >= 0) {
            return Long.valueOf(hash).toString();
        } else {
            return Long.valueOf(Integer.MAX_VALUE + Math.abs(hash)).toString();
        }
    }

    private void extractPackedFileWithFullPath(Path bankFilePath, PackedFileInfoDto packedFileInfo, String outputDirectory) throws IOException {
        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_UNPACK, bankFilePath.toString(), packedFileInfo.getFullName(), outputDirectory);
        handleCommandLineErrors(processResult);

        Path extractedPath = Paths.get(outputDirectory, packedFileInfo.getShortName());


        Path targetPath = getUnpackedFilePath(packedFileInfo.getFullName(), Paths.get(outputDirectory));

        Files.createDirectories(targetPath.getParent());
        Files.move(extractedPath, targetPath);
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }
}
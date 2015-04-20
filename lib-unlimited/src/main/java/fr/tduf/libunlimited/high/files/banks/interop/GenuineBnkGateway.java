package fr.tduf.libunlimited.high.files.banks.interop;

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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static fr.tduf.libunlimited.common.helper.CommandLineHelper.EXIT_CODE_SUCCESS;
import static java.util.stream.Collectors.toList;

/**
 * Bnk support, implementation relying on TDUMT-cli application.
 */
public class GenuineBnkGateway implements BankSupport {

    static final String EXE_TDUMT_CLI = ".\\tools\\tdumt-cli\\tdumt-cli.exe";
    static final String CLI_COMMAND_BANK_INFO = "BANK-I";
    static final String CLI_COMMAND_BANK_UNPACK = "BANK-U";
    static final String CLI_COMMAND_BANK_REPLACE = "BANK-R";

    static final String PREFIX_ORIGINAL_BANK_FILE = "original-";

    private static final String PREFIX_PACKED_FILE_PATH = "D:\\Eden-Prog\\Games\\TestDrive\\Resources\\";

    private static final String EXTENSION_BANKS = "bnk";

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

        String originalBankFileName = searchOriginalBankFileName(inputDirectory);

        Path originalBankFilePath = Paths.get(inputDirectory, originalBankFileName);
        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        Path inputPath = Paths.get(inputDirectory, originalBankFileName);
        Files.walk(inputPath)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> !EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .filter((path) -> {
                    try {
                        BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
                        // TODO creation time would be better than last access time - but not handled on Linux (failing tests)
                        return fileAttributes.lastModifiedTime().compareTo(fileAttributes.lastAccessTime()) > 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return true;
                    }
                })

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

    /**
     * To be used only with database repacking ! (file layout does not need packed folders)
     */
    @Override
    public void prepareFilesToBeRepacked(String sourceDirectory, List<Path> repackedPaths, String targetBankFileName, String targetDirectory) throws IOException {

        String originalBankFileName = PREFIX_ORIGINAL_BANK_FILE + targetBankFileName;
        Files.copy(Paths.get(sourceDirectory, originalBankFileName), Paths.get(targetDirectory, originalBankFileName));

        Files.createDirectory(Paths.get(targetDirectory, targetBankFileName));

        repackedPaths

                .forEach((filePath) -> {
                    Path targetPath = Paths.get(targetDirectory, targetBankFileName, filePath.getFileName().toString());
                    try {
                        Files.copy(filePath, targetPath);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to recreate file structure: " + targetPath, ioe);
                    }
                });
    }

    static String searchOriginalBankFileName(String inputDirectory) throws IOException {
        return Files.walk(Paths.get(inputDirectory))

                    .filter((path) -> Files.isDirectory(path))

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
        Path targetPath = Paths.get(outputDirectory, bankFilePath.getFileName().toString(), packedFileInfo.getShortName());

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
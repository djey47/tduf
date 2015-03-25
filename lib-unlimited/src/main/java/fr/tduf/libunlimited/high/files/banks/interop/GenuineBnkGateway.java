package fr.tduf.libunlimited.high.files.banks.interop;

import com.google.common.base.Joiner;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBankInfoOutputDto;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Bnk support, implementation relying on TDUMT-cli application.
 */
// TODO handle tdumt-cli command failures
public class GenuineBnkGateway implements BankSupport {

    private static final String PATH_SEPARATOR_REGEX = "\\\\";

    private static final String ORIGINAL_BANK_NAME = "originalBank.bnk";

    private static final String CLI_COMMAND_BANK_INFO = "BANK-I";
    private static final String CLI_COMMAND_BANK_UNPACK = "BANK-U";
    private static final String CLI_COMMAND_BANK_REPLACE = "BANK-R";

    @Override
    public BankInfoDto getBankInfo(String bankFileName) throws IOException {

        String jsonOutput = runCliCommand(CLI_COMMAND_BANK_INFO, bankFileName).getOut();
//        String errorOutput = IOUtils.toString(bankInfoProcess.getErrorStream());

        GenuineBankInfoOutputDto outputObject = new ObjectMapper().readValue(jsonOutput, GenuineBankInfoOutputDto.class);

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

    @Override
    public void packAll(String inputDirectory, String outputBankFileName) throws IOException {

        Path originalBankFilePath = Paths.get(inputDirectory, ORIGINAL_BANK_NAME);

        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        Path inputPath = Paths.get(inputDirectory);

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(inputPath)) {
            for (Path packedFile : paths) {
                if (Files.isDirectory(packedFile)) {
                    continue;
                }

                String packedFilePath = getInternalPackedFilePath(packedFile, inputPath);

                runCliCommand(CLI_COMMAND_BANK_REPLACE, outputBankFileName, packedFilePath, packedFile.toString());
            }
        }
    }

    static ProcessResult runCliCommand(String command, String... args) throws IOException {
        requireNonNull(command, "A CLI command is required.");

        List<String> processCommands = new ArrayList<>();
        processCommands.add(command);
        processCommands.addAll(asList(args));

        ProcessBuilder builder = new ProcessBuilder(processCommands);

        try {
            return new ProcessResult(
                    builder.start().waitFor(),
                    IOUtils.toString(builder.start().getInputStream()),
                    IOUtils.toString(builder.start().getErrorStream()));
        } catch (InterruptedException ie) {
            throw new IOException("Process was interrupted: " + command, ie);
        }
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

        return "\\D:\\Eden-Prog\\Games\\TestDrive\\Resources\\" + Joiner.on('\\').join(pathElements);
    }

    static String getTargetFileNameFromPathCompounds(String bankFileName, String[] filePathCompounds) {
        // Format: '\D:\Eden-Prog\Games\....'
        String[] pathElements = new String[filePathCompounds.length-7];

        System.arraycopy(filePathCompounds, 6, pathElements, 0, pathElements.length);
        pathElements[pathElements.length - 1] = getFileNameFromPathCompounds(filePathCompounds);

        return Paths.get(bankFileName, pathElements).toString();
    }

    static String getFileNameFromPathCompounds(String[] filePathCompounds) {
        // Format: '\\D:\Eden-Prog\Games\....'
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

    private static void extractPackedFileWithFullPath(String bankFileName, String filePath, String outputDirectory) throws IOException {
        runCliCommand(CLI_COMMAND_BANK_UNPACK, bankFileName, filePath, outputDirectory);

        String[] filePathCompounds = filePath.split(PATH_SEPARATOR_REGEX);

        File bank = new File(bankFileName);
        File extractedFile = new File(outputDirectory, getFileNameFromPathCompounds(filePathCompounds));
        File targetFile = new File(outputDirectory, getTargetFileNameFromPathCompounds(bank.getName(), filePathCompounds));

        Files.move(extractedFile.toPath(), targetFile.toPath());
    }
}
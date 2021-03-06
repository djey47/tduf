package fr.tduf.libunlimited.high.files.banks.interop;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBankInfoOutputDto;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBatchInputDto;
import fr.tduf.libunlimited.high.files.common.interop.GenuineGateway;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.common.interop.GenuineGateway.CommandLineOperation.BANK_INFO;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Bnk support, implementation relying on TDUMT-cli application.
 */
public class GenuineBnkGateway extends GenuineGateway implements BankSupport {
    private static final String THIS_CLASS_NAME = GenuineBnkGateway.class.getSimpleName();

    public static final String EXTENSION_BANKS = "bnk";
    public static final String PREFIX_ORIGINAL_BANK_FILE = "original-";

    private static final String SEPARATOR_PACKED_PATH = "\\";
    private static final String PREFIX_PACKED_FILE_PATH = join(SEPARATOR_PACKED_PATH, "D:", "Eden-Prog", "Games", "TestDrive", "Resources", "");

    /**
     * Main Constructor
     * @param commandLineHelper     : mandatory command-line helper.
     */
    public GenuineBnkGateway(CommandLineHelper commandLineHelper) {
        super(commandLineHelper);
    }

    /**
     * tdumt-cli syntax: BANK-I <bankFileName>
     */
    @Override
    public BankInfoDto getBankInfo(String bankFileName) throws IOException {
        String result = callCommandLineInterface(BANK_INFO, bankFileName);

        GenuineBankInfoOutputDto outputObject = new ObjectMapper().readValue(result, GenuineBankInfoOutputDto.class);
        return mapGenuineBankInfoToBankInfoObject(outputObject);
    }

    /**
     * tdumt-cli syntax: BANK-UX <bankFileName> <batchInputPath>
     */
    @Override
    public void extractAll(String bankFileName, String outputDirectory) throws IOException {
        Log.debug(THIS_CLASS_NAME, "bankFileName: " + bankFileName);
        Log.debug(THIS_CLASS_NAME, "outputDirectory: " + outputDirectory);

        Path bankFilePath = Paths.get(bankFileName);
        Files.copy(bankFilePath, Paths.get(outputDirectory, PREFIX_ORIGINAL_BANK_FILE + bankFilePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);

        GenuineBatchInputDto batchInputObject = createBatchInput(bankFileName, outputDirectory);
        batchExtractPackedFilesWithFullPath(bankFileName, batchInputObject);
    }

    /**
     * tdumt-cli syntax: BANK-RX <bankFileName> <batchInputPath>
     */
    @Override
    public void packAll(String inputDirectory, String outputBankFileName) throws IOException {
        Log.debug(THIS_CLASS_NAME, "inputDirectory: " + inputDirectory);
        Log.debug(THIS_CLASS_NAME, "outputBankFileName: " + outputBankFileName);

        Path originalBankFilePath = searchOriginalBankPath(inputDirectory);
        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        Log.debug(THIS_CLASS_NAME, "originalBankFilePath: " + originalBankFilePath);

        GenuineBatchInputDto batchInputObject = createBatchInput(originalBankFilePath.toString(), inputDirectory);
        batchRepackFilesWithFullPath(outputBankFileName, batchInputObject);
    }

    static Path searchOriginalBankPath(String inputDirectory) throws IOException {
        try (Stream<Path> stream =  Files.walk(Paths.get(inputDirectory))) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> EXTENSION_BANKS.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                    .findAny()
                    .orElseThrow(() -> new IOException("No original bank has been found in this directory: " + inputDirectory));
        }
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

        return PREFIX_PACKED_FILE_PATH + join(SEPARATOR_PACKED_PATH, pathElements);
    }

    static Path getRealFilePathFromInternalPath(String internalPath, Path basePath) {

        Path filePath = Paths.get(internalPath
                .replace(PREFIX_PACKED_FILE_PATH, "")
                .replace(SEPARATOR_PACKED_PATH, File.separator));

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
            return Long.toString(hash);
        } else {
            return Long.toString(Integer.MAX_VALUE + Math.abs(hash));
        }
    }

    private GenuineBatchInputDto createBatchInput(String bankFileName, String externalDirectory) throws IOException {
        Set<GenuineBatchInputDto.Item> batchItems = getBankInfo(bankFileName).getPackedFiles().stream()

                .map(PackedFileInfoDto::getFullName)

                .map(packedPath -> {
                    String externalFile = getRealFilePathFromInternalPath(packedPath, Paths.get(externalDirectory)).toString();

                    return GenuineBatchInputDto.Item.builder()
                            .forPackedPath(packedPath)
                            .withExternalFileName(externalFile)
                            .build();
                })

                .collect(toSet());

        return GenuineBatchInputDto.builder()
                .addItems(batchItems)
                .build();
    }

    private void batchExtractPackedFilesWithFullPath(String bankFile, GenuineBatchInputDto batchInputObject) throws IOException {
        try {
            Log.debug(THIS_CLASS_NAME, "bankFile: " + bankFile);
            Log.debug(THIS_CLASS_NAME, "batchInputObject: " + batchInputObject);

            createExtractTargetDirectories(batchInputObject);

            String batchInputFileName = createBatchInputFile(batchInputObject);

            callCommandLineInterface(CommandLineOperation.BANK_BATCH_UNPACK, bankFile, batchInputFileName);
        } catch (IOException ioe) {
            throw new IOException(String.format("Error while extracting from file: %s", bankFile), ioe);
        }
    }

    private void batchRepackFilesWithFullPath(String outputBankFile, GenuineBatchInputDto batchInputObject) throws IOException {
        try {
            Log.debug(THIS_CLASS_NAME, "outputBankFile: " + outputBankFile);
            Log.debug(THIS_CLASS_NAME, "batchInputObject: " + batchInputObject);

            String batchInputFileName = createBatchInputFile(batchInputObject);

            callCommandLineInterface(CommandLineOperation.BANK_BATCH_REPLACE, outputBankFile, batchInputFileName);
        } catch (IOException ioe) {
            throw new IOException(String.format("Error while repacking to file: %s", outputBankFile), ioe);
        }
    }

    private static BankInfoDto mapGenuineBankInfoToBankInfoObject(GenuineBankInfoOutputDto outputObject) {
        List<PackedFileInfoDto> packedFilesInfos = outputObject.getPackedFiles().stream()

                .map(packedFileInfo -> PackedFileInfoDto.builder()
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

    private static void createExtractTargetDirectories(GenuineBatchInputDto batchInputObject) {
        batchInputObject.getItems()
                .forEach(item -> {
                    Path parentDirectory = Paths.get(".");
                    try {
                        parentDirectory = Paths.get(item.getExternalFile()).getParent();
                        Files.createDirectories(parentDirectory);
                    } catch (IOException ioe) {
                        Log.error(ExceptionUtils.getStackTrace(ioe));
                        throw new RuntimeException(String.format("Unable to create directory to extract to: %s", parentDirectory.toAbsolutePath()), ioe);
                    }
                });
    }

    private static String createBatchInputFile(GenuineBatchInputDto batchInputObject) throws IOException {
        File batchInputFile = Files.createTempDirectory("libUnlimited-banks").resolve("BatchInput.json").toFile();
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(batchInputFile, batchInputObject);

        String batchInputFileName = batchInputFile.getAbsolutePath();
        Log.debug(THIS_CLASS_NAME, "batchInputFileName: " + batchInputFileName);
        return batchInputFileName;
    }
}

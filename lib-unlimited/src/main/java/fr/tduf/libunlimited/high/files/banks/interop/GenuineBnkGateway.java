package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import net.sf.jni4net.Bridge;
import tdumoddinglibrary.fileformats.TduFile;
import tdumoddinglibrary.fileformats.banks.BNK;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Bnk support, implementation relying on TDUMT .net assemblies.
 */
public class GenuineBnkGateway implements BankSupport {

    static {
        try {
            Bridge.init();
            // TODO Generate Assemblies with CSC on Windows platform
            Bridge.LoadAndRegisterAssemblyFrom(new File("libs/TduModdingLibrary.j4n.dll"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String ORIGINAL_BANK_NAME = "originalBank.bnk";

    @Override
    public BankInfoDto getBankInfo(String bankFileName) {

        BNK bankFile = (BNK) TduFile.GetFile(bankFileName);

        Collection<String> packedFilesNames = (Collection<String>) bankFile.GetPackedFilesPaths(null);

        List<PackedFileInfoDto> packedFilesInfos = packedFilesNames.stream()

                .map((fileName) -> PackedFileInfoDto.builder()
                        .forReference(generatePackedFileReference(fileName))
                        .withSize((int) bankFile.GetPackedFileSize(fileName))
                        .withFullName(fileName)
                        .build())

                .collect(toList());

        return BankInfoDto.builder()
                .fromYear(bankFile.getYear())
                .withFileSize(bankFile.getSize())
                .addPackedFiles(packedFilesInfos)
                .build();
    }

    @Override
    public void extractAll(String bankFileName, String outputDirectory) throws IOException {

        Files.copy(Paths.get(bankFileName), Paths.get(outputDirectory, ORIGINAL_BANK_NAME), StandardCopyOption.REPLACE_EXISTING);

        BNK bankFile = (BNK) TduFile.GetFile(bankFileName);

        Collection<String> packedFilesNames = (Collection<String>) bankFile.GetPackedFilesPaths(null);

        packedFilesNames.stream()

                .forEach((filePath) -> extractPackedFileWithFullPath(bankFile, filePath, outputDirectory));

    }

    @Override
    public void packAll(String inputDirectory, String outputBankFileName) throws IOException {

        Path originalBankFilePath = Paths.get(inputDirectory, ORIGINAL_BANK_NAME);

        Files.copy(originalBankFilePath, Paths.get(outputBankFileName), StandardCopyOption.REPLACE_EXISTING);

        BNK outputBankFile = (BNK) TduFile.GetFile(outputBankFileName);



        // TODO for each packed file in directory / or only modified files since extract date (date info should be kept in a json file)
        String packedFilePath = "";
        String repackedFileName = "";
        outputBankFile.ReplacePackedFile(packedFilePath, repackedFileName);
    }

    private static void extractPackedFileWithFullPath(BNK bankFile, String filePath, String outputDirectory) {
        bankFile.ExtractPackedFile(filePath, outputDirectory, true);

        // TODO check file paths
        File extractedFile = new File(outputDirectory, getFileNameFromPath(filePath));
        File targetFile = new File(outputDirectory, filePath);
        try {
            Files.move(extractedFile.toPath(), targetFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameFromPath(String filePath) {
        // TODO
        return null;
    }

    private static String generatePackedFileReference(String fileName) {
        return Integer.valueOf(fileName.hashCode()).toString();
    }
}
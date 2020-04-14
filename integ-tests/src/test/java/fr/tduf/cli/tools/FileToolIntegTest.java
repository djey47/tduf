package fr.tduf.cli.tools;

import fr.tduf.libtesting.common.helper.AssertionsHelper;
import fr.tduf.libtesting.common.helper.ConsoleHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.tduf.tests.IntegTestsConstants.RESOURCES_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class FileToolIntegTest {

    private final String sourceFileNameToBeEncrypted = "TDU_CarColors.json";

    private final String encryptedFileName = "TDU_CarColors.json.enc";
    private final String sourceDirectoryForEncryption = RESOURCES_PATH.resolve("crypto").toString();
    private final String decryptDirectory = RESOURCES_PATH.resolve("unencrypted").toString();
    private final String encryptDirectory = RESOURCES_PATH.resolve("encrypted").toString();
    private final String jsonifyDirectory = RESOURCES_PATH.resolve( "jsonified").toString();
    private final String applyjsonDirectory = RESOURCES_PATH.resolve("applied").toString();

    private final String bankFileName =  "Bank.bnk";
    private final String bankDirectory = RESOURCES_PATH.resolve("banks").toString();
    private final String unpackedDirectory = RESOURCES_PATH.resolve("unpacked/").toString();
    private final String repackedDirectory = RESOURCES_PATH.resolve("repacked").toString();

    @Mock
    private BankSupport bankSupportMock;

    @InjectMocks
    private FileTool fileTool;  // Used for bank testing only. Do not use twice in a same test method!

    @BeforeEach
    void setUp() throws IOException {
        initMocks(this);
        
        FileUtils.deleteDirectory(new File(encryptDirectory));
        FilesHelper.createDirectoryIfNotExists(encryptDirectory);

        FileUtils.deleteDirectory(new File(decryptDirectory));
        FilesHelper.createDirectoryIfNotExists(decryptDirectory);

        FileUtils.deleteDirectory(new File(jsonifyDirectory));
        FilesHelper.createDirectoryIfNotExists(jsonifyDirectory);

        FileUtils.deleteDirectory(new File(applyjsonDirectory));
        FilesHelper.createDirectoryIfNotExists(applyjsonDirectory);

        FileUtils.deleteDirectory(new File(repackedDirectory));
        FileUtils.deleteDirectory(new File(unpackedDirectory));
    }

    @AfterEach
    void tearDown() {
        ConsoleHelper.restoreOutput();
    }

    @Test
    void encryptDecrypt_whenSavegameMode_shouldGiveOriginalFileBack() throws IOException {
        // GIVEN
        String inputFile = Paths.get(sourceDirectoryForEncryption, sourceFileNameToBeEncrypted).toString();
        String outputFile = Paths.get(encryptDirectory, encryptedFileName).toString();

        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[]{"encrypt", "-n", "-i", inputFile, "-o", outputFile, "-c", "0"});

        // THEN: file should exist
        assertThat(new File(outputFile)).exists();


        // GIVEN
        inputFile = outputFile;
        outputFile = Paths.get(decryptDirectory, sourceFileNameToBeEncrypted).toString();

        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-n", "-i", inputFile, "-o", outputFile, "-c", "0"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(outputFile);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasSameTextualContentAs(new File(sourceDirectoryForEncryption, sourceFileNameToBeEncrypted));
    }

    @Test
    void encryptDecrypt_whenDatabaseMode_shouldGiveOriginalFileBack() throws IOException {
        // GIVEN
        String inputFile = Paths.get(sourceDirectoryForEncryption, sourceFileNameToBeEncrypted).toString();
        String outputFile = Paths.get(encryptDirectory, encryptedFileName).toString();

        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[]{"encrypt", "-n", "-i", inputFile, "-o", outputFile, "-c", "1"});

        // THEN: file should exist
        assertThat(new File(outputFile)).exists();


        // GIVEN
        inputFile = outputFile;
        outputFile = Paths.get(decryptDirectory, sourceFileNameToBeEncrypted).toString();

        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-n", "-i", inputFile, "-o", outputFile, "-c", "1"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(outputFile);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasSameTextualContentAs(new File(sourceDirectoryForEncryption, sourceFileNameToBeEncrypted));
    }

    @Test
    void jsonifyApplyJson_whenEncryptedContents_shouldGiveOriginalContentsBack() throws IOException {
        String researchDirectory = RESOURCES_PATH.resolve("research").toString();
        String structureFileName = Paths.get(researchDirectory, "BTRQ-map.json").toString();

        // GIVEN
        String sourceFileNameToBeJsonified = "Brutal.btrq";
        String inputFile = Paths.get(researchDirectory, sourceFileNameToBeJsonified).toString();
        String jsonifiedFileName = "Brutal.json";
        String outputFile = Paths.get(jsonifyDirectory, jsonifiedFileName).toString();

        // WHEN: jsonify
        System.out.println("-> Jsonify!");
        FileTool.main(new String[]{"jsonify", "-n", "-i", inputFile, "-o", outputFile, "-s", structureFileName});

        // THEN: file should exist
        assertThat(new File(outputFile)).exists();


        // GIVEN
        inputFile = outputFile;
        outputFile = Paths.get(applyjsonDirectory, sourceFileNameToBeJsonified).toString();

        // WHEN: applyJson
        System.out.println("-> Applyjson!");
        FileTool.main(new String[]{"applyjson", "-n", "-i", inputFile, "-o", outputFile, "-s", structureFileName});

        // THEN: file should exist
        assertThat(new File(outputFile)).exists();


        // GIVEN
        String inputFile1 = outputFile;
        String inputFile2 = Paths.get(researchDirectory, sourceFileNameToBeJsonified).toString();
        String outputFile1 = Paths.get(applyjsonDirectory, "Brutal.btrq.dec").toString();
        String outputFile2 = Paths.get(applyjsonDirectory, "Brutal.btrq.ref.dec").toString();

        // WHEN: decrypt both files
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-n", "-i", inputFile1, "-o", outputFile1, "-c", "1"});
        FileTool.main(new String[]{"decrypt", "-n", "-i", inputFile2, "-o", outputFile2, "-c", "1"});

        // THEN: file should exist and match reference one, once unencrypted
        File actualFile = new File(outputFile1);
        File expectedFile = new File(outputFile2);

        assertThat(actualFile).exists();
        assertThat(expectedFile).exists();
        AssertionsHelper.assertFileMatchesReference(actualFile, expectedFile);
    }

    @Test
    void bankInfo_shouldReturnInformation() throws IOException, JSONException {
        String bankFile = Paths.get(bankDirectory, "Empty.bnk").toString();

        // GIVEN
        PackedFileInfoDto packedFileInfoObject = PackedFileInfoDto.builder()
                .forReference("10011001")
                .withFullName("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.2DM\\CLK_55")
                .withShortName("CLK_55.2DM")
                .withSize(1005)
                .withTypeDescription("Materials")
                .build();
        BankInfoDto bankInfoObject = BankInfoDto.builder()
                .fromYear(2014)
                .addPackedFile(packedFileInfoObject)
                .build();
        when(bankSupportMock.getBankInfo(bankFile)).thenReturn(bankInfoObject);

        // WHEN
        System.out.println("-> Bankinfo!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        fileTool.doMain(new String[]{"bankinfo", "-n", "-i", bankFile});

        // THEN
        byte[] jsonContents = Files.readAllBytes(Paths.get(bankDirectory, "json", "bankInfo.out.json"));
        String expectedJson = new String(jsonContents, FilesHelper.CHARSET_DEFAULT);
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outputStream, expectedJson);
    }

    @Test
    void unpack_shouldCallGateway() throws IOException {
        String bankFile = Paths.get(bankDirectory, bankFileName).toString();

        // WHEN
        System.out.println("-> Unpack!");
        fileTool.doMain(new String[]{"unpack", "-n", "-i", bankFile, "-o", unpackedDirectory});

        // THEN
        verify(bankSupportMock).extractAll(bankFile, unpackedDirectory);
    }

    @Test
    void repack_shouldCallGateway() throws IOException {
        String outputBankFile = Paths.get(repackedDirectory, bankFileName).toString();

        // WHEN
        System.out.println("-> Repack!");
        fileTool.doMain(new String[]{"repack", "-n", "-i", unpackedDirectory, "-o", outputBankFile});

        // THEN
        verify(bankSupportMock).packAll(unpackedDirectory, outputBankFile);
    }

    @Test
    void repack_whenOutputFileNotProvided_andInputEndsWithSeparator_shouldGenerateRightFileName() throws IOException {
        String expectedOutputBankFile = RESOURCES_PATH.resolve("unpacked-repacked.bnk").toString();

        // WHEN
        System.out.println("-> Repack!");
        fileTool.doMain(new String[]{"repack", "-n", "-i", unpackedDirectory + File.separator});

        // THEN
        verify(bankSupportMock).packAll(unpackedDirectory, expectedOutputBankFile);
    }
}

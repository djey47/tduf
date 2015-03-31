package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.AssertionsHelper;
import fr.tduf.cli.common.helper.ConsoleHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileToolIntegTest {

    private final String testRootDirectory = "integ-tests/";

    private final String sourceFileNameToBeEncrypted = "TDU_CarColors.json";
    private final String sourceFileNameToBeJsonified = "Brutal.btrq";
    private final String jsonifiedFileName = "Brutal.json";
    private final String appliedUnencryptedFileName = "Brutal.btrq.dec";
    private final String sourceUnencryptedFileName = "Brutal.btrq.ref.dec";

    private final String encryptedFileName = "TDU_CarColors.json.enc";
    private final String sourceDirectoryForEncryption = testRootDirectory + "crypto";
    private final String decryptDirectory = testRootDirectory + "unencrypted";
    private final String encryptDirectory = testRootDirectory + "encrypted";
    private final String jsonifyDirectory = testRootDirectory + "jsonified";
    private final String applyjsonDirectory = testRootDirectory + "applied";

    private final String bankDirectory = testRootDirectory + "banks";
    private final String unpackedDirectory = testRootDirectory + "unpacked";
    private final String repackedDirectory = testRootDirectory + "repacked";
    private final String bankFileName =  "Bank.bnk";

    @Mock
    private BankSupport bankSupportMock;

    @InjectMocks
    private FileTool fileTool;  // Used for bank testing only. Do not use twice in a same test method!

    @Before
    public void setUp() throws IOException {
        Files.createDirectories(Paths.get(encryptDirectory));
        Files.createDirectories(Paths.get(decryptDirectory));

        Files.createDirectories(Paths.get(jsonifyDirectory));
        Files.createDirectories(Paths.get(applyjsonDirectory));

        Files.createDirectories(Paths.get(unpackedDirectory));
        Files.createDirectories(Paths.get(repackedDirectory));

        Files.deleteIfExists(Paths.get(encryptDirectory, encryptedFileName));
        Files.deleteIfExists(Paths.get(decryptDirectory, sourceFileNameToBeEncrypted));

        Files.deleteIfExists(Paths.get(jsonifyDirectory, jsonifiedFileName));
        Files.deleteIfExists(Paths.get(applyjsonDirectory, sourceFileNameToBeJsonified));
        Files.deleteIfExists(Paths.get(applyjsonDirectory, appliedUnencryptedFileName));
        Files.deleteIfExists(Paths.get(applyjsonDirectory, sourceUnencryptedFileName));

        Files.deleteIfExists(Paths.get(repackedDirectory, bankFileName));
    }

    @Test
    public void encryptDecrypt_whenSavegameMode_shouldGiveOriginalFileBack() throws IOException {
        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[]{"encrypt", "-i", sourceDirectoryForEncryption + "/" + sourceFileNameToBeEncrypted, "-o", encryptDirectory + "/" + encryptedFileName, "-c", "0"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-i", encryptDirectory + "/" + encryptedFileName, "-o", decryptDirectory + "/" + sourceFileNameToBeEncrypted, "-c", "0"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(decryptDirectory, sourceFileNameToBeEncrypted);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasContentEqualTo(new File(sourceDirectoryForEncryption, sourceFileNameToBeEncrypted));
    }

    @Test
    public void encryptDecrypt_whenDatabaseMode_shouldGiveOriginalFileBack() throws IOException {
        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[]{"encrypt", "-i", sourceDirectoryForEncryption + "/" + sourceFileNameToBeEncrypted, "-o", encryptDirectory + "/" + encryptedFileName, "-c", "1"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-i", encryptDirectory + "/" + encryptedFileName, "-o", decryptDirectory + "/" + sourceFileNameToBeEncrypted, "-c", "1"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(decryptDirectory, sourceFileNameToBeEncrypted);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasContentEqualTo(new File (sourceDirectoryForEncryption, sourceFileNameToBeEncrypted));
    }

    @Test
    public void jsonifyApplyJson_whenEncryptedContents_shouldGiveOriginalContentsBack() throws IOException {
        String researchDirectory = testRootDirectory + "research";
        String structureFileName = researchDirectory + "/" + "BTRQ-map.json";

        // WHEN: jsonify
        System.out.println("-> Jsonify!");
        FileTool.main(new String[]{"jsonify", "-i", researchDirectory + "/" + sourceFileNameToBeJsonified, "-o", jsonifyDirectory + "/" + jsonifiedFileName, "-s", structureFileName});

        // THEN: file should exist
        assertThat(new File(jsonifyDirectory, jsonifiedFileName)).exists();


        // WHEN: applyJson
        System.out.println("-> Applyjson!");
        FileTool.main(new String[]{"applyjson", "-i", jsonifyDirectory + "/" + jsonifiedFileName, "-o", applyjsonDirectory + "/" + sourceFileNameToBeJsonified, "-s", structureFileName});

        // THEN: file should exist
        assertThat(new File(applyjsonDirectory, sourceFileNameToBeJsonified)).exists();


        // WHEN: decrypt both files
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-i", applyjsonDirectory + "/" + sourceFileNameToBeJsonified, "-o", applyjsonDirectory + "/" + appliedUnencryptedFileName, "-c", "1"});
        FileTool.main(new String[]{"decrypt", "-i", researchDirectory + "/" + sourceFileNameToBeJsonified, "-o", applyjsonDirectory + "/" + sourceUnencryptedFileName, "-c", "1"});

        // THEN: file should exist and match reference one, once unencrypted
        File actualFile = new File(applyjsonDirectory, appliedUnencryptedFileName);
        File expectedFile = new File(applyjsonDirectory, sourceUnencryptedFileName);

        assertThat(actualFile).exists();
        assertThat(expectedFile).exists();
        assertThat(actualFile).hasContentEqualTo(expectedFile);
    }

    @Test
    public void bankInfo_shouldReturnInformation() throws IOException {
        String bankFile = bankDirectory + "/Empty.bnk";

        // GIVEN
        BankInfoDto bankInfoObject = BankInfoDto.builder()
                .fromYear(2014)
                .build();
        when(bankSupportMock.getBankInfo(bankFile)).thenReturn(bankInfoObject);

        // WHEN
        System.out.println("-> Bankinfo!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        fileTool.doMain(new String[]{"bankinfo", "-n", "-i", bankFile});

        // THEN
        String expectedJson =
                "{\n" +
                "  \"bankFile\" : \"integ-tests/banks/Empty.bnk\",\n" +
                "  \"bankInfo\" : {\n" +
                "    \"year\" : 2014,\n" +
                "    \"fileSize\" : 0,\n" +
                "    \"packedFiles\" : [ ]\n" +
                "  }\n" +
                "}";
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outputStream, expectedJson);
    }

    @Test
    public void unpack_shouldCallGateway() throws IOException {
        String bankFile = bankDirectory + "/" + bankFileName;

        // WHEN
        System.out.println("-> Unpack!");
        fileTool.doMain(new String[]{"unpack", "-i", bankFile, "-o", unpackedDirectory});

        // THEN
        verify(bankSupportMock).extractAll(bankFile, unpackedDirectory);
    }

    @Test
    public void repack_shouldCallGateway() throws IOException {
        String outputBankFile = repackedDirectory + "/" + bankFileName;

        // WHEN
        System.out.println("-> Repack!");
        fileTool.doMain(new String[]{"repack", "-i", unpackedDirectory, "-o", outputBankFile});

        // THEN
        verify(bankSupportMock).packAll(unpackedDirectory, outputBankFile);
    }
}
package fr.tduf.cli.tools;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Before
    public void setUp() {
        new File(encryptDirectory).mkdirs();
        new File(decryptDirectory).mkdirs();

        new File(jsonifyDirectory).mkdirs();
        new File(applyjsonDirectory).mkdirs();

        new File(unpackedDirectory).mkdirs();
        new File(repackedDirectory).mkdirs();

        new File(encryptDirectory, encryptedFileName).delete();
        new File(decryptDirectory, sourceFileNameToBeEncrypted).delete();

        new File(jsonifyDirectory, jsonifiedFileName).delete();
        new File(applyjsonDirectory, sourceFileNameToBeJsonified).delete();
        new File(applyjsonDirectory, appliedUnencryptedFileName).delete();
        new File(applyjsonDirectory, sourceUnencryptedFileName).delete();

        new File(repackedDirectory, bankFileName).delete();
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
        FileTool.main(new String[] { "encrypt", "-i", sourceDirectoryForEncryption + "/" + sourceFileNameToBeEncrypted, "-o", encryptDirectory + "/" + encryptedFileName, "-c", "1"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[] { "decrypt", "-i", encryptDirectory + "/" + encryptedFileName, "-o", decryptDirectory + "/" + sourceFileNameToBeEncrypted, "-c", "1"});

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
        FileTool.main(new String[] { "jsonify", "-i", researchDirectory + "/" + sourceFileNameToBeJsonified, "-o", jsonifyDirectory + "/" + jsonifiedFileName, "-s", structureFileName});

        // THEN: file should exist
        assertThat(new File(jsonifyDirectory, jsonifiedFileName)).exists();


        // WHEN: applyJson
        System.out.println("-> Applyjson!");
        FileTool.main(new String[] { "applyjson", "-i", jsonifyDirectory + "/" + jsonifiedFileName, "-o", applyjsonDirectory + "/" + sourceFileNameToBeJsonified, "-s", structureFileName});

        // THEN: file should exist
        assertThat(new File(applyjsonDirectory, sourceFileNameToBeJsonified)).exists();


        // WHEN: decrypt both files
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-i", applyjsonDirectory + "/" + sourceFileNameToBeJsonified, "-o", applyjsonDirectory + "/" + appliedUnencryptedFileName, "-c", "1"});
        FileTool.main(new String[] { "decrypt", "-i", researchDirectory + "/" + sourceFileNameToBeJsonified, "-o", applyjsonDirectory + "/" + sourceUnencryptedFileName, "-c", "1"});

        // THEN: file should exist and match reference one, once unencrypted
        File actualFile = new File(applyjsonDirectory, appliedUnencryptedFileName);
        File expectedFile = new File(applyjsonDirectory, sourceUnencryptedFileName);

        assertThat(actualFile).exists();
        assertThat(expectedFile).exists();
        assertThat(actualFile).hasContentEqualTo(expectedFile);
    }

    @Test
    @Ignore
    public void bankInfo_shouldReturnInformation() throws IOException {
        String emptyBankFileName = "Empty.bnk";

        // WHEN
        System.out.println("-> Bankinfo!");
        FileTool.main(new String[] { "bankinfo", "-i", bankDirectory + "/" + emptyBankFileName});

        // THEN: no exception
    }

    @Test
    @Ignore
    public void unpackRepackBankInfo_shouldReturnInformation() throws IOException {
        // WHEN
        System.out.println("-> Unpack!");
        FileTool.main(new String[] { "unpack", "-i", bankDirectory + "/" + bankFileName, "-o", unpackedDirectory });

        // WHEN
        System.out.println("-> Repack!");
        FileTool.main(new String[]{"repack", "-i", unpackedDirectory, "-o", repackedDirectory + "/" + bankFileName});

        // WHEN
        System.out.println("-> Bankinfo!");
        FileTool.main(new String[] { "bankinfo", "-i", repackedDirectory + "/" + bankFileName });

        // THEN: no exception
    }
}
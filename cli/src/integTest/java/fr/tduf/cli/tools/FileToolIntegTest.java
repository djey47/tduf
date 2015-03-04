package fr.tduf.cli.tools;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FileToolIntegTest {

    private final String sourceFileNameToBeEncrypted = "TDU_CarColors.json";
    private final String sourceFileNameToBeJsonified = "Brutal.btrq";
    private final String jsonifiedFileName = "Brutal.json";
    private final String appliedUnencryptedFileName = "Brutal.btrq.dec";
    private final String sourceUnencryptedFileName = "Brutal.btrq.ref.dec";

    private final String encryptedFileName = "TDU_CarColors.json.enc";
    private final String sourceDirectoryForEncryption = "integ-tests/crypto";
    private final String decryptDirectory = "integ-tests/unencrypted";
    private final String encryptDirectory = "integ-tests/encrypted";
    private final String jsonifyDirectory = "integ-tests/jsonified";
    private final String applyjsonDirectory = "integ-tests/applied";


    @Before
    public void setUp() {
        new File(encryptDirectory).mkdirs();
        new File(decryptDirectory).mkdirs();

        new File(jsonifyDirectory).mkdirs();
        new File(applyjsonDirectory).mkdirs();

        new File(encryptDirectory, encryptedFileName).delete();
        new File(decryptDirectory, sourceFileNameToBeEncrypted).delete();

        new File(jsonifyDirectory, jsonifiedFileName).delete();
        new File(applyjsonDirectory, sourceFileNameToBeJsonified).delete();
        new File(applyjsonDirectory, appliedUnencryptedFileName).delete();
        new File(applyjsonDirectory, sourceUnencryptedFileName).delete();
    }

    @Test
    public void encryptDecrypt_whenSavegameMode_shouldGiveOriginalFileBack() throws IOException {
        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[] { "encrypt", "-i", "integ-tests/crypto/TDU_CarColors.json", "-o", "integ-tests/encrypted/TDU_CarColors.json.enc", "-c", "0"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[] { "decrypt", "-i", "integ-tests/encrypted/TDU_CarColors.json.enc", "-o", "integ-tests/unencrypted/TDU_CarColors.json", "-c", "0"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(decryptDirectory, sourceFileNameToBeEncrypted);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasContentEqualTo(new File (sourceDirectoryForEncryption, sourceFileNameToBeEncrypted));
    }

    @Test
    public void encryptDecrypt_whenDatabaseMode_shouldGiveOriginalFileBack() throws IOException {
        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[] { "encrypt", "-i", "integ-tests/crypto/TDU_CarColors.json", "-o", "integ-tests/encrypted/TDU_CarColors.json.enc", "-c", "1"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Decrypt!");
        FileTool.main(new String[] { "decrypt", "-i", "integ-tests/encrypted/TDU_CarColors.json.enc", "-o", "integ-tests/unencrypted/TDU_CarColors.json", "-c", "1"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(decryptDirectory, sourceFileNameToBeEncrypted);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasContentEqualTo(new File (sourceDirectoryForEncryption, sourceFileNameToBeEncrypted));
    }

    @Test
    public void jsonifyApplyJson_whenEncryptedContents_shouldGiveOriginalContentsBack() throws IOException {
        // WHEN: jsonify
        System.out.println("-> Jsonify!");
        FileTool.main(new String[] { "jsonify", "-i", "integ-tests/research/Brutal.btrq", "-o", "integ-tests/jsonified/Brutal.json", "-s", "integ-tests/research/BTRQ-map.json" });

        // THEN: file should exist
        assertThat(new File(jsonifyDirectory, jsonifiedFileName)).exists();


        // WHEN: applyJson
        System.out.println("-> Applyjson!");
        FileTool.main(new String[] { "applyjson", "-i", "integ-tests/jsonified/Brutal.json", "-o", "integ-tests/applied/Brutal.btrq", "-s", "integ-tests/research/BTRQ-map.json" });

        // THEN: file should exist
        assertThat(new File(applyjsonDirectory, sourceFileNameToBeJsonified)).exists();


        // WHEN: decrypt both files
        System.out.println("-> Decrypt!");
        FileTool.main(new String[]{"decrypt", "-i", "integ-tests/applied/Brutal.btrq", "-o", "integ-tests/applied/Brutal.btrq.dec", "-c", "1"});
        FileTool.main(new String[] { "decrypt", "-i", "integ-tests/research/Brutal.btrq", "-o", "integ-tests/applied/Brutal.btrq.ref.dec", "-c", "1"});

        // THEN: file should exist and match reference one, once unencrypted
        File actualFile = new File(applyjsonDirectory, appliedUnencryptedFileName);
        File expectedFile = new File(applyjsonDirectory, sourceUnencryptedFileName);

        assertThat(actualFile).exists();
        assertThat(expectedFile).exists();
        assertThat(actualFile).hasContentEqualTo(expectedFile);
    }

    @Test
    public void bankInfo_shouldReturnInformation() throws IOException {
        // WHEN
        System.out.println("-> Bankinfo!");
        FileTool.main(new String[] { "bankinfo", "-i", "integ-tests/banks/Empty.bnk" });

        // THEN: no exception
    }

    //TODO add assertions when feature complete
    @Test
    public void unpackRepackBankInfo_shouldReturnInformation() throws IOException {
        String unpackedDirectory = "integ-tests/unpacked";

        // WHEN
        System.out.println("-> Unpack!");
        FileTool.main(new String[] { "unpack", "-i", "integ-tests/banks/Bank.bnk", "-o", unpackedDirectory });

        // WHEN
        System.out.println("-> Repack!");
        FileTool.main(new String[] { "repack", "-i", unpackedDirectory, "-o", "integ-tests/repacked/Bank.bnk" });

        // WHEN
        System.out.println("-> Bankinfo!");
        FileTool.main(new String[] { "bankinfo", "-i", "integ-tests/repacked/Bank.bnk" });

        // THEN: no exception
    }
}
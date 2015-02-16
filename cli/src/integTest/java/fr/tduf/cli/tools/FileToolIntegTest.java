package fr.tduf.cli.tools;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FileToolIntegTest {

    private final String sourceFileName = "TDU_CarColors.json";
    private final String encryptedFileName = "TDU_CarColors.json.enc";

    private final String sourceDirectory = "integ-tests/crypto";
    private final String decryptDirectory = "integ-tests/unencrypted";
    private final String encryptDirectory = "integ-tests/encrypted";

    @Before
    public void setUp() {
        new File(encryptDirectory).mkdirs();
        new File(decryptDirectory).mkdirs();

        new File(encryptDirectory, encryptedFileName).delete();
        new File(decryptDirectory, sourceFileName).delete();
    }

    @Test
    public void encryptDecrypt_whenSavegameMode_shouldGiveOriginalFileBack() throws IOException {
        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[] { "encrypt", "-i", "integ-tests/crypto/TDU_CarColors.json", "-o", "integ-tests/encrypted/TDU_CarColors.json.enc", "-c", "0"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[] { "decrypt", "-i", "integ-tests/encrypted/TDU_CarColors.json.enc", "-o", "integ-tests/unencrypted/TDU_CarColors.json", "-c", "0"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(decryptDirectory, sourceFileName);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasContentEqualTo(new File (sourceDirectory, sourceFileName));
    }

    @Test
    public void encryptDecrypt_whenDatabaseMode_shouldGiveOriginalFileBack() throws IOException {
        // WHEN: encrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[] { "encrypt", "-i", "integ-tests/crypto/TDU_CarColors.json", "-o", "integ-tests/encrypted/TDU_CarColors.json.enc", "-c", "1"});

        // THEN: file should exist
        assertThat(new File(encryptDirectory, encryptedFileName)).exists();


        // WHEN: decrypt
        System.out.println("-> Encrypt!");
        FileTool.main(new String[] { "decrypt", "-i", "integ-tests/encrypted/TDU_CarColors.json.enc", "-o", "integ-tests/unencrypted/TDU_CarColors.json", "-c", "1"});

        // THEN: file should exist and have same contents as original one
        File actualFile = new File(decryptDirectory, sourceFileName);
        assertThat(actualFile).exists();
        assertThat(actualFile).hasContentEqualTo(new File (sourceDirectory, sourceFileName));
    }
}
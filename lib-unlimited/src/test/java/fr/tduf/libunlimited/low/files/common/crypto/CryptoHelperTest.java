package fr.tduf.libunlimited.low.files.common.crypto;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoHelperTest {
    private final static Class thisClass = CryptoHelperTest.class;

    @Test(expected = IllegalArgumentException.class)
    public void decryptXTEA_withProvidedContents_andLengthNotMultipleOf8_shouldThrowException() throws IOException, InvalidKeyException {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[10]);

        // WHEN
        CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.SAVEGAME);

        // THEN : exception
    }

    @Test
    public void decryptXTEA_withRealFile_andSavegameMode_shouldGiveClearContentsBack() throws URISyntaxException, IOException, InvalidKeyException {
        // GIVEN
        byte[] contentBytes = getBytesFromResource("/common/crypto/savegame/encrypted.bin");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        byte[] expectedBytes = getBytesFromResource("/common/crypto/clear.txt");


        // WHEN
        ByteArrayOutputStream actualOutputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.SAVEGAME);


        // THEN
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedBytes);
    }

//    @Test
//    public void decryptXTEA_withRealFile_andOtherMode_shouldGiveClearContentsBack() throws IOException, URISyntaxException, InvalidKeyException {
//        // GIVEN
//        byte[] contentBytes = getBytesFromResource("/common/crypto/other/encrypted.bin");
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
//
//        byte[] expectedBytes = getBytesFromResource("/common/crypto/clear.txt");
//
//
//        // WHEN
//        ByteArrayOutputStream actualOutputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
//
//
//        // THEN
//        assertThat(actualOutputStream).isNotNull();
//        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedBytes);
//    }

    @Test
    public void readInt32_withAtLeast4BytesToRead_shouldReturnProperValue() {
        // GIVEN
        byte[] buffer = {
                0x00,
                0x10,
                0x20,
                0X30,
                0x40,
        };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        // WHEN
        int actualValue = CryptoHelper.readInt32(inputStream);

        // THEN
        assertThat(actualValue).isEqualTo(1056816);
    }

    @Test
    public void writeInt32_shouldSetProperValue() {
        // GIVEN
        byte[] expectedBytes = {
                0x00,
                0x10,
                0x20,
                0X30
        };
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // WHEN
        CryptoHelper.writeInt32(1056816, outputStream);

        // THEN
        assertThat(outputStream.toByteArray()).isEqualTo(expectedBytes);
    }

    private static byte[] getBytesFromResource(String resourceName) throws URISyntaxException, IOException {
        Path encryptedPath = Paths.get(thisClass.getResource(resourceName).toURI()) ;
        return Files.readAllBytes(encryptedPath);
    }
}
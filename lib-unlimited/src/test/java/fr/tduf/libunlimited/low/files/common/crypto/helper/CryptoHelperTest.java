package fr.tduf.libunlimited.low.files.common.crypto.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoHelperTest {

    @BeforeEach
    void setUp() {
        CryptoHelper.overrideTimestamp(1419670800);
    }

    @AfterEach
    void after() {
        CryptoHelper.restoreTimestamp();
    }

    @Test
    void decryptXTEA_withProvidedContents_andLengthNotMultipleOf8_shouldThrowException() {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[10]);

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.SAVEGAME));
    }

    @Test
    void decryptXTEA_withRealFile_andSavegameMode_shouldGiveClearContentsBack() throws IOException {
        // GIVEN
        byte[] contentBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/savegame/encrypted.bin");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/clear.txt");


        // WHEN
        ByteArrayOutputStream actualOutputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.SAVEGAME);


        // THEN
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedBytes);
    }

    @Test
    void decryptXTEA_withRealFile_andOtherMode_shouldGiveClearContentsBack() throws IOException {
        // GIVEN
        byte[] contentBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/other/encrypted.bin");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/clear.txt");


        // WHEN
        ByteArrayOutputStream actualOutputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);


        // THEN
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedBytes);
    }

    @Test
    void encryptXTEA_withProvidedContents_andLengthNotMultipleOf8_shouldThrowException() {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[10]);

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> CryptoHelper.encryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.SAVEGAME));
    }

    @Test
    void encryptXTEA_withRealFile_andSavegameMode_shouldGiveEncryptedContentsBack() throws IOException {
        // GIVEN
        byte[] contentBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/clear.txt");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/savegame/encrypted.bin");


        // WHEN
        ByteArrayOutputStream actualOutputStream = CryptoHelper.encryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.SAVEGAME);


        // THEN
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedBytes);
    }

    @Test
    void encryptXTEA_withRealFile_andOtherMode_shouldGiveEncryptedContentsBack() throws IOException {
        // GIVEN
        byte[] contentBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/clear.txt");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/common/crypto/other/encrypted.bin");


        // WHEN
        ByteArrayOutputStream actualOutputStream = CryptoHelper.encryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);


        // THEN
        assertThat(actualOutputStream).isNotNull();
        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(expectedBytes.length);
        // Decryption to check
        ByteArrayInputStream actualInputStream = new ByteArrayInputStream(actualBytes);
        ByteArrayOutputStream decryptedOutputStream = CryptoHelper.decryptXTEA(actualInputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
        byte[] decryptedBytes = decryptedOutputStream.toByteArray();
        assertThat(decryptedBytes).isEqualTo(contentBytes);
    }

    @Test
    void introduceTimestamp_shouldPrependContentsWith8Bytes() {
        // GIVEN
        byte[] inputBytes = new byte[] { 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16 };
        byte[] expectedBytes = new byte[] { 84, -98, 117, 16, -85, 97, -118, -17, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16 };

        // WHEN
        byte[] actualBytes = CryptoHelper.introduceTimeStamp(inputBytes);

        // THEN
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }
}
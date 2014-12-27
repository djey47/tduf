package fr.tduf.libunlimited.low.files.common.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

/**
 * Utility class to handle TDU files encryption/decryption.
 */
public class CryptoHelper {

    /**
     * Converts encrypted contents in provided input stream to clear ones.
     * @param inputStream           : contents to be decrypted - size must be multiple of 8
     * @param encryptionModeEnum    : encryption mode to be used
     * @return an output stream with clear contents.
     */
    public static ByteArrayOutputStream decryptXTEA(ByteArrayInputStream inputStream, EncryptionModeEnum encryptionModeEnum) throws InvalidKeyException, IOException {
        int contentsSize = inputStream.available();
        if (contentsSize % 8 != 0) {
            throw new IllegalArgumentException("Buffer to be decoded must have length multiple of 8. Current=" + contentsSize);
        }

        byte[] inputBytes = readBytesAndCheckSize(inputStream, contentsSize);

        XTEA.engineInit(encryptionModeEnum.key, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int position = 0;
        while(position < contentsSize) {

            if (position >= encryptionModeEnum.contentsOffset) {
                byte[] outputBytes = XTEA.engineCrypt(inputBytes, position);

                if (encryptionModeEnum == EncryptionModeEnum.OTHER_AND_SPECIAL) {
                    // XOR XTEA decipher result with input file data
                    for (int i = 0 ; i < outputBytes.length ; i++) {
                        int offset = position - encryptionModeEnum.contentsOffset;
                        outputBytes[i] ^= inputBytes[offset + i];
                    }
                }

                outputStream.write(outputBytes);
            }

            position += 8;
        }

        return outputStream;
    }

    /**
     * Converts clear contents in provided input stream to encrypted ones.
     * @param inputStream           : contents to be encrypted - size must be multiple of 8
     * @param encryptionModeEnum    : encryption mode to be used
     * @return an output stream with encrypted contents.
     */
    public static ByteArrayOutputStream encryptXTEA(ByteArrayInputStream inputStream, EncryptionModeEnum encryptionModeEnum) throws IOException, InvalidKeyException {
        int contentsSize = inputStream.available();
        if (contentsSize % 8 != 0) {
            throw new IllegalArgumentException("Buffer to be encoded must have length multiple of 8. Current=" + contentsSize);
        }

        byte[] inputBytes = readBytesAndCheckSize(inputStream, contentsSize);

        if(encryptionModeEnum == EncryptionModeEnum.OTHER_AND_SPECIAL) {
            inputBytes = introduceTimeStamp(inputBytes);
        }

        XTEA.engineInit(encryptionModeEnum.key, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int position = 0;
        while(position < contentsSize) {

            byte[] outputBytes = XTEA.engineCrypt(inputBytes, position);
            outputStream.write(outputBytes);

            position += 8;
        }

        return outputStream;
    }

    static byte[] introduceTimeStamp(byte[] inputBytes) {

        int currentTime = (int) (System.currentTimeMillis() / 1000L);

        byte[] currentTimeBytes = ByteBuffer
                .allocate(4)
                .putInt(currentTime)
                .array();
        byte[] currentTimeComplementBytes = ByteBuffer
                .allocate(4)
                .putInt(~currentTime)
                .array();

        byte[] resultBytes = new byte[inputBytes.length + 8];
        System.arraycopy(currentTimeBytes, 0, resultBytes, 0, 4);
        System.arraycopy(currentTimeComplementBytes, 0, resultBytes, 4, 4);
        System.arraycopy(inputBytes, 0, resultBytes, 8, inputBytes.length);

        return resultBytes;
    }

    private static byte[] readBytesAndCheckSize(ByteArrayInputStream inputStream, int contentsSize) throws IOException {
        byte[] inputBytes = new byte[contentsSize];

        int readBytes = inputStream.read(inputBytes);
        assert readBytes == contentsSize : "Unable to read till the end of the buffer.";

        return inputBytes;
    }

    /**
     * All encrypted file types with associated keys.
     */
    public enum EncryptionModeEnum {
        /**
         * Game saves
         */
        SAVEGAME(new int[] {
                0x64EA432C,
                0xF8A35B24,
                0x018ECD81,
                0x8326BEAC },
                0),

        /**
         * Other files (as database).
         * First 8 bytes of encrypted contents are timestamps so must be ignored.
         */
        OTHER_AND_SPECIAL(new int[] {
                0x4FE23C4A,
                0x80BAC211,
                0x6917BD3A,
                0xF0528EBD },
                8);

        /**
         * Encryption key
         */
        private int[] key;

        /**
         * Position in bytes from which contents have to be taken into account.
         */
        private int contentsOffset;

        EncryptionModeEnum(int[] key, int contentsOffset) {
            this.key = key;
            this.contentsOffset = contentsOffset;
        }
    }
}
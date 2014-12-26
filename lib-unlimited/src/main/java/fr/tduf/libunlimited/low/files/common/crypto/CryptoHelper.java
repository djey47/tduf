package fr.tduf.libunlimited.low.files.common.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Utility class to handle TDU files encryption/decryption.
 */
public class CryptoHelper {

    /**
     * Converts encrypted contents in provided input stream to clear ones.
     * @param inputStream           : contents to be decrypted
     * @param encryptionModeEnum    : encryption mode to be used
     * @return an output stream with clear contents.
     */
    public static ByteArrayOutputStream decryptXTEA(ByteArrayInputStream inputStream, EncryptionModeEnum encryptionModeEnum) throws InvalidKeyException, IOException {
        int contentsSize = inputStream.available();
        if (contentsSize % 8 != 0) {
            throw new IllegalArgumentException("Buffer to be decoded must have length multiple of 8. Current=" + contentsSize);
        }

        byte[] inputBytes = new byte[contentsSize];
        int readBytes = inputStream.read(inputBytes);
        assert readBytes == contentsSize : "Unable to read till the end of the buffer.";

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
     *
     * @param byteArrayInputStream
     * @param encryptionModeEnum
     * @return
     */
    public static ByteArrayOutputStream encryptXTEA(ByteArrayInputStream byteArrayInputStream, EncryptionModeEnum encryptionModeEnum) {
        return null;
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
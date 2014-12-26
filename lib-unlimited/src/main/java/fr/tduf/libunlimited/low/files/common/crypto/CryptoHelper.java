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

    /* Number of rounds each pair will be encrypted */
    private final static int NUM_ROUNDS = 32;

    /* Delta value */
    private final static int DELTA = 0x9E3779B9;

    /**
     *
     * @param inputStream
     * @param encryptionModeEnum
     * @return
     */
    public static ByteArrayOutputStream decryptXTEA(ByteArrayInputStream inputStream, EncryptionModeEnum encryptionModeEnum) throws InvalidKeyException, IOException {
        int contentsSize = inputStream.available();
        if (contentsSize % 8 != 0) {
            throw new IllegalArgumentException("Buffer to be decoded must have size multiple of 8. Current=" + contentsSize);
        }

        byte[] inputBytes = new byte[contentsSize];
        int readBytes = inputStream.read(inputBytes);
        assert readBytes == contentsSize : "Unable to read till the end of the buffer.";

        XTEA.engineInit(encryptionModeEnum.key, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int offset = 0;
        while(offset < contentsSize) {
            byte[] outputBytes = XTEA.engineCrypt(inputBytes, offset);
            outputStream.write(outputBytes);

            offset += 8;
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

    static int readInt32(ByteArrayInputStream inputStream) {
        byte[] outBuffer = new byte[4];

        int readBytes = inputStream.read(outBuffer, 0, 4);
        assert readBytes == 4 : "Unconsistent buffer: expected 4 bytes remaining, got " + readBytes;

        return ByteBuffer
                .wrap(outBuffer)
                .getInt();
    }

    static void writeInt32(int value, ByteArrayOutputStream outputStream) {
        byte[] valueBytes = ByteBuffer
                .allocate(4)
                .putInt(value)
                .array();

        for (byte b : valueBytes) {
            outputStream.write(b);
        }
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
                0x8326BEAC
        }),

        /**
         * Other files (as database)
         */
        OTHER_AND_SPECIAL(new int[] {
                0x4FE23C4A,
                0x80BAC211,
                0x6917BD3A,
                0xF0528EBD
        });

        private int[] key;

        EncryptionModeEnum(int[] key) {
            this.key = key;
        }
    }
}
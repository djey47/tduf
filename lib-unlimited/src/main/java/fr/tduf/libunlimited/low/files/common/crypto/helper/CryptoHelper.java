package fr.tduf.libunlimited.low.files.common.crypto.helper;

import fr.tduf.libunlimited.low.files.common.crypto.XTEA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.common.helper.AssertorHelper.assertSimpleCondition;

/**
 * Utility class to handle TDU files encryption/decryption.
 */
public class CryptoHelper {

    private static final int BLOCK_SIZE = 8;

    private static Integer timestampOverride;

    /**
     * Converts encrypted contents in provided input stream to clear ones.
     *
     * @param inputStream        : contents to be decrypted - size must be multiple of 8
     * @param encryptionModeEnum : encryption mode to be used
     * @return an output stream with clear contents.
     */
    public static ByteArrayOutputStream decryptXTEA(ByteArrayInputStream inputStream, EncryptionModeEnum encryptionModeEnum) throws IOException {
        int contentsSize = checkContentsSize(inputStream);
        byte[] inputBytes = readBytes(inputStream, contentsSize);

        try {
            XTEA.engineInit(encryptionModeEnum.key, true);
        } catch (InvalidKeyException e) {
            throw new IOException("Provided key is invalid to decrypt contents.", e);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int position = encryptionModeEnum.contentsOffset;
        while (position < contentsSize) {

            byte[] outputBytes = XTEA.engineCrypt(inputBytes, position);

            if (encryptionModeEnum == EncryptionModeEnum.OTHER_AND_SPECIAL) {
                // XOR XTEA decipher result with input file data
                for (int i = 0; i < outputBytes.length; i++) {
                    int offset = position - encryptionModeEnum.contentsOffset;
                    outputBytes[i] ^= inputBytes[offset + i];
                }
            }

            outputStream.write(outputBytes);

            position += BLOCK_SIZE;
        }

        return outputStream;
    }

    /**
     * Converts clear contents in provided input stream to encrypted ones.
     *
     * @param inputStream        : contents to be encrypted - size must be multiple of 8
     * @param encryptionModeEnum : encryption mode to be used
     * @return an output stream with encrypted contents.
     */
    public static ByteArrayOutputStream encryptXTEA(ByteArrayInputStream inputStream, EncryptionModeEnum encryptionModeEnum) throws IOException {
        int contentsSize = checkContentsSize(inputStream);
        byte[] inputBytes = readBytes(inputStream, contentsSize);

        try {
            XTEA.engineInit(encryptionModeEnum.key, false);
        } catch (InvalidKeyException e) {
            throw new IOException("Provided key is invalid to encrypt contents.", e);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (encryptionModeEnum == EncryptionModeEnum.OTHER_AND_SPECIAL) {
            inputBytes = introduceTimeStamp(inputBytes);
            outputStream.write(inputBytes, 0, 8);
            contentsSize = inputBytes.length;
        }

        int position = encryptionModeEnum.contentsOffset;
        while (position < contentsSize) {

            if (encryptionModeEnum == EncryptionModeEnum.OTHER_AND_SPECIAL) {
                // XOR XTEA encipher current block with previously decoded block
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    inputBytes[position + i] ^= inputBytes[position - BLOCK_SIZE + i];
                }
            }

            byte[] outputBytes = XTEA.engineCrypt(inputBytes, position);
            outputStream.write(outputBytes);

            if (encryptionModeEnum == EncryptionModeEnum.OTHER_AND_SPECIAL) {
                // Store result in input buffer to be used with next iteration
                System.arraycopy(outputBytes, 0, inputBytes, position, BLOCK_SIZE);
            }

            position += BLOCK_SIZE;
        }

        return outputStream;
    }

    private static int getTimestamp() {
        if (timestampOverride == null) {
            return (int) (System.currentTimeMillis() / 1000L);
        }
        return timestampOverride;
    }

    static byte[] introduceTimeStamp(byte[] inputBytes) {

        byte[] currentTimeBytes = ByteBuffer
                .allocate(4)
                .putInt(getTimestamp())
                .array();
        byte[] currentTimeComplementBytes = ByteBuffer
                .allocate(4)
                .putInt(~getTimestamp())
                .array();

        byte[] resultBytes = new byte[inputBytes.length + 8];
        System.arraycopy(currentTimeBytes, 0, resultBytes, 0, 4);
        System.arraycopy(currentTimeComplementBytes, 0, resultBytes, 4, 4);
        System.arraycopy(inputBytes, 0, resultBytes, 8, inputBytes.length);

        return resultBytes;
    }

    /**
     * @param timestamp : in seconds
     */
    static void overrideTimestamp(int timestamp) {
        CryptoHelper.timestampOverride = timestamp;
    }

    static void restoreTimestamp() {
        CryptoHelper.timestampOverride = null;
    }

    private static byte[] readBytes(ByteArrayInputStream inputStream, int contentsSize) throws IOException {
        byte[] inputBytes = new byte[contentsSize];

        int readBytes = inputStream.read(inputBytes);

        assertSimpleCondition(() -> readBytes == contentsSize, "Unable to read till the end of the buffer.");

        return inputBytes;
    }

    private static int checkContentsSize(ByteArrayInputStream inputStream) {
        int contentsSize = inputStream.available();
        if (contentsSize % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Provided buffer must have length multiple of " + BLOCK_SIZE + ". Current=" + contentsSize);
        }
        return contentsSize;
    }

    /**
     * All encrypted file types with associated keys.
     */
    public enum EncryptionModeEnum {
        /**
         * Game saves
         */
        SAVEGAME(0,
                new int[]{
                    0x64EA432C,
                    0xF8A35B24,
                    0x018ECD81,
                    0x8326BEAC},
                0),

        /**
         * Other files (as database).
         * First 8 bytes of encrypted contents are timestamps so must be ignored.
         */
        OTHER_AND_SPECIAL(1,
                new int[]{
                    0x4FE23C4A,
                    0x80BAC211,
                    0x6917BD3A,
                    0xF0528EBD},
                BLOCK_SIZE);

        /**
         * Unique identifier
         */
        private final int id;

        /**
         * Encryption key
         */
        private int[] key;

        /**
         * Position in bytes from which contents have to be taken into account.
         */
        private int contentsOffset;

        EncryptionModeEnum(int id, int[] key, int contentsOffset) {
            this.id = id;
            this.key = key;
            this.contentsOffset = contentsOffset;
        }

        /**
         * Return encryption mode with given identifier.
         * @param cryptoMode    : identifier
         * @return a EncryptionModeEnum member
         */
        public static EncryptionModeEnum fromIdentifier(int cryptoMode) {
            return Stream.of(EncryptionModeEnum.values())
                    .filter(encryptionModeEnum -> encryptionModeEnum.id == cryptoMode)
                    .findAny()
                    .<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException("Unhandled encryption mode: " + cryptoMode));
        }
    }
}
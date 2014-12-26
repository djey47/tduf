package fr.tduf.libunlimited.low.files.common.crypto;

import java.security.InvalidKeyException;

/**
 * XTEA algorithm implementation for Java.
 */
public class XTEA {

    // Configuration
    private static final int
            ROUNDS      = 32,   // iteration count (cycles)
            BLOCK_SIZE  = 8,    // bytes in a data block (64 bits)
            KEY_SIZE    = 4,    // key size (4x32bits)
            DELTA       = 0x9E3779B9,
            D_SUM       = 0xC6EF3720;

    // Subkeys
    private static int[] S;

    // False for encipher, true for decipher
    private static boolean decrypt;

    /**
     * XTEA engine initialization.
     * @param subKey    : sub key to use (derived from encryption key)
     * @param decipher  : true to decrypt data, false to encrypt.
     * @throws InvalidKeyException if provided key does not fulfill requirements (null or invalid size).
     */
    public static void engineInit(int[] subKey, boolean decipher) throws InvalidKeyException {
        checkKey(subKey);

        S = subKey;
        decrypt = decipher;
    }

    /**
     * Encrypt/decrypt one block (BLOCK_SIZE) of data with XTEA algorithm.
     * @param inputBytes    : full array of bytes
     * @param inputOffset   : byte index in array of block to convert
     * @return an array of BLOCK_SIZE elements, which are encrypted or decrypted, as needed.
     */
    public static byte[] engineCrypt(byte[] inputBytes, int inputOffset) {
        // Pack bytes into integers
        int v0 = packBytes(inputBytes, inputOffset);
        int v1 = packBytes(inputBytes, inputOffset + 4);

        int n = ROUNDS;

        if (decrypt) {
            // Decipher
            int sum = D_SUM;

            while (n-- > 0) {
                v1	-= ((v0 << 4 ^ v0 >>> 5) + v0) ^ (sum + S[sum >> 11 & 3]);
                sum -= DELTA;
                v0	-= ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + S[sum & 3]);
            }
        } else {
            // Encipher
            int sum = 0;

            while (n-- > 0) {
                v0	+= ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + S[sum & 3]);
                sum += DELTA;
                v1	+= ((v0 << 4 ^ v0 >>> 5) + v0) ^ (sum + S[sum >> 11 & 3]);
            }
        }

        return unpackInts(v0, v1);
    }

    private static void checkKey(int[] key) throws InvalidKeyException {
        if (key == null ) {
            throw new InvalidKeyException("Null key");
        }

        if (key.length != KEY_SIZE) {
            throw new InvalidKeyException("Invalid key length");
        }
    }

    private static int packBytes(byte[] valueBytes, int inOffset) {
        return ((valueBytes[inOffset++] & 0xFF)      ) |
                ((valueBytes[inOffset++] & 0xFF)    <<  8)  |
                ((valueBytes[inOffset++] & 0xFF)    << 16)  |
                ((valueBytes[inOffset])             << 24);
    }

    private static byte[] unpackInts(int v0, int v1) {
        byte[] out = new byte[BLOCK_SIZE];
        int outOffset = 0;

        out[outOffset++]    = (byte)(v0       );
        out[outOffset++]    = (byte)(v0 >>>  8);
        out[outOffset++]    = (byte)(v0 >>> 16);
        out[outOffset++]    = (byte)(v0 >>> 24);

        out[outOffset++]    = (byte)(v1       );
        out[outOffset++]    = (byte)(v1 >>>  8);
        out[outOffset++]    = (byte)(v1 >>> 16);
        out[outOffset]      = (byte)(v1 >>> 24);

        return out;
    }
}
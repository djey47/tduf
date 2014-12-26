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
     * Initialization
     * @param subKey
     * @param decipher
     * @throws InvalidKeyException
     */
    public static void engineInit(int[] subKey, boolean decipher) throws InvalidKeyException {
        checkKey(subKey);

        S = subKey;
        decrypt = decipher;
    }

    /**
     * Encrypt/decrypt one block of data with XTEA algorithm.
     * @param in
     * @param inOffset
     * @return
     */
    public static byte[] engineCrypt(byte[] in, int inOffset) {
        // Pack bytes into integers
        //TODO see to replace with ByteBuffer.toInt
        int v0 = ((in[inOffset++] & 0xFF)      ) |
                ((in[inOffset++] & 0xFF) <<  8) |
                ((in[inOffset++] & 0xFF) << 16) |
                ((in[inOffset++]       ) << 24);
        int v1 = ((in[inOffset++] & 0xFF)      ) |
                ((in[inOffset++] & 0xFF) <<  8) |
                ((in[inOffset++] & 0xFF) << 16) |
                ((in[inOffset++]       ) << 24);

        int n = ROUNDS, sum;

        // Decipher
        if (decrypt) {
            sum = D_SUM;

            while (n-- > 0) {
                v1	-= ((v0 << 4 ^ v0 >>> 5) + v0) ^ (sum + S[sum >> 11 & 3]);
                sum -= DELTA;
                v0	-= ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + S[sum & 3]);
            }
            // Encipher
        } else {
            sum = 0;

            while (n-- > 0) {
                v0	+= ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + S[sum & 3]);
                sum += DELTA;
                v1	+= ((v0 << 4 ^ v0 >>> 5) + v0) ^ (sum + S[sum >> 11 & 3]);
            }
        }

        // Unpack and return
        int outOffset = 0;
        byte[] out = new byte[BLOCK_SIZE];
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

    private static void checkKey(int[] key) throws InvalidKeyException {
        if (key == null ) {
            throw new InvalidKeyException("Null key");
        }

        if (key.length != KEY_SIZE) {
            throw new InvalidKeyException("Invalid key length");
        }
    }
}
package fr.tduf.libunlimited.low.files.research.common;

import java.nio.ByteBuffer;

/**
 * Helper to class to handle differences between value representations.
 */
public class TypeHelper {

    /**
     * Converts a raw value to TEXT.
     * @param rawValueBytes : raw value to convert
     * @return corresponding value as String
     */
    public static String rawToText(byte[] rawValueBytes) {
        return new String(rawValueBytes);
    }

    /**
     * Converts a raw value to NUMERIC.
     * @param rawValueBytes : raw value to convert
     * @return corresponding value as 64-bit integer
     * @throws IllegalArgumentException when provided Array is not 64-bit (8 bytes)
     */
    public static long rawToNumeric(byte[] rawValueBytes) throws IllegalArgumentException {
        if(rawValueBytes.length != 8) {
            throw new IllegalArgumentException("Provided raw value is not compatible to 64-bit.");
        }

        return ByteBuffer
                .wrap(rawValueBytes)
                .getLong();
    }
}
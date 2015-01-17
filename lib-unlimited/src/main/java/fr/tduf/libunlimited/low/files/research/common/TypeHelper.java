package fr.tduf.libunlimited.low.files.research.common;

import org.apache.commons.lang3.ArrayUtils;

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
     * Converts a raw value to INTEGER.
     * @param rawValueBytes : raw value to convert
     * @return corresponding value as 64-bit integer
     * @throws IllegalArgumentException when provided Array is not 64-bit (8 bytes)
     */
    public static long rawToInteger(byte[] rawValueBytes) throws IllegalArgumentException {
        check64BitRawValue(rawValueBytes);

        return ByteBuffer
                .wrap(rawValueBytes)
                .getLong();
    }

    /**
     * Converts a raw value to FPOINT.
     * @param rawValueBytes : raw value to convert
     * @return corresponding value as 32-bit floating point
     * @throws IllegalArgumentException when provided Array is not 32-bit (4 bytes)
     */
    public static float rawToFloatingPoint(byte[] rawValueBytes) throws IllegalArgumentException {
        check32BitRawValue(rawValueBytes);

        return ByteBuffer
                .wrap(rawValueBytes)
                .getFloat();
    }

    /**
     * Converts a TEXT value to raw byte array.
     * @param textValue  : text value to convert
     * @return corresponding value with default encoding as byte array.
     */
    public static byte[] textToRaw(String textValue) {
        return textValue.getBytes();
    }

    /**
     * Converts an INTEGER value to raw byte array.
     * @param numericValue  : numeric value to convert
     * @return corresponding value with long spec (64-bit, 8 bytes) as byte array.
     */
    public static byte[] integerToRaw(long numericValue) {
        return ByteBuffer
                .allocate(8)
                .putLong(numericValue)
                .array();
    }

    /**
     * Converts a FPOINT value to raw byte array.
     * @param numericValue  : numeric value to convert
     * @return corresponding value with float spec (32-bit, 4 bytes) as byte array.
     */
    public static byte[] floatingPointToRaw(float numericValue) {
        return ByteBuffer
                .allocate(4)
                .putFloat(numericValue)
                .array();
    }

    /**
     * Changes endian type of provided value.
     * @param valueAsBytes  : value to change endian type, size must be >= 2 bytes
     * @return corresponding value with inverted endian.
     */
    public static byte[] changeEndianType(byte[] valueAsBytes) {
        byte[] targetArray = ArrayUtils.clone(valueAsBytes);
        ArrayUtils.reverse(targetArray);
        return targetArray;
    }

    private static void check64BitRawValue(byte[] rawValueBytes) {
        if(rawValueBytes.length != 8) {
            throw new IllegalArgumentException("Provided raw value is not compatible to 64-bit.");
        }
    }

    private static void check32BitRawValue(byte[] rawValueBytes) {
        if(rawValueBytes.length != 4) {
            throw new IllegalArgumentException("Provided raw value is not compatible to 32-bit.");
        }
    }
}
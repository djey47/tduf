package fr.tduf.libunlimited.low.files.research.common.helper;

import com.esotericsoftware.minlog.Log;
import org.apache.commons.lang3.ArrayUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.framework.primitives.Ints.asList;
import static java.lang.Math.min;

/**
 * Helper to class to handle differences between value representations.
 */
public class TypeHelper {
    private static final String THIS_CLASS_NAME = TypeHelper.class.getSimpleName();

    private static final String CHARSET = "ISO-8859-1";

    /**
     * Converts a raw value to TEXT, using array length.
     *
     * @param rawValueBytes : raw value to convert
     * @return corresponding value as String
     */
    public static String rawToText(byte[] rawValueBytes) {
        // TODO convert all other usages with simplified signature
        return rawToText(rawValueBytes, rawValueBytes.length);
    }

    /**
     * Converts a raw value to TEXT, with a specific length.
     *
     * @param rawValueBytes : raw value to convert
     * @param length        : length of String. Actual String will be truncated / followed by 0 when necessary.
     * @return corresponding value as String
     */
    public static String rawToText(byte[] rawValueBytes, int length) {
        String valueAsString = "";
        try {
            valueAsString = new String(rawValueBytes, CHARSET);
        } catch (UnsupportedEncodingException uee) {
            Log.warn(THIS_CLASS_NAME, "Unsupported encoding, resolved value will be empty", uee);
        }

        if (valueAsString.length() < length) {
            byte[] zeroBytes = new byte[length - valueAsString.length()];
            return valueAsString + new String(zeroBytes);
        } else {
            return valueAsString.substring(0, length);
        }
    }

    /**
     * Converts a raw value to INTEGER.
     *
     * @param rawValueBytes : raw value to convert
     * @param signed        : indicates if specified value is signed or not
     * @param size          : value size, in bytes
     * @return corresponding value as 64-bit integer
     * @throws IllegalArgumentException when provided Array is not 64-bit (8 bytes)
     */
    public static long rawToInteger(byte[] rawValueBytes, boolean signed, int size) throws IllegalArgumentException {
        check64BitRawValue(rawValueBytes);
        checkRegularSize(size);

        if (signed) {
            ByteBuffer wrappedBytes = ByteBuffer
                    .wrap(rawValueBytes, 8 - size, size);

            if (size == 1) {
                return wrappedBytes.get();
            } else if (size == 2) {
                return wrappedBytes.getShort();
            } else if (size == 4) {
                return wrappedBytes.getInt();
            } else {
                return wrappedBytes.getLong();
            }
        }

        return ByteBuffer
                .wrap(rawValueBytes)
                .getLong();
    }

    /**
     * Converts a raw value to FPOINT.
     *
     * @param rawValueBytes : raw value to convert
     * @return corresponding value as 32-bit floating point
     * @throws IllegalArgumentException when provided Array is not 32-bit (4 bytes)
     */
    public static float rawToFloatingPoint(byte[] rawValueBytes) throws IllegalArgumentException {
        check16Or32BitRawValue(rawValueBytes);

        if (rawValueBytes.length == 4) {
            // Float
            return ByteBuffer
                    .wrap(rawValueBytes)
                    .getFloat();
        }

        // Half-Float
        short shortFromBytes = ByteBuffer
                .wrap(rawValueBytes)
                .getShort();
        return halfFloatToFloat(shortFromBytes);
    }

    /**
     * Converts a TEXT value to raw byte array.
     *
     * @param textValue : text value to convert
     * @param length    : length of raw byte array. Actual bytes will be truncated / followed by 0 when necessary.
     * @return corresponding value with default encoding as byte array.
     */
    public static byte[] textToRaw(String textValue, int length) {
        byte[] valueBytes = new byte[0];
        try {
            valueBytes = textValue.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            Log.warn(THIS_CLASS_NAME, "Unsupported encoding, resolved value will be of 0 size");
        }

        byte[] targetByteArray = new byte[length];
        System.arraycopy(valueBytes, 0, targetByteArray, 0, min(valueBytes.length, length));

        return targetByteArray;
    }

    /**
     * Converts an INTEGER value to raw byte array.
     *
     * @param numericValue : numeric value to convert
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
     *
     * @param numericValue : numeric value to convert
     * @return corresponding value with float spec (32-bit, 4 bytes) as byte array.
     */
    public static byte[] floatingPoint32ToRaw(float numericValue) {
        return ByteBuffer
                .allocate(4)
                .putFloat(numericValue)
                .array();
    }

    /**
     * Converts a FPOINT value to raw byte array.
     *
     * @param numericValue : numeric value to convert
     * @return corresponding value with half-float spec (16-bit, 2 bytes) as byte array.
     */
    public static byte[] floatingPoint16ToRaw(float numericValue) {
        int intBytes = halfFloatfromFloat(numericValue);

        return ByteBuffer
                .allocate(2)
                .putShort((short) intBytes)
                .array();
    }

    /**
     * Changes endian type of provided value.
     *
     * @param valueAsBytes : value to change endian type, size must be >= 2 bytes
     * @return corresponding value with inverted endian.
     */
    public static byte[] changeEndianType(byte[] valueAsBytes) {
        byte[] targetArray = ArrayUtils.clone(valueAsBytes);
        ArrayUtils.reverse(targetArray);
        return targetArray;
    }

    /**
     * Provides a byte array to fit particular size (truncate or fill with zeros).
     *
     * @param valueBytes    : byte array to transform
     * @param length        : wanted size, may be null. In that case, array will be returned as is.
     * @return a new byte array
     */
    public static byte[] fitToSize(byte[] valueBytes, Integer length) {

        if (valueBytes == null) {
            return null;
        }

        if (length == null) {
            length = valueBytes.length;
        }

        byte[] newArray = new byte[length];
        System.arraycopy(
                valueBytes, 0,
                newArray, 0, (length < valueBytes.length ? length : valueBytes.length));

        return newArray;
    }


    /**
     * Gives a representation of provided byte array with hexadecimal values.
     * @param valueBytes    : byte array to be converted
     */
    public static String byteArrayToHexRepresentation(byte[] valueBytes) {
        if (valueBytes == null) {
            return null;
        }

        String hexBytesWithSpaceSeparator = DatatypeConverter.printHexBinary(valueBytes)
                .replaceAll(".{2}", "$0 ")
                .trim();

        return String.format("0x[%s]", hexBytesWithSpaceSeparator);
    }

    /**
     * Creates a byte array from provided hexadecimal string representation.
     * @param hexRepresentation : representation to be converted
     */
    public static byte[] hexRepresentationToByteArray(String hexRepresentation) {

        if(hexRepresentation == null) {
            return null;
        }

        Pattern hexRepresentationPattern = Pattern.compile("0x\\[([0-9a-fA-F]{2}\\s?)*([0-9a-fA-F]{2})?]");
        if(!hexRepresentationPattern.matcher(hexRepresentation).matches()) {
            throw new IllegalArgumentException("Provided hexadecimal representation is invalid.");
        }

        String extractedBytes = hexRepresentation
                .substring(3, hexRepresentation.length() - 1)
                .replace(" ", "");

        return DatatypeConverter.parseHexBinary(extractedBytes);
    }

    private static void checkRegularSize(int size) {
        if (!asList(1, 2, 4, 8).contains(size)) {
            throw new IllegalArgumentException("Provided size is not any of BYTE(1), SHORT(2), INTEGER(4) or LONG(8): " + size);
        }
    }

    private static void check64BitRawValue(byte[] rawValueBytes) {
        if (rawValueBytes.length != 8) {
            throw new IllegalArgumentException("Provided raw value is not compatible to 64-bit.");
        }
    }

    private static void check16Or32BitRawValue(byte[] rawValueBytes) {
        if (rawValueBytes.length != 2 && rawValueBytes.length != 4) {
            throw new IllegalArgumentException("Provided raw value is not compatible to 16-bit nor 32-bit.");
        }
    }

    private static float halfFloatToFloat(int hbits) {
        int mant = hbits & 0x03ff;            // 10 bits mantissa
        int exp = hbits & 0x7c00;            // 5 bits exponent
        if (exp == 0x7c00)                   // NaN/Inf
            exp = 0x3fc00;                    // -> NaN/Inf
        else if (exp != 0)                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if (mant == 0 && exp > 0x1c400)  // smooth transition
                return Float.intBitsToFloat((hbits & 0x8000) << 16
                        | exp << 13 | 0x3ff);
        } else if (mant != 0)                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                   // mantissa * 2
                exp -= 0x400;                 // decrease exp by 1
            } while ((mant & 0x400) == 0); // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
                (hbits & 0x8000) << 16          // sign  << ( 31 - 15 )
                        | (exp | mant) << 13);         // value << ( 23 - 10 )
    }

    private static int halfFloatfromFloat(float fval)
    {
        int fbits = Float.floatToIntBits( fval );
        int sign = fbits >>> 16 & 0x8000;          // sign only
        int val = ( fbits & 0x7fffffff ) + 0x1000; // rounded value

        if( val >= 0x47800000 )               // might be or become NaN/Inf
        {                                     // avoid Inf due to rounding
            if( ( fbits & 0x7fffffff ) >= 0x47800000 )
            {                                 // is or must become NaN/Inf
                if( val < 0x7f800000 )        // was value but too large
                    return sign | 0x7c00;     // make it +/-Inf
                return sign | 0x7c00 |        // remains +/-Inf or NaN
                        ( fbits & 0x007fffff ) >>> 13; // keep NaN (and Inf) bits
            }
            return sign | 0x7bff;             // unrounded not quite Inf
        }
        if( val >= 0x38800000 )               // remains normalized value
            return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
        if( val < 0x33000000 )                // too small for subnormal
            return sign;                      // becomes +/-0
        val = ( fbits & 0x7fffffff ) >>> 23;  // tmp exp for subnormal calc
        return sign | ( ( fbits & 0x7fffff | 0x800000 ) // add subnormal bit
                + ( 0x800000 >>> val - 102 )     // round depending on cut off
                >>> 126 - val );   // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
    }
}
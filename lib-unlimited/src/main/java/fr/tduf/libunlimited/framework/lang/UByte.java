package fr.tduf.libunlimited.framework.lang;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;
import java.util.StringJoiner;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;

/**
 * Mimics the .net's unsigned Byte which is the default
 */
public class UByte {
    private final byte signedValueHolder;
    private final int unsignedValueHolder;

    private UByte(byte signedValue, int unsignedValue){
        unsignedValueHolder = unsignedValue;
        signedValueHolder = signedValue;
    }

    public static UByte fromSigned(byte signedByte) {
        return new UByte(signedByte, 255 - signedByte);
    }

    public int get() {
        return unsignedValueHolder;
    }

    public int getSigned() {
        return signedValueHolder;
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o, "signedValueHolder");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this, "signedValueHolder");
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", getClass().getSimpleName(), unsignedValueHolder);
    }
}

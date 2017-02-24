package fr.tduf.libunlimited.low.files.research.domain;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a store entry to bring more information.
 */
class Entry {
    private final Type type;
    private final boolean signed;
    private final byte[] rawValue;
    private final int size;

    Entry(Type type, byte[] rawValue) {
        this(type, false, rawValue.length, rawValue);
    }

    Entry(Type type, boolean signed, Integer size, byte[] rawValue) {
        this.type = type;
        this.size = size;
        this.rawValue = rawValue;
        this.signed = signed;
    }

    /**
     * @return full copy of current entry instance.
     */
    public Entry copy() {
        byte[] rawValueCopy = new byte[rawValue.length];
        System.arraycopy(rawValue, 0, rawValueCopy, 0, rawValue.length);
        return new Entry(type, signed, size, rawValueCopy);
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    public boolean isSigned() {
        return signed;
    }

    public int getSize() {
        return size;
    }

    byte[] getRawValue() {
        return rawValue;
    }

    Type getType() {
        return type;
    }
}
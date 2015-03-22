package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a store entry to bring more information.
 */
class Entry {
    private final FileStructureDto.Type type;
    private final byte[] rawValue;

    Entry(FileStructureDto.Type type, byte[] rawValue) {
        this.type = type;
        this.rawValue = rawValue;
    }

    byte[] getRawValue() {
        return rawValue;
    }

    FileStructureDto.Type getType() {
        return type;
    }

    /**
     * @return full copy of current entry instance.
     */
    public Entry copy() {
        byte[] rawValueCopy = new byte[rawValue.length];
        System.arraycopy(rawValue, 0, rawValueCopy, 0, rawValue.length);
        return new Entry(type, rawValueCopy);
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
}
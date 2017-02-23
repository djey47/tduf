package fr.tduf.libunlimited.low.files.research.domain;

/**
 * Describes all field types.
 */
public enum Type {
    /**
     * Values to be understood, still.
     */
    UNKNOWN(true),
    /**
     * Value as string
     */
    TEXT(true),
    /**
     * Numeric Integer value. Currently handled: 32bit only.
     */
    INTEGER(true),
    /**
     * Numeric Floating Point value. Currently handled: 32bit only.
     */
    FPOINT(true),
    /**
     * Delimiter with particular value
     */
    DELIMITER(true),
    /**
     * Allow to repeat a sub-structure
     */
    REPEATER(false),
    /**
     * Hole in the file. Only contains zeros.
     */
    GAP(false),
    /**
     * Value is always the same
     */
    CONSTANT(false);

    /**
     * Indicates if this type of value will be stored to allow requests on it.
     */
    private final boolean valuedToBeStored;

    Type(boolean valueToBeStored) {
        this.valuedToBeStored = valueToBeStored;
    }

    public boolean isValueToBeStored() {
        return valuedToBeStored;
    }
}

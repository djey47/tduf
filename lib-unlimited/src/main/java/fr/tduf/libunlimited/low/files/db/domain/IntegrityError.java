package fr.tduf.libunlimited.low.files.db.domain;

/**
 * Represents an error contained in database files
 */
public class IntegrityError {
    //TODO bring more information on error

    private final ErrorTypeEnum errorTypeEnum;

    public IntegrityError(ErrorTypeEnum errorTypeEnum) {
        this.errorTypeEnum = errorTypeEnum;
    }

    public String getError() {
        return errorTypeEnum.name();
    }

    @Override
    public String toString() {
        return "DatabaseIntegrityError: " + errorTypeEnum;
    }

    /**
     * All integrity error types.
     */
    public enum ErrorTypeEnum {
        /**
         * Read Item count not same as displayed one
         */
        CONTENT_ITEMS_COUNT_MISMATCH,

        /**
         * Read Field count in structure not same as displayed one
         */
        STRUCTURE_FIELDS_COUNT_MISMATCH,

        /**
         * Read Field count in contents not same as displayed one
         */
        CONTENTS_FIELDS_COUNT_MISMATCH,

        /**
         * Read resource items count not same over all language files
         */
        RESOURCE_ITEMS_COUNT_MISMATCH
    }
}

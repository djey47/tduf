package fr.tduf.libunlimited.low.files.db.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an error contained in database files
 */
public class IntegrityError {
    private final ErrorTypeEnum errorTypeEnum;

    private final Map<String, Object> info;

    private IntegrityError(ErrorTypeEnum errorTypeEnum, Map<String, Object> info) {
        this.errorTypeEnum = errorTypeEnum;
        this.info = info;
    }

    public static IntegrityErrorBuilder builder() {
        return new IntegrityErrorBuilder() {
            private final Map<String, Object> info = new HashMap<>();
            private ErrorTypeEnum errorTypeEnum;

            @Override
            public IntegrityErrorBuilder ofType(ErrorTypeEnum errorTypeEnum) {
                this.errorTypeEnum = errorTypeEnum;
                return this;
            }

            @Override
            public IntegrityErrorBuilder addInformation(String label, String value) {
                this.info.put(label, value);
                return this;
            }

            @Override
            public IntegrityErrorBuilder addInformations(Map<String, Object> info) {
                this.info.putAll(info);
                return this;
            }

            @Override
            public IntegrityError build() {
                return new IntegrityError(this.errorTypeEnum, this.info);
            }
        };
    }

    public String getError() {
        return errorTypeEnum.name();
    }

    public String getErrorMessageFormat() {
        return errorTypeEnum.errorMessageFormat;
    }

    @Override
    public String toString() {
        return "DatabaseIntegrityError: " + errorTypeEnum + ", " + info;
    }

    /**
     * All integrity error types.
     */
    public enum ErrorTypeEnum {
        /**
         * Read Item count not same as displayed one
         */
        CONTENT_ITEMS_COUNT_MISMATCH("Item count information (%d) is not same as actual item count (%d)."),

        /**
         * Read Field count in structure not same as displayed one
         */
        STRUCTURE_FIELDS_COUNT_MISMATCH("Field count information in structure (%d) is not same as actual field count (%d)."),

        /**
         * Read Field count in contents not same as displayed one
         */
        CONTENTS_FIELDS_COUNT_MISMATCH("Field count information in structure (%d) is not same as actual field count (%d)."),

        /**
         * For a topic, could not access a corresponding resource.
         */
        RESOURCE_NOT_FOUND("A resource was not found for topic %s."),

        /**
         * Read resource items count not same over all language files
         */
        RESOURCE_ITEMS_COUNT_MISMATCH("Resource items count is not same over %d languages:\n%s");

        private final String errorMessageFormat;

        ErrorTypeEnum(String errorMessageFormat) {
            this.errorMessageFormat = errorMessageFormat;
        }
    }

    public interface IntegrityErrorBuilder {

        IntegrityErrorBuilder ofType(ErrorTypeEnum errorTypeEnum);

        IntegrityErrorBuilder addInformation(String label, String value);

        IntegrityErrorBuilder addInformations(Map<String, Object> info);

        IntegrityError build();
    }
}
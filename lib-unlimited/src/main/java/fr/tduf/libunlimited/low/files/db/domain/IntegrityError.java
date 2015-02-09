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

    public String getError() { return errorTypeEnum.name(); }

    public Map<String, Object> getInformation() {
        return info;
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
        CONTENT_ITEMS_COUNT_MISMATCH("Item count information is not same as actual item count: %s"),

        /**
         * Read Field count in structure not same as displayed one
         */
        STRUCTURE_FIELDS_COUNT_MISMATCH("Field count information in structure is not same as actual field count: %s"),

        /**
         * Read Field count in contents not same as displayed one
         */
        CONTENTS_FIELDS_COUNT_MISMATCH("Field count information in structure is not same as actual field count: %s"),

        /**
         * For a topic, could not access to corresponding resource.
         */
        RESOURCE_NOT_FOUND("A language resource file was not found for current topic: %s"),

        /**
         * For a reference value, could not access to corresponding value in resource.
         */
        RESOURCE_REFERENCE_NOT_FOUND("A reference to resource in same topic was not found: %s for locale: %s"),

        /**
         * For a topic, could not access to corresponding contents.
         */
        CONTENTS_NOT_FOUND("Contents file was not found for current topic: %s.db"),

        /**
         * For a topic, contents file exist but could not be unencrypted
         */
        CONTENTS_ENCRYPTION_NOT_SUPPORTED("Contents file could not be unencrypted for current topic: %s.db"),

        /**
         * Read resource items count not same over all language files
         */
        RESOURCE_ITEMS_COUNT_MISMATCH("Resource items count is not same over all language files: %s");

        private final String errorMessageFormat;

        ErrorTypeEnum(String errorMessageFormat) {
            this.errorMessageFormat = errorMessageFormat;
        }
    }

    public interface IntegrityErrorBuilder {

        IntegrityErrorBuilder ofType(ErrorTypeEnum errorTypeEnum);

        IntegrityErrorBuilder addInformations(Map<String, Object> info);

        IntegrityError build();
    }
}
package fr.tduf.libunlimited.low.files.db.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an error contained in database files
 */
public class IntegrityError {
    private final ErrorTypeEnum errorTypeEnum;

    private final Map<ErrorInfoEnum, Object> info;

    private IntegrityError(ErrorTypeEnum errorTypeEnum, Map<ErrorInfoEnum, Object> info) {
        this.errorTypeEnum = errorTypeEnum;
        this.info = info;
    }

    public static IntegrityErrorBuilder builder() {
        return new IntegrityErrorBuilder() {
            private final Map<ErrorInfoEnum, Object> info = new HashMap<>();
            private ErrorTypeEnum errorTypeEnum;

            @Override
            public IntegrityErrorBuilder ofType(ErrorTypeEnum errorTypeEnum) {
                this.errorTypeEnum = errorTypeEnum;
                return this;
            }

            @Override
            public IntegrityErrorBuilder addInformations(Map<ErrorInfoEnum, Object> info) {
                this.info.putAll(info);
                return this;
            }

            @Override
            public IntegrityErrorBuilder addInformation(ErrorInfoEnum errorInfoEnum, Object value) {
                this.info.put(errorInfoEnum,value);
                return this;
            }

            @Override
            public IntegrityError build() {
                return new IntegrityError(this.errorTypeEnum, this.info);
            }
        };
    }

    public String getError() { return errorTypeEnum.name(); }

    public ErrorTypeEnum getErrorTypeEnum() { return errorTypeEnum; }

    public Map<ErrorInfoEnum, Object> getInformation() {
        return info;
    }

    public String getErrorMessageFormat() {
        return errorTypeEnum.errorMessageFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrityError that = (IntegrityError) o;
        return Objects.equals(errorTypeEnum, that.errorTypeEnum) &&
                Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorTypeEnum, info);
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
         * An incomplete list of topic objects has been provided to database checker
         */
        INCOMPLETE_DATABASE("One or more database topics are missing: %s"),

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
        RESOURCE_NOT_FOUND("Resource file was not found for current topic: %s"),

        /**
         * For a reference value, could not access to corresponding value in resource.
         */
        RESOURCE_REFERENCE_NOT_FOUND("A reference to resource in topic was not found: %s"),

        /**
         * For a topic, could not access to corresponding contents.
         */
        CONTENTS_NOT_FOUND("Contents file was not found for current topic: %s"),

        /**
         * For a reference value, could not access to corresponding entry in other topic contents.
         */
        CONTENTS_REFERENCE_NOT_FOUND("A reference to contents in topic was not found: %s"),

        /**
         * For a topic, contents file exist but could not be unencrypted
         */
        CONTENTS_ENCRYPTION_NOT_SUPPORTED("Contents file could not be unencrypted for current topic: %s"),

        /**
         * Read resource items count not same over all language files
         */
        RESOURCE_ITEMS_COUNT_MISMATCH("Resource items count is not the same over all language files: %s"),

        /**
         * Per-locale values for a globalized resource reference are not the same
         */
        RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES("Resource values for gloablized resource are not the same through all locales: %s");

        private final String errorMessageFormat;

        ErrorTypeEnum(String errorMessageFormat) {
            this.errorMessageFormat = errorMessageFormat;
        }
    }

    /**
     * All error informations.
     */
    public enum ErrorInfoEnum {

        SOURCE_TOPIC("Source Topic"),
        REMOTE_TOPIC("Remote Topic"),
        MISSING_TOPICS("Missing topics"),
        LOCALE("Locale"),
        REFERENCE("Reference"),
        PER_LOCALE_COUNT("Per-Locale Count"),
        PER_VALUE_COUNT("Per-Value Count"),
        EXPECTED_COUNT("Expected Count"),
        ACTUAL_COUNT("Actual Count"),
        FILE("File Name"),
        ENTRY_ID("Data Entry Identifier");

        private final String infoLabel;

        ErrorInfoEnum(String infoLabel) {
            this.infoLabel = infoLabel;
        }

        @Override
        public String toString() {
            return this.infoLabel;
        }

        public String getInfoLabel() {
            return infoLabel;
        }
    }

    public interface IntegrityErrorBuilder {

        IntegrityErrorBuilder ofType(ErrorTypeEnum errorTypeEnum);

        IntegrityErrorBuilder addInformations(Map<ErrorInfoEnum, Object> info);

        IntegrityErrorBuilder addInformation(ErrorInfoEnum errorInfoEnum, Object value);

        IntegrityError build();
    }
}
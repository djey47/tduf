package fr.tduf.cli.tools.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;

import java.io.Serializable;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * Object to describe encountered database integrity errors.
 */
@JsonTypeName("databaseIntegrityError")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseIntegrityErrorDto implements Serializable {

    @JsonProperty("errorType")
    private String errorType;

    @JsonProperty("information")
    private Map<String, Object> information;

    private DatabaseIntegrityErrorDto(String errorType, Map<String, Object> information) {
        this.errorType = errorType;
        this.information = information;
    }

    /**
     * @param integrityError    : domain error to convert.
     * @return instance corresponding to domain error.
     */
    public static DatabaseIntegrityErrorDto fromIntegrityError(IntegrityError integrityError) {
        Map<String, Object> displayedInformation = requireNonNull(integrityError, "Integrity error is required.").getInformation().entrySet().stream()

                .collect(toMap((entry) -> toAttributeCase(entry.getKey().getInfoLabel()), Map.Entry::getValue));

        return new DatabaseIntegrityErrorDto(integrityError.getErrorTypeEnum().name(), displayedInformation);
    }

    static String toAttributeCase(String text) {
        if (text == null) {
            return null;
        }

        if ("".equals(text)) {
            return "";
        }

        text = text.replaceAll("[\\s_\\-]", "");

        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    public String getErrorType() {
        return errorType;
    }

    public Map<String, Object> getInformation() {
        return information;
    }
}
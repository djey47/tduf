package fr.tduf.cli.tools.dto;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Output object for all errors.
 */
@JsonTypeName("errorOutput")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ErrorOutputDto implements Serializable {

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("stackTrace")
    private String stackTrace;

    private ErrorOutputDto(String errorMessage, String stackTrace) {
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public static ErrorOutputDto fromException(Exception exception) {
        requireNonNull(exception, "An exception is required.");

        return new ErrorOutputDto(exception.getMessage(), ExceptionUtils.getStackTrace(exception));
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
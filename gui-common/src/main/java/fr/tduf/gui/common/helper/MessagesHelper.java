package fr.tduf.gui.common.helper;

import javafx.concurrent.Service;

import static fr.tduf.gui.common.DisplayConstants.*;
import static java.util.Objects.requireNonNull;

/**
 * Provide static methods for messages rendering
 */
public class MessagesHelper {
    /**
     * Formats error message for given exception
     * @param exception   exception instance
     * @return error message to be displayed
     */
    public static String getGenericErrorMessage(Exception exception) {
        requireNonNull(exception, "Exception must not be null");
        return String.format(FORMAT_MESSAGE_GENERIC_ERROR, exception.getMessage());
    }

    /**
     * Formats advanced error message for given throwable
     * @param throwable   throwable instance
     * @return error message to be displayed
     */
    public static String getAdvancedErrorMessage(Throwable throwable, String additionalMessage) {
        requireNonNull(throwable, "Throwable must not be null");

        String causeMessage = "";
        if (throwable.getCause() != null
                && throwable.getCause() != throwable) {
            causeMessage = throwable.getCause().getMessage();
        }

        return String.format(FORMAT_MESSAGE_ADVANCED_ERROR,
                throwable.getMessage(),
                causeMessage,
                additionalMessage == null ? "" : additionalMessage);
    }

    /**
     * Formats error message for given service
     * @param service   service instance
     * @return error message to be displayed
     */
    public static String getServiceErrorMessage(Service<?> service) {
        requireNonNull(service, "Service instance must not be null");
        return String.format(FORMAT_MESSAGE_SERVICE_ERROR, service.getMessage());
    }
}

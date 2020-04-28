package fr.tduf.gui.common.helper;

import static fr.tduf.gui.common.DisplayConstants.FORMAT_MESSAGE_GENERIC_ERROR;
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
}

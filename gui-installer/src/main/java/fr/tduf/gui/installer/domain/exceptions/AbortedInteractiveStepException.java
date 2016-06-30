package fr.tduf.gui.installer.domain.exceptions;

import fr.tduf.gui.installer.common.DisplayConstants;

/**
 * User has cancelled while performing selection
 */
public class AbortedInteractiveStepException extends Exception {
    /**
     * Unique constructor
     */
    public AbortedInteractiveStepException() {
        super(DisplayConstants.MESSAGE_ABORTED_USER);
    }
}

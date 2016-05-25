package fr.tduf.gui.common;

/**
 * Gives all constants to be displayed by controllers.
 */
public class DisplayConstants {
    public static final String TITLE_SUB_CHECK_DB = " : Check database";
    public static final String TITLE_SUB_FIX_DB = " : Fix database";

    public static final String MESSAGE_DB_CHECK_OK = "Database was checked succesfully.";
    public static final String MESSAGE_DB_CHECK_KO = "Database could not be checked.";
    public static final String MESSAGE_DB_ZERO_ERROR = "No integrity error was found.";
    public static final String MESSAGE_DB_FIX_OK = "Database was fixed succesfully.";
    public static final String MESSAGE_DB_FIX_KO = "Database could not be fixed.";
    public static final String MESSAGE_DB_REMAINING_ERRORS = "Integrity error(s) do remain.\nGame might be playable, though.";

    public static final String LABEL_FMT_ERROR_CPL = "(%d)";

    public static final String STATUS_FMT_CHECK_IN_PROGRESS = "Performing database check %s, please wait...";
    public static final String STATUS_FMT_CHECK_DONE = "Done checking database, %d error(s).";
    public static final String STATUS_FMT_FIX_IN_PROGRESS = "Performing database fix %s, please wait...";
    public static final String STATUS_FMT_FIX_DONE = "Done fixing database, %d error(s) remaining.";

    private DisplayConstants() {}
}

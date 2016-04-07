package fr.tduf.gui.installer.common;

/**
 * Gives all constants to be displayed by controllers.
 */
public class DisplayConstants {
    public static final String PROMPT_TEXT_TDU_LOCATION = "e.g C:\\Program Files\\Test Drive Unlimited";

    public static final String TITLE_APPLICATION = "TDUF Vehicle Installer";
    public static final String TITLE_SUB_INSTALL = " : Install";
    public static final String TITLE_SUB_MAP_UPDATE = " : Update Magic Map";
    public static final String TITLE_SUB_RESET_DB_CACHE = " : Reset database cache";
    public static final String TITLE_SUB_CHECK_DB = " : Check database";
    public static final String TITLE_SUB_FIX_DB = " : Fix database";
    public static final String TITLE_SUB_SEARCH_SLOT = " : Search for vehicle slot";

    public static final String LABEL_SEARCH_SLOT = "With REF?";
    public static final String LABEL_UNKNOWN = "???";

    public static final String LABEL_FMT_FREE_SLOTS = "%d / %d";

    public static final String MESSAGE_INSTALLED = "All install tasks went succesfully!";
    public static final String MESSAGE_NOT_INSTALLED = "An issue occured while installing!";
    public static final String MESSAGE_UPDATED_MAP = "Magic map was updated to take new files into account:";
    public static final String MESSAGE_DELETED_CACHE = "Database cache directory was deleted succesfully:";
    public static final String MESSAGE_DB_CHECK_OK = "Database was checked succesfully.";
    public static final String MESSAGE_DB_FIX_OK = "Database was fixed succesfully.";
    public static final String MESSAGE_DB_FIX_KO = "Database could not be fixed succesfully.";
    public static final String MESSAGE_DB_ZERO_ERROR = "No integrity error was found.";
    public static final String MESSAGE_DB_REMAINING_ERRORS = "Integrity error(s) do remain.\nGame might be playable, though.";

    public static final String ITEM_UNAVAILABLE = "N/A";
}

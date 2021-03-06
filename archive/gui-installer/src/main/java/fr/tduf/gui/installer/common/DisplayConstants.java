package fr.tduf.gui.installer.common;

/**
 * Gives all constants to be displayed by controllers.
 */
public class DisplayConstants {
    public static final String PROMPT_TEXT_TDU_LOCATION = "e.g C:\\Program Files\\Test Drive Unlimited";

    public static final String TITLE_APPLICATION = "TDUF Vehicle Installer";
    public static final String TITLE_SUB_INSTALL = " : Install";
    public static final String TITLE_SUB_UNINSTALL = " : Uninstall";
    public static final String TITLE_SUB_MAP_UPDATE = " : Update Magic Map";
    public static final String TITLE_SUB_RESET_TDUCP_SLOT = " : Reset TDUCP slot";
    public static final String TITLE_SUB_RESET_DB_CACHE = " : Reset database cache";
    public static final String TITLE_SUB_CHECK_DB = " : Check database";
    public static final String TITLE_SUB_FIX_DB = " : Fix database";
    public static final String TITLE_SUB_SEARCH_SLOT = " : Search for vehicle slot";

    public static final String LABEL_SEARCH_SLOT = "With REF?";
    public static final String LABEL_UNKNOWN = "???";
    public static final String LABEL_FMT_FREE_SLOTS = "%d / %d";
    public static final String LABEL_FREE_DEALER_SLOT = "<available>";
    public static final String LABEL_STEP_UNKNOWN = "<Uncategorized>";

    public static final String MESSAGE_INSTALLED = "All install tasks went succesfully!";
    public static final String MESSAGE_UNINSTALLED = "All uninstall tasks went succesfully!";
    public static final String MESSAGE_NOT_INSTALLED = "An issue occured while installing!";
    public static final String MESSAGE_NOT_UNINSTALLED = "An issue occured while uninstalling!";
    public static final String MESSAGE_NOT_RESET = "An issue occured while resetting TDUCP new slot!";
    public static final String MESSAGE_UPDATED_MAP = "Magic map was updated to take new files into account:";
    public static final String MESSAGE_DELETED_CACHE = "Database cache directory was deleted succesfully:";
    public static final String MESSAGE_RESET_SLOT = "TDUCP new slot was reverted succesfully!";
    public static final String MESSAGE_DB_CHECK_OK = "Database was checked succesfully.";
    public static final String MESSAGE_DB_CHECK_KO = "Database could not be checked.";
    public static final String MESSAGE_DB_FIX_OK = "Database was fixed succesfully.";
    public static final String MESSAGE_DB_FIX_KO = "Database could not be fixed.";
    public static final String MESSAGE_DB_LOAD_KO = "Database could not be loaded.";
    public static final String MESSAGE_DB_ZERO_ERROR = "No integrity error was found.";
    public static final String MESSAGE_DB_REMAINING_ERRORS = "Integrity error(s) do remain.\nGame might be playable, though.";
    public static final String MESSAGE_ABORTED_USER = "Aborted by user";
    public static final String MESSAGE_INVALID_PROPERTIES = "Invalid patch properties";
    public static final String MESSAGE_STEP_KO = "Current step could not be performed";
    public static final String MESSAGE_PATCH_LOAD_KO = "Install patch could not be loaded";
    public static final String MESSAGE_INSTALL_ABORTED = "Install cannot continue";
    public static final String MESSAGE_OPERATION_ABORTED = "Current operation cannot continue";
    public static final String MESSAGE_BACKUP_NOT_FOUND = "No backup found.\nUse advanced operations to restore a vehicle slot.";

    public static final String FORMAT_MESSAGE_STEP = "[Step: %s]";
    public static final String MESSAGE_FMT_INVALID_SLOT_INFO = "Unable to get valid information for vehicle slot, as it does not exist: %s";
    public static final String MESSAGE_FMT_PATCH_NOT_FOUND = "Patch file not found in %s subdirectory.";

    public static final String STATUS_FMT_LOAD_IN_PROGRESS = "Performing database load %s, please wait...";
    public static final String STATUS_LOAD_DONE = "Done loading database.";
    public static final String STATUS_LOAD_FAILED = "Could not load database.";
    public static final String STATUS_INSTALL_IN_PROGRESS = "Performing install, please wait...";
    public static final String STATUS_UNINSTALL_IN_PROGRESS = "Performing uninstall, please wait...";
    public static final String STATUS_RESET_IN_PROGRESS = "Performing TDUCP slot reset, please wait...";
    public static final String STATUS_INSTALL_DONE = "Done installing.";
    public static final String STATUS_UNINSTALL_DONE = "Done uninstalling.";
    public static final String STATUS_RESET_DONE = "Done resetting.";
    public static final String STATUS_INSTALL_KO = "Done installing with error(s).";
    public static final String STATUS_UNINSTALL_KO = "Done uninstalling with error(s).";
    public static final String STATUS_RESET_KO = "Done resetting with error(s).";

    public static final String ITEM_UNAVAILABLE = "N/A";
    public static final String ITEM_DEALER_KIND_ALL = "All";
    public static final String ITEM_DEALER_KIND_CAR_DEALER = "Car dealer";
    public static final String ITEM_DEALER_KIND_BIKE_DEALER = "Bike dealer";
    public static final String ITEM_DEALER_KIND_RENTAL = "V-Rent";
    public static final String ITEM_VEHICLE_KIND_DRIVABLE = "Drivable";
    public static final String ITEM_VEHICLE_KIND_CAR = "Car";
    public static final String ITEM_VEHICLE_KIND_BIKE = "Bike";
    public static final String ITEM_SLOT_KIND_ALL = "All";
    public static final String ITEM_SLOT_KIND_GENUINE = "Genuine";
    public static final String ITEM_SLOT_KIND_TDUCP = "TDUCP (all)";
    public static final String ITEM_SLOT_KIND_TDUCP_NEW = "TDUCP (new)";

    private DisplayConstants() {}
}

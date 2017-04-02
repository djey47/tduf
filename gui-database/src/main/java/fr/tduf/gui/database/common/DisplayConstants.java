package fr.tduf.gui.database.common;

/**
 * Gives all constants to be displayed by controllers.
 */
public class DisplayConstants {
    public static final String SEPARATOR_VALUES = " - ";
    public static final String VALUE_ENTRY_CELL = "%d:%s";
    public static final String VALUE_UNKNOWN = "<%s>";
    public static final String VALUE_FIELD_DEFAULT = "";
    public static final String VALUE_RESOURCE_DEFAULT = "";
    public static final String VALUE_YES = "Y";
    public static final String VALUE_NO = "N";

    public static final String LABEL_BUTTON_BROWSE = "...";
    public static final String LABEL_BUTTON_CANCEL = "Cancel";
    public static final String LABEL_BUTTON_GOTO = "\u25B6";
    public static final String LABEL_BUTTON_MINUS = "-";
    public static final String LABEL_BUTTON_UP = "\u25B2";
    public static final String LABEL_BUTTON_DOWN = "\u25BC";
    public static final String LABEL_BUTTON_OK = "OK";
    public static final String LABEL_BUTTON_PLUS = "+";
    public static final String LABEL_BUTTON_CLOSE = "Close";
    public static final String LABEL_BUTTON_SAVE = "Save...";
    public static final String LABEL_ITEM_DATABASE_ENTRY = "%s : %s";
    public static final String LABEL_ITEM_ENTRY_COUNT = "/ %d";
    public static final String LABEL_ITEM_ENTRY_COUNT_DEFAULT = "";
    public static final String LABEL_ITEM_ENTRY_DEFAULT = "";
    public static final String LABEL_ITEM_ENTRY_INDEX_DEFAULT = "";
    public static final String LABEL_ITEM_LOCALE_ALL = "All";
    public static final String LABEL_ITEM_LOCALE_CURRENT = "%s (current)";
    public static final String LABEL_ITEM_PERCENT_DEFAULT = "";
    public static final String LABEL_ITEM_REFERENCE = "Reference to another topic.";
    public static final String LABEL_ITEM_TOPIC_DEFAULT = "";
    public static final String LABEL_SEARCH_ENTRY = "Enter an entry REF";
    public static final String LABEL_HEADER_SEARCH_VALUE = "Enter a resource value";
    public static final String LABEL_STATUS_VERSION = "TDUF Database Editor by Djey.";
    public static final String LABEL_TEXTFIELD_REFERENCE = "Reference:";
    public static final String LABEL_TEXTFIELD_VALUE = "Value@Locale:";
    public static final String LABEL_ENTRY_SELECT_SINGLE = "Click an entry below to use it.";
    public static final String LABEL_ENTRY_SELECT_MANY = "CTRL-Pick one or many entries below.";
    static final String LABEL_EXTENSION_MINI_JSON = "TDUF patch files (.mini.json)";
    static final String LABEL_EXTENSION_PCH = "TDUMT patch files (.PCH)";
    static final String LABEL_EXTENSION_TDUPK = "TDUPE Performance Packs (.tdupk)";
    static final String LABEL_EXTENSION_TXT = "Text files (.txt)";
    static final String LABEL_EXTENSION_ALL = "All files";

    public static final String TOOLTIP_BUTTON_ADD_LINKED_ENTRY = "Adds a new linked entry.";
    public static final String TOOLTIP_BUTTON_BROWSE_ENTRIES = "Browses available entries in topic.";
    public static final String TOOLTIP_BUTTON_BROWSE_RESOURCES = "Browses available resources in topic.";
    public static final String TOOLTIP_BUTTON_DELETE_LINKED_ENTRY = "Removes selected entry.";
    public static final String TOOLTIP_BUTTON_GOTO_LINKED_ENTRY = "Goes to target entry in linked topic.";
    public static final String TOOLTIP_BUTTON_GOTO_SELECTED_ENTRY = "Goes to selected entry in linked topic.";
    public static final String TOOLTIP_BUTTON_MOVE_LINKED_ENTRY_UP = "Moves current linked entry one rank up.";
    public static final String TOOLTIP_BUTTON_MOVE_LINKED_ENTRY_DOWN = "Moves current linked entry one rank down.";
    public static final String TOOLTIP_FIELD_TEMPLATE = "#%d:%s";
    public static final String TOOLTIP_ERROR_RESOURCE_NOT_FOUND = "No resource with provided identifier: either select or create one.";
    public static final String TOOLTIP_ERROR_CONTENT_NOT_FOUND = "No content with provided identifier: either select or create one.";

    public static final String COLUMN_HEADER_DATA = "Linked data";
    public static final String COLUMN_HEADER_ID = "#";
    public static final String COLUMN_HEADER_REF = "REF";

    public static final String TITLE_APPLICATION = "TDUF Database Editor";
    public static final String TITLE_BROWSE_GAME_DIRECTORY = "Browse game directory...";
    public static final String TITLE_SUB_ENTRIES = " : Content entries";
    public static final String TITLE_SUB_FIELDS = " : Content fields";
    public static final String TITLE_SUB_RESOURCES = " : Resources";
    public static final String TITLE_SUB_SAVE = " : Save database";
    public static final String TITLE_SUB_LOAD = " : Load database";
    public static final String TITLE_SUB_EXPORT = " : Export entry";
    public static final String TITLE_SUB_EXPORT_FILE = " : Export contents to file";
    public static final String TITLE_SUB_IMPORT = " : Import entry";
    public static final String TITLE_SUB_IMPORT_PERFORMANCE_PACK = " : Import TDUPE Performance Pack";
    public static final String TITLE_SUB_IMPORT_TDUMT_PATCH = " : Import TDUMT Patch";
    public static final String TITLE_SUB_RESET_DB_CACHE = " : Reset database cache";
    public static final String TITLE_SUB_RESET_SETTINGS = " : Reset settings";
    public static final String TITLE_FORMAT_LOAD = "Load %s file...";
    public static final String TITLE_FORMAT_SAVE = "Save to %s file...";
    public static final String TITLE_SEARCH_CONTENTS_ENTRY = "Search for contents entry";
    public static final String TITLE_SEARCH_RESOURCE_ENTRY = "Search for resource entry";

    public static final String MESSAGE_ADDED_RESOURCE = "Add resource in topic: ";
    public static final String MESSAGE_DATABASE_SAVED = "Current database was saved.";
    public static final String MESSAGE_DATABASE_LOAD_KO = "Database could not be loaded.";
    public static final String MESSAGE_DATABASE_SAVE_KO = "Current database could not be saved.";
    public static final String MESSAGE_DIFFERENT_RESOURCE = "Please try again with a different resource reference.";
    public static final String MESSAGE_EDITED_RESOURCE = "Edit: %s - %s";
    public static final String MESSAGE_ENTRIES_EXPORTED = "Selected entries were exported to TDUF patch.";
    public static final String MESSAGE_ALL_ENTRIES_EXPORTED = "All topic entries were exported to TDUF patch.";
    public static final String MESSAGE_DATA_IMPORTED = "Current patch file data was imported to database.";
    public static final String MESSAGE_DATA_IMPORTED_PERFORMANCE_PACK = "Current pack file data was imported to database.";
    public static final String MESSAGE_DATA_IMPORTED_TDUMT_PATCH = "Current patch file data was imported to database.";
    public static final String MESSAGE_SEE_LOGS = "See logs for details.";
    public static final String MESSAGE_UNABLE_EXPORT_ENTRIES = "Unable to export selected entries.";
    public static final String MESSAGE_UNABLE_EXPORT_ALL_ENTRIES = "Unable to export all entries from current topic.";
    public static final String MESSAGE_UNABLE_IMPORT_PATCH = "Unable to import TDUF patch.";
    public static final String MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK = "Unable to import TDUPE Performance Pack.";
    public static final String MESSAGE_UNABLE_IMPORT_TDUMT_PATCH = "Unable to import TDUMT Patch.";
    public static final String MESSAGE_DELETED_CACHE = "Database cache directory was deleted succesfully:";
    public static final String MESSAGE_DELETED_SETTINGS = "Application settings were deleted succesfully.";
    public static final String MESSAGE_RESTART_APP = "Please restart application.";
    public static final String MESSAGE_FILE_EXPORT_OK = "Contents were exported to file.";
    public static final String MESSAGE_FILE_EXPORT_KO = "Contents could not be exported to file.";

    public static final String TAB_NAME_DEFAULT = "Default";

    public static final String STATUS_SAVING = "Saving data, please wait...";
    public static final String STATUS_LOADING_DATA = "Loading data, please wait...";
    public static final String STATUS_FORMAT_SAVED_DATABASE = "Saved database: %s";
    public static final String STATUS_FORMAT_LOADED_DATABASE = "Loaded database: %s";

    private DisplayConstants() {}
}

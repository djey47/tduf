package fr.tduf.gui.database.common;

/**
 * Gives all constants to be displayed by controllers.
 */
public class DisplayConstants {
    public static final String SEPARATOR_VALUES = " - ";
    public static final String VALUE_ENTRY_CELL = "%d:%s";
    public static final String VALUE_ERROR_RESOURCE_NOT_FOUND = "<ERROR: resource not found!>";
    public static final String VALUE_ERROR_ENTRY_NOT_FOUND = "<ERROR: entry not found!>";
    public static final String VALUE_FIELD_DEFAULT = "";
    public static final String VALUE_RESOURCE_DEFAULT = "";
    public static final String VALUE_UNKNOWN = "<?>";

    public static final String LABEL_BUTTON_ALL = "All";
    public static final String LABEL_BUTTON_BROWSE = "...";
    public static final String LABEL_BUTTON_CANCEL = "Cancel";
    public static final String LABEL_BUTTON_CURRENT_LOCALE = "Current (%s)";
    public static final String LABEL_BUTTON_GOTO = "\u25B6";
    public static final String LABEL_BUTTON_MINUS = "-";
    public static final String LABEL_BUTTON_UP = "\u25B2";
    public static final String LABEL_BUTTON_DOWN = "\u25BC";
    public static final String LABEL_BUTTON_OK = "OK";
    public static final String LABEL_BUTTON_PLUS = "+";
    public static final String LABEL_BUTTON_CLOSE = "Close";
    public static final String LABEL_CHOICEBOX_LOCALE = "Locale:";
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
    public static final String LABEL_SEARCH_ENTRY = "With REF?";
    public static final String LABEL_STATUS_VERSION = "TDUF Database Editor by Djey.";
    public static final String LABEL_TEXTFIELD_REFERENCE = "Reference:";
    public static final String LABEL_TEXTFIELD_VALUE = "Value:";

    public static final String TOOLTIP_BUTTON_ADD_LINKED_ENTRY = "Adds a new linked entry.";
    public static final String TOOLTIP_BUTTON_BROWSE_ENTRIES = "Browses available entries in topic.";
    public static final String TOOLTIP_BUTTON_BROWSE_RESOURCES = "Browses available resources in topic.";
    public static final String TOOLTIP_BUTTON_DELETE_LINKED_ENTRY = "Removes selected entry.";
    public static final String TOOLTIP_BUTTON_GOTO_LINKED_ENTRY = "Goes to target entry in linked topic.";
    public static final String TOOLTIP_BUTTON_GOTO_SELECTED_ENTRY = "Goes to selected entry in linked topic.";
    public static final String TOOLTIP_BUTTON_MOVE_LINKED_ENTRY_UP = "Moves current linked entry one rank up.";
    public static final String TOOLTIP_BUTTON_MOVE_LINKED_ENTRY_DOWN = "Moves current linked entry one rank down.";
    public static final String TOOLTIP_FIELD_TEMPLATE = "#%d:%s";


    public static final String COLUMN_HEADER_DATA = "Linked data";
    public static final String COLUMN_HEADER_REF = "#";

    public static final String TITLE_APPLICATION = "TDUF Database Editor";
    public static final String TITLE_SUB_ENTRIES = " : Content entries";
    public static final String TITLE_SUB_RESOURCES = " : Resources";
    public static final String TITLE_SUB_EXPORT = " : Export entry";
    public static final String TITLE_SUB_IMPORT = " : Import entry";
    public static final String TITLE_SUB_IMPORT_PERFORMANCE_PACK = " : Import TDUPE Performance Pack";
    public static final String TITLE_SUB_SEARCH_ENTRY = " : Search for entry";
    public static final String TITLE_SUB_SEARCH_RESOURCE_ENTRY = " : Search for resource entry";

    public static final String MESSAGE_ADDED_RESOURCE = "Add resource in topic: ";
    public static final String MESSAGE_DATABASE_SAVED = "Current database was saved.";
    public static final String MESSAGE_DELETED_RESOURCE = "Delete: %s - %s";
    public static final String MESSAGE_DIFFERENT_RESOURCE = "Please try again with a different resource reference.";
    public static final String MESSAGE_EDITED_RESOURCE = "Edit: %s - %s";
    public static final String MESSAGE_ENTRY_EXPORTED = "Current entry was exported to TDUF patch.";
    public static final String MESSAGE_ALL_ENTRIES_EXPORTED = "All topic entries were exported to TDUF patch.";
    public static final String MESSAGE_DATA_IMPORTED = "Current patch file data was imported to database.";
    public static final String MESSAGE_DATA_IMPORTED_PERFORMANCE_PACK = "Current pack file data was imported to database.";
    public static final String MESSAGE_SEE_LOGS = "See logs for details.";
    public static final String MESSAGE_UNABLE_EXPORT_ENTRY = "Unable to export current entry.";
    public static final String MESSAGE_UNABLE_EXPORT_ALL_ENTRIES = "Unable to export all entries from current topic.";
    public static final String MESSAGE_UNABLE_IMPORT_PATCH = "Unable to import TDUF patch.";
    public static final String MESSAGE_UNABLE_IMPORT_PERFORMANCE_PACK = "Unable to import TDUPE Performance Pack.";

    public static final String QUESTION_AFFECTED_LOCALES = "Which locales should be affected?";

    public static final String WARNING_DELETED_RESOURCE = "This resource will be removed.";
}

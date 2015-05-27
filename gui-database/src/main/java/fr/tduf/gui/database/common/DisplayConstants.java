package fr.tduf.gui.database.common;

/**
 * Gives all constants to be displayed by controllers.
 */
public class DisplayConstants {
    public static final String SEPARATOR_VALUES = " - ";
    public static final String VALUE_UNKNOWN = "<?>";
    public static final String VALUE_ERROR_RESOURCE_NOT_FOUND = "<ERROR: resource not found!>";
    public static final String VALUE_ERROR_ENTRY_NOT_FOUND = "<ERROR: entry not found!>";

    public static final String LABEL_BUTTON_ALL = "All";
    public static final String LABEL_BUTTON_BROWSE = "...";
    public static final String LABEL_BUTTON_CANCEL = "Cancel";
    public static final String LABEL_BUTTON_CURRENT_LOCALE = "Current (%s)";
    public static final String LABEL_BUTTON_GOTO = "->";
    public static final String LABEL_BUTTON_OK = "OK";
    public static final String LABEL_TEXTFIELD_REFERENCE = "Reference:";
    public static final String LABEL_TEXTFIELD_VALUE = "Value:";

    public static final String COLUMN_HEADER_REF = "#";
    public static final String COLUMN_HEADER_DATA = "Linked data";

    public static final String TITLE_APPLICATION = "TDUF Database Editor";
    public static final String TITLE_SUB_RESOURCES = " : Resources";

    public static final String MESSAGE_ADDED_RESOURCE = "Add resource in topic: ";
    public static final String MESSAGE_DATABASE_SAVED = "Current database was saved.";
    public static final String MESSAGE_DELETED_RESOURCE = "Delete: %s - %s";
    public static final String MESSAGE_EDITED_RESOURCE = "Edit: %s - %s";
    public static final String MESSAGE_DIFFERENT_RESOURCE = "Please try again with a different resource reference.";

    public static final String QUESTION_AFFECTED_LOCALES = "Which locales should be affected?";

    public static final String WARNING_DELETED_RESOURCE = "This resource will be removed.";
}
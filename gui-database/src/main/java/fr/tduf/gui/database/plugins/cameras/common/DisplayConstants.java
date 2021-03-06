package fr.tduf.gui.database.plugins.cameras.common;

public class DisplayConstants {

    public static final String LABEL_FORMAT_CAMERA_ITEM = "%s (%d) - %d views";
    public static final String LABEL_FORMAT_VIEW_ITEM = "(%d) %s";

    public static final String LABEL_CAMERA_RAW_VALUE_DEFAULT = "";
    public static final String LABEL_NEW_SET = "NEW CAMERA SET";

    public static final String LABEL_AVAILABLE_CAMERAS = "Available cameras:";
    public static final String LABEL_AVAILABLE_VIEWS = "Available views:";

    public static final String LABEL_ADD_BUTTON = "+";
    public static final String LABEL_DEL_BUTTON = "-";
    public static final String LABEL_IMPORT_SET_BUTTON = "Import...";
    public static final String LABEL_EXPORT_SET_BUTTON = "Export";
    public static final String LABEL_EXPORT_CURRENT_BUTTON = "Current view only...";
    public static final String LABEL_EXPORT_ALL_BUTTON = "All views from this set...";

    static final String LABEL_EXTENSION_CAM_JSON = "TDUF camera patch files (.cam.json)";

    public static final String LABEL_ERROR_TOOLTIP = "Set identifier is not valid or does not exist in provided cameras.\r\n" +
            "Either select or create a new one.";

    public static final String HEADER_PROPTABLE_SETTING = "Setting";
    public static final String HEADER_PROPTABLE_DESCRIPTION = "?";
    public static final String HEADER_PROPTABLE_VALUE = "Value";

    public static final String TOOLTIP_ADD_BUTTON = "Creates a new camera set, based on current one";
    public static final String TOOLTIP_DEL_BUTTON = "Deletes current camera set";
    public static final String TOOLTIP_IMPORT_SET_BUTTON = "Imports one or many camera sets from camera patch";
    public static final String TOOLTIP_EXPORT_SET_BUTTON = "Exports one or many views from current set";

    public static final String TITLE_ADD_SET = "Add camera set...";
    public static final String TITLE_IMPORT = "Import camera sets...";
    public static final String TITLE_EXPORT = "Export camera set...";

    public static final String MESSAGE_ADD_SET_IDENTIFIER = "Enter a numeric identifier for new camera set:";
    public static final String MESSAGE_DATA_IMPORTED = "Camera set data was imported succesfully";
    public static final String MESSAGE_DATA_EXPORTED = "Camera set data was exported succesfully";
    public static final String MESSAGE_UNABLE_IMPORT_PATCH = "Camera set could not be imported";
    public static final String MESSAGE_UNABLE_EXPORT_PATCH = "Camera set could not be exported";

    public static final String FORMAT_MESSAGE_WARN_NO_CAMBIN = "No Cameras.bin file was found in database directory: %s";

    private DisplayConstants() {}
}

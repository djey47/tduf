package fr.tduf.gui.database.common;

import javafx.stage.FileChooser;

import java.util.Collections;

import static java.util.Arrays.asList;

/**
 * Gives all constants needed to link to Java FX resources.
 */
public class FxConstants {
    public static final String PATH_RESOURCE_MAIN_STAGE_DESIGNER = "/gui-database/designer/MainDesigner.fxml";
    public static final String PATH_RESOURCE_RES_STAGE_DESIGNER = "/gui-database/designer/ResourcesDesigner.fxml";
    public static final String PATH_ENTRIES_RES_STAGE_DESIGNER = "/gui-database/designer/EntriesDesigner.fxml";
    public static final String PATH_FIELDS_BROWSER_STAGE_DESIGNER = "/gui-database/designer/FieldsBrowserDesigner.fxml";

    public static final String PATH_RESOURCE_CSS_TOOLBARS = "/gui-database/css/ToolBars.css";
    public static final String PATH_RESOURCE_CSS_TABCONTENTS = "/gui-database/css/TabContents.css";
    public static final String PATH_RESOURCE_CSS_PANES = "/gui-database/css/Panes.css";
    public static final String PATH_RESOURCE_CSS_COLORS = "/gui-database/css/Colors.css";
    public static final String PATH_RESOURCE_CSS_COMMON = "/gui-database/css/Common.css";

    public static final String PATH_RESOURCE_IMG_SPLASH = "/gui-database/img/splash.jpg";

    public static final String CSS_CLASS_FIELD_LABEL = "fieldLabel";
    public static final String CSS_CLASS_FIELD_NAME = "fieldName";
    public static final String CSS_CLASS_READONLY_FIELD = "readonlyField";
    public static final String CSS_CLASS_ERROR = "error";
    public static final String CSS_CLASS_VERTICAL_BUTTON_BOX = "verticalButtonBox";
    public static final String CSS_CLASS_BUTTON = "button";
    public static final String CSS_CLASS_BUTTON_SMALL = "smallButton";
    public static final String CSS_CLASS_FIELD_BOX = "fieldBox";
    public static final String CSS_CLASS_VALUE_TEXT_FIELD = "valueTextField";
    public static final String CSS_CLASS_TAB = "tab";
    public static final String CSS_CLASS_TAB_CONTAINER = "tabContainer";
    public static final String CSS_CLASS_CHECKBOX = "checkBox";
    public static final String CSS_CLASS_COMBOBOX = "comboBox";
    public static final String CSS_CLASS_TABLEVIEW = "tableView";

    public static final FileChooser.ExtensionFilter EXTENSION_FILTER_TDUF_PATCH = new FileChooser.ExtensionFilter(DisplayConstants.LABEL_EXTENSION_MINI_JSON, asList("*.mini.json", "*.MINI.JSON"));
    public static final FileChooser.ExtensionFilter EXTENSION_FILTER_TDUMT_PATCH = new FileChooser.ExtensionFilter(DisplayConstants.LABEL_EXTENSION_PCH, asList("*.pch", "*.PCH"));
    public static final FileChooser.ExtensionFilter EXTENSION_FILTER_TDUPE_PP = new FileChooser.ExtensionFilter(DisplayConstants.LABEL_EXTENSION_TDUPK, asList("*.tdupk", "*.TDUPK"));
    public static final FileChooser.ExtensionFilter EXTENSION_FILTER_TEXT = new FileChooser.ExtensionFilter(DisplayConstants.LABEL_EXTENSION_TXT, asList("*.txt", "*.TXT"));
    public static final FileChooser.ExtensionFilter EXTENSION_FILTER_ALL = new FileChooser.ExtensionFilter(DisplayConstants.LABEL_EXTENSION_ALL, Collections.singletonList("*"));

    private FxConstants() {}
}

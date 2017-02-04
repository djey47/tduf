package fr.tduf.gui.database.plugins.cameras.common;

import javafx.stage.FileChooser;

import static fr.tduf.gui.database.plugins.cameras.common.DisplayConstants.LABEL_EXTENSION_CAM_JSON;
import static java.util.Arrays.asList;

public class FxConstants {
    public static final String PATH_RESOURCE_CSS_CAMERAS = "/gui-database/css/plugins/CamerasPlugin.css";

    public static final String CSS_CLASS_MAIN_COLUMN = "camMainColumn";
    public static final String CSS_CLASS_CAM_SELECTOR_BOX = "camSelectorBox";
    public static final String CSS_CLASS_VIEW_SELECTOR_BOX = "viewSelectorBox";
    public static final String CSS_CLASS_CAM_SELECTOR_COMBOBOX = "camSelectorComboBox";
    public static final String CSS_CLASS_VIEW_SELECTOR_COMBOBOX = "viewSelectorComboBox";
    public static final String CSS_CLASS_SET_PROPERTY_TABLEVIEW = "setPropertyTableView";
    public static final String CSS_CLASS_SETTING_TABLECOLUMN = "settingTableColumn";
    public static final String CSS_CLASS_DESCRIPTION_TABLECOLUMN = "descriptionTableColumn";
    public static final String CSS_CLASS_VALUE_TABLECOLUMN = "valueTableColumn";

    public static final FileChooser.ExtensionFilter EXTENSION_FILTER_TDUF_CAMERA_PATCH = new FileChooser.ExtensionFilter(LABEL_EXTENSION_CAM_JSON, asList("*.cam.json", "*.MINI.JSON"));

    private FxConstants() {}
}

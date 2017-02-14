package fr.tduf.gui.common;

import javafx.css.PseudoClass;

/**
 * Gives all constants needed to link to Java FX resources.
 */
public class FxConstants {
    private FxConstants() {}

    public static final String PATH_RESOURCE_DB_CHECK_STAGE_DESIGNER = "/gui-common/designer/DatabaseCheckDesigner.fxml";

    public static final String PATH_RESOURCE_CSS_CHECK = "/gui-common/css/DatabaseCheck.css";
    public static final String PATH_RESOURCE_CSS_DIALOGS = "/gui-common/css/Dialogs.css";

    public static final String CSS_CLASS_INTEGRITY_ERROR_LABEL = "integrityErrorLabel";
    public static final String CSS_CLASS_TEXT_FIELD = "textField";

    public static PseudoClass CSS_PSEUDO_CLASS_ERROR = PseudoClass.getPseudoClass("error");
}

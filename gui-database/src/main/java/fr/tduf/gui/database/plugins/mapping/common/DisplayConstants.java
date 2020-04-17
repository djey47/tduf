package fr.tduf.gui.database.plugins.mapping.common;

public class DisplayConstants {
    public static final String HEADER_FILESTABLE_KIND = "Kind";
    public static final String HEADER_FILESTABLE_PATH = "Path";
    public static final String HEADER_FILESTABLE_EXISTS = "Exists?";
    public static final String HEADER_FILESTABLE_REGISTERED = "Registered?";
    
    public static final String TOOLTIP_BUTTON_SEE_DIRECTORY = "Opens selected directory in file browser.";
    public static final String TOOLTIP_BUTTON_REGISTER = "Adds this file to Bnk1.map.";
    
    public static final String LABEL_BUTTON_REGISTER = "Register";

    public static final String LABEL_ERROR_TOOLTIP_UNREGISTERED = "One of listed files is not registered into Bnk1.map, may not be taken into account by the game.";

    public static final String FORMAT_MESSAGE_WARN_NO_MAPPING = "No bnk1.map file was found under game directory: %s";

    private DisplayConstants() {}
}

package fr.tduf.gui.database.plugins.materials.common;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Color;

import java.util.HashMap;
import java.util.Map;

public class DisplayConstants {
    public static final String FORMAT_MESSAGE_WARN_NO_MATERIALS = "No colors.bnk file was found under game directory: %s";

    public static final String LABEL_AVAILABLE_MATERIALS = "Available materials: ";
    public static final String LABEL_COLOR_AMBIENT = "Ambient";
    public static final String LABEL_COLOR_DIFFUSE = "Diffuse";
    public static final String LABEL_COLOR_SPECULAR = "Specular";
    public static final String LABEL_COLOR_OTHER = "Other (unused?)";
    public static final String LABEL_SHADER = "Shader: ";
    public static final String LABEL_ALPHA = "Alpha: ";
    public static final String LABEL_NO_NAME = "?";
    public static final String LABEL_BUTTON_ADVANCED_INFO = "More...";
    public static final String LABEL_COLOR_DESCRIPTION_DEFAULT = "(no color)";
    public static final String LABEL_TOOLTIP_COLOR_PICKER = "Allows to pick a color from palette";
    public static final String LABEL_TOOLTIP_ADVANCED_INFO = "Displays advanced information on shader and layers";

    public static final Map<Color.ColorKind, String> DICTIONARY_LABELS_COLORS = new HashMap<>();
    static {
        DICTIONARY_LABELS_COLORS.put(Color.ColorKind.AMBIENT, LABEL_COLOR_AMBIENT);
        DICTIONARY_LABELS_COLORS.put(Color.ColorKind.DIFFUSE, LABEL_COLOR_DIFFUSE);
        DICTIONARY_LABELS_COLORS.put(Color.ColorKind.OTHER, LABEL_COLOR_OTHER);
        DICTIONARY_LABELS_COLORS.put(Color.ColorKind.SPECULAR, LABEL_COLOR_SPECULAR);
    }

    public static final String FORMAT_ALPHA_VALUE = "%d with blending (x=%s y=%d)";
    public static final String FORMAT_MATERIAL_LABEL = "%s (%s)";
    public static final String FORMAT_SHADER_LABEL = "%s (%s)";
    public static final String FORMAT_DESCRIPTION_RESOURCE_CREATION = "It will be created automatically at REF %s in %s topic.";
    public static final String FORMAT_MESSAGE_RESOURCE_NAME_NOT_FOUND = "A resource was not found for material name: %s";
    public static final String FORMAT_TITLE_ADVANCED = "Advanced info for material: %s";

    public static final String TITLE_SELECTING_MATERIAL = "Selecting new material...";
    public static final String TITLE_SUB_ADVANCED_INFO = " : Material advanced information";
}

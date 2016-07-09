package fr.tduf.gui.installer.common;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Gives all constants to access particular information about TDU files.
 */
public class FileConstants {
    public static final Pattern PATTERN_INTERIOR_MODEL_BANK_FILE_NAME = Pattern.compile(".+_I\\.bnk", CASE_INSENSITIVE);
    public static final Pattern PATTERN_RIM_BANK_FILE_NAME = Pattern.compile(".+_(F|R)_([0-9]{2})\\.bnk", CASE_INSENSITIVE);

    public static final String SUFFIX_INTERIOR_BANK_FILE = "_I";
    public static final String SUFFIX_AUDIO_BANK_FILE = "_audio";

    public static final String INDICATOR_FRONT_RIMS = "F";

    public static final String DIRECTORY_NAME_VEHICLES = "Vehicules";
    public static final String DIRECTORY_NAME_FRONT_END = "FrontEnd";
    public static final String DIRECTORY_NAME_HUDS = "Gauges";

    public static final String RESOURCE_NAME_CLEAN_PATCH = "clean-slot.mini.json";
    public static final String RESOURCE_NAME_TDUCP_CAR_PATCH = "tducp-car-slot.mini.json";
    public static final String RESOURCE_NAME_TDUCP_BIKE_PATCH = "tducp-bike-slot.mini.json";

    private FileConstants() {}
}

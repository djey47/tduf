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

    public static final String FILE_NAME_EFFECTIVE_PROPERTIES = "installed.properties";
    public static final String FILE_NAME_EFFECTIVE_PATCH = "installed.mini.json";

    private FileConstants() {}
}

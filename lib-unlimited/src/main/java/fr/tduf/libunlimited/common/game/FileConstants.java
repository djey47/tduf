package fr.tduf.libunlimited.common.game;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Constants for common game file names and directories
 */
public class FileConstants {
    public static final String DIRECTORY_EURO = "Euro";
    public static final String DIRECTORY_BANKS = "Bnk";
    public static final String DIRECTORY_DATABASE = "Database";
    public static final String DIRECTORY_NAME_VEHICLES = "Vehicules";
    public static final String DIRECTORY_NAME_FRONT_END = "FrontEnd";
    public static final String DIRECTORY_NAME_HUDS = "Gauges";
    public static final String DIRECTORY_SOUNDS = "Sound";

    public static final Pattern PATTERN_INTERIOR_MODEL_BANK_FILE_NAME = Pattern.compile(".+_I\\.bnk", CASE_INSENSITIVE);
    public static final Pattern PATTERN_RIM_BANK_FILE_NAME = Pattern.compile(".+_(F|R)_([0-9]{2})\\.bnk", CASE_INSENSITIVE);

    public static final String PREFIX_SPOT_EXTERIOR_BANK = "e";
    public static final String PREFIX_SPOT_INTERIOR_BANK = "i";
    public static final String PREFIX_SPOT_GARAGE_BANK = "g";
    public static final String PREFIX_SPOT_LOUNGE_BANK = "l";

    public static final String INDICATOR_FRONT_RIMS = "F";
    public static final String INDICATOR_REAR_RIMS = "R";

    public static final String SUFFIX_INTERIOR_BANK_FILE = "_I";
    public static final String SUFFIX_AUDIO_BANK_FILE = "_audio";

    private FileConstants() {}
}
